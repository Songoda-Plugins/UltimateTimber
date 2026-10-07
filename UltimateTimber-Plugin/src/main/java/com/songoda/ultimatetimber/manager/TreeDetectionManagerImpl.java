package com.songoda.ultimatetimber.manager;

import com.songoda.core.SongodaPlugin;
import com.songoda.core.compatibility.folia.SchedulerUtils;
import com.songoda.core.hooks.internal.ReloadHook;
import com.songoda.core.vinject.annotation.RegisterReloadHook;
import com.songoda.ultimatetimber.api.manager.PlacedBlockManager;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.manager.TreeDetectionManager;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockSet;
import com.songoda.ultimatetimber.api.tree.TreeBlockType;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.tree.DetectedTreeImpl;
import com.songoda.ultimatetimber.tree.TreeBlockImpl;
import com.songoda.ultimatetimber.utils.LongSet;
import com.songoda.ultimatetimber.utils.TreeGeometry;
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.annotation.component.Component;
import net.vortexdevelopment.vinject.annotation.lifecycle.PostConstruct;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Service scanning world blocks to detect and validate trees.
 *
 * <p>Detection works on integer block coordinates only, so scanning a canopy allocates no
 * intermediate locations. Distance checks are boundary inclusive, and scanning stops once the
 * configured log limit is reached instead of walking an oversized tree in full.
 */
@Component
@RegisterReloadHook
public class TreeDetectionManagerImpl implements TreeDetectionManager, ReloadHook {

    /**
     * Leaves kept per configured log, which bounds oversized canopies the same way the log limit does.
     */
    private static final int LEAF_LIMIT_RATIO = 8;

    @Inject
    private TimberConfig config;

    @Inject
    private TreeDefinitionManager treeDefinitionManager;

    @Inject
    private PlacedBlockManager placedBlockManager;

    private volatile int leavesRequiredForTree = 5;
    private volatile boolean onlyDetectLogsUpwards = true;
    private volatile boolean breakEntireTreeBase = false;
    private volatile boolean destroyLeaves = true;
    private volatile int maxLogsPerChop = 150;
    private volatile int maxLeavesPerTree = 150 * LEAF_LIMIT_RATIO;

    private static Block getRelativeInCurrentRegion(Block block, int offsetX, int offsetY, int offsetZ) {
        if (SchedulerUtils.isFolia()) {
            Location targetLocation = block.getLocation().add(offsetX, offsetY, offsetZ);
            if (!SchedulerUtils.isOwnedByCurrentRegion(targetLocation)) {
                throw new CrossRegionBlockAccessException();
            }
        }

        return block.getRelative(offsetX, offsetY, offsetZ);
    }

    @PostConstruct
    public void initialize() {
        onReload();
    }

    @Override
    public void onReload() {
        this.leavesRequiredForTree = this.config.getLeavesRequiredForTree();
        this.onlyDetectLogsUpwards = this.config.isOnlyDetectLogsUpwards();
        this.breakEntireTreeBase = this.config.isBreakEntireTreeBase();
        this.destroyLeaves = this.config.isDestroyLeaves();
        this.maxLogsPerChop = Math.max(1, this.config.getMaxLogsPerChop());
        this.maxLeavesPerTree = (int) Math.min(Integer.MAX_VALUE, (long) this.maxLogsPerChop * LEAF_LIMIT_RATIO);
    }

    @Override
    public @Nullable DetectedTree detectTree(@NotNull Block initialBlock) {
        if (SchedulerUtils.isFolia() && !SchedulerUtils.isOwnedByCurrentRegion(initialBlock)) {
            return null;
        }

        try {
            return this.detectTreeInCurrentRegion(initialBlock);
        } catch (CrossRegionBlockAccessException ignored) {
            return null;
        }
    }

    private @Nullable DetectedTree detectTreeInCurrentRegion(@NotNull Block initialBlock) {
        if (this.placedBlockManager.isBlockPlaced(initialBlock)) {
            return null;
        }

        Set<TreeDefinition> possibleTreeDefinitions = new HashSet<>(this.treeDefinitionManager.getTreeDefinitionsForLog(initialBlock));
        if (possibleTreeDefinitions.isEmpty()) {
            return null;
        }

        TreeBlock<Block> initialTreeBlock = new TreeBlockImpl(initialBlock, TreeBlockType.LOG);
        TreeBlockSet<Block> detectedTreeBlocks = new TreeBlockSet<>(initialTreeBlock);

        List<Block> trunkBlocks = collectTrunkBlocks(possibleTreeDefinitions, initialBlock);

        LongSet visitedBranchLocations = new LongSet();
        for (Block trunkBlock : trunkBlocks) {
            recursiveBranchSearch(possibleTreeDefinitions, trunkBlocks, detectedTreeBlocks, trunkBlock, initialBlock.getY(), visitedBranchLocations);
        }

        boolean detectLeavesDiagonally = possibleTreeDefinitions.stream().anyMatch(TreeDefinition::shouldDetectLeavesDiagonally);
        detectLeaves(possibleTreeDefinitions, detectedTreeBlocks, detectLeavesDiagonally);

        if (possibleTreeDefinitions.isEmpty()) {
            return null;
        }

        TreeDefinition actualTreeDefinition = possibleTreeDefinitions.iterator().next();

        if (detectedTreeBlocks.getLeafBlocks().size() < this.leavesRequiredForTree) {
            return null;
        }

        if (!this.destroyLeaves) {
            detectedTreeBlocks.removeAll(TreeBlockType.LEAF);
        }

        if (this.breakEntireTreeBase && isTreeBaseBlocked(possibleTreeDefinitions, detectedTreeBlocks, actualTreeDefinition, initialBlock.getY())) {
            return null;
        }

        return new DetectedTreeImpl(actualTreeDefinition, detectedTreeBlocks);
    }

    @Override
    public void detectTreeAsync(@NotNull Block initialBlock, @NotNull Consumer<DetectedTree> callback) {
        if (!SchedulerUtils.isFolia()) {
            callback.accept(this.detectTree(initialBlock));
            return;
        }

        if (!SchedulerUtils.isOwnedByCurrentRegion(initialBlock)) {
            SchedulerUtils.runLocationTask(SongodaPlugin.getInstance(), initialBlock.getLocation(),
                    () -> this.detectTreeAsync(initialBlock, callback));
            return;
        }

        if (this.placedBlockManager.isBlockPlaced(initialBlock)) {
            callback.accept(null);
            return;
        }

        Material initialMaterial = initialBlock.getType();
        Set<TreeDefinition> possibleTreeDefinitions = this.treeDefinitionManager.getTreeDefinitionsForLog(initialBlock);
        if (possibleTreeDefinitions.isEmpty()) {
            callback.accept(null);
            return;
        }

        Location initialLocation = initialBlock.getLocation().clone();
        new RegionAwareTreeDetector(
                SongodaPlugin.getInstance(),
                initialBlock,
                initialLocation,
                initialMaterial,
                possibleTreeDefinitions,
                this.treeDefinitionManager,
                this.placedBlockManager,
                this.leavesRequiredForTree,
                this.onlyDetectLogsUpwards,
                this.breakEntireTreeBase,
                this.destroyLeaves,
                this.maxLogsPerChop,
                callback
        ).start();
    }

    /**
     * Walks the trunk upwards, and downwards when configured, narrowing the candidate definitions as
     * it goes. The returned list starts at the lowest trunk block, so a tree that hits the log limit
     * keeps its lowest logs.
     *
     * @param treeDefinitions The candidate tree definitions, narrowed in place
     * @param initialBlock    The block the tree topple started from
     * @return The trunk blocks from lowest to highest
     */
    private List<Block> collectTrunkBlocks(Set<TreeDefinition> treeDefinitions, Block initialBlock) {
        List<Block> trunkBlocks = new ArrayList<>();
        trunkBlocks.add(initialBlock);

        Block targetBlock = initialBlock;
        while (trunkBlocks.size() < this.maxLogsPerChop) {
            Block nextBlock = getRelativeInCurrentRegion(targetBlock, 0, 1, 0);
            if (!isLogBlock(treeDefinitions, nextBlock)) {
                break;
            }
            trunkBlocks.add(nextBlock);
            narrowDefinitions(treeDefinitions, nextBlock.getType(), TreeBlockType.LOG);
            targetBlock = nextBlock;
        }

        if (!this.onlyDetectLogsUpwards) {
            targetBlock = initialBlock;
            while (trunkBlocks.size() < this.maxLogsPerChop) {
                Block nextBlock = getRelativeInCurrentRegion(targetBlock, 0, -1, 0);
                if (!isLogBlock(treeDefinitions, nextBlock)) {
                    break;
                }
                trunkBlocks.add(nextBlock);
                narrowDefinitions(treeDefinitions, nextBlock.getType(), TreeBlockType.LOG);
                targetBlock = nextBlock;
            }
        }

        trunkBlocks.sort(Comparator.comparingInt(Block::getY));
        return trunkBlocks;
    }

    /**
     * Recursively searches for branches off a given block.
     *
     * @param treeDefinitions        The candidate tree definitions, narrowed in place
     * @param trunkBlocks            The trunk blocks of the tree for distance checks
     * @param treeBlocks             The detected tree blocks
     * @param block                  The block to search from
     * @param startingBlockY         The Y coordinate of the initial block
     * @param visitedBranchLocations Packed keys of already visited locations to avoid redundant checks
     */
    private void recursiveBranchSearch(Set<TreeDefinition> treeDefinitions,
                                       List<Block> trunkBlocks,
                                       TreeBlockSet<Block> treeBlocks,
                                       Block block,
                                       int startingBlockY,
                                       LongSet visitedBranchLocations) {
        if (!hasLogBudget(treeBlocks)) {
            return;
        }

        int[][] offsets = this.onlyDetectLogsUpwards ? TreeGeometry.BRANCH_OFFSETS : TreeGeometry.TRUNK_OFFSETS;
        for (int[] offset : offsets) {
            Block targetBlock = getRelativeInCurrentRegion(block, offset[0], offset[1], offset[2]);
            if (!visitedBranchLocations.add(TreeGeometry.packBlockKey(targetBlock))) {
                continue;
            }

            if (!isBranchLog(treeDefinitions, targetBlock, trunkBlocks)) {
                continue;
            }

            if (!hasLogBudget(treeBlocks)) {
                return;
            }

            treeBlocks.add(new TreeBlockImpl(targetBlock, TreeBlockType.LOG));
            narrowDefinitions(treeDefinitions, targetBlock.getType(), TreeBlockType.LOG);
            if (!this.onlyDetectLogsUpwards || targetBlock.getY() > startingBlockY) {
                recursiveBranchSearch(treeDefinitions, trunkBlocks, treeBlocks, targetBlock, startingBlockY, visitedBranchLocations);
            }
        }
    }

    /**
     * Checks whether the detected tree may still accept logs.
     *
     * @param treeBlocks The detected tree blocks
     * @return True while the configured log limit has room left
     */
    private boolean hasLogBudget(TreeBlockSet<Block> treeBlocks) {
        return treeBlocks.getLogBlocks().size() < this.maxLogsPerChop;
    }

    /**
     * Searches the canopy around every detected log block.
     *
     * @param treeDefinitions        The candidate tree definitions, narrowed in place
     * @param detectedTreeBlocks     The detected tree blocks, leaves are added to it
     * @param detectLeavesDiagonally Whether leaves should be searched diagonally
     */
    private void detectLeaves(Set<TreeDefinition> treeDefinitions, TreeBlockSet<Block> detectedTreeBlocks, boolean detectLeavesDiagonally) {
        LeafSearchContext context = new LeafSearchContext(detectedTreeBlocks, detectLeavesDiagonally, this.maxLeavesPerTree);
        for (TreeBlock<Block> logBlock : detectedTreeBlocks.getLogBlocks()) {
            if (!context.canAddLeaf()) {
                return;
            }
            recursiveLeafSearch(treeDefinitions, context, logBlock.block());
        }
    }

    /**
     * Recursively searches for leaves next to a given block.
     *
     * @param treeDefinitions The candidate tree definitions, narrowed in place
     * @param context         The detected log geometry and visited locations
     * @param block           The block to search from
     */
    private void recursiveLeafSearch(Set<TreeDefinition> treeDefinitions, LeafSearchContext context, Block block) {
        int[][] offsets = context.detectLeavesDiagonally ? TreeGeometry.TRUNK_OFFSETS : TreeGeometry.LEAF_OFFSETS;
        for (int[] offset : offsets) {
            Block targetBlock = getRelativeInCurrentRegion(block, offset[0], offset[1], offset[2]);
            if (!context.visitedLocations.add(TreeGeometry.packBlockKey(targetBlock))) {
                continue;
            }

            if (!isValidLeafType(treeDefinitions, context, targetBlock) || doesLeafBorderInvalidLog(treeDefinitions, context, targetBlock)) {
                continue;
            }

            if (!context.canAddLeaf()) {
                return;
            }

            context.treeBlocks.add(new TreeBlockImpl(targetBlock, TreeBlockType.LEAF));
            context.leafCount++;
            narrowDefinitions(treeDefinitions, targetBlock.getType(), TreeBlockType.LEAF);
            recursiveLeafSearch(treeDefinitions, context, targetBlock);
        }
    }

    /**
     * Checks whether a block is a leaf of the tree, meaning it matches a candidate definition and sits
     * within the configured leaf distance of a detected log.
     *
     * @param treeDefinitions The candidate tree definitions
     * @param context         The detected log geometry
     * @param block           The block to check
     * @return True if the block is a valid leaf
     */
    private boolean isValidLeafType(Set<TreeDefinition> treeDefinitions, LeafSearchContext context, Block block) {
        Material material = this.getMaterialInCurrentRegion(block);
        if (!isLeafMaterial(treeDefinitions, material)) {
            return false;
        }

        if (this.placedBlockManager.isBlockPlaced(block)) {
            return false;
        }

        if (context.logXs.length == 0) {
            return true;
        }

        return isLogWithinDistance(context, block.getX(), block.getY(), block.getZ(), maxLeafDistanceFromLog(treeDefinitions));
    }

    /**
     * Checks whether any detected log is within the given radius. Offsets are rejected per axis with
     * integer comparisons, so only candidates inside the bounding box reach the distance math.
     *
     * @param context The detected log geometry
     * @param x       The block x coordinate
     * @param y       The block y coordinate
     * @param z       The block z coordinate
     * @param radius  The inclusive leaf radius
     * @return True if a detected log is within the radius
     */
    private boolean isLogWithinDistance(LeafSearchContext context, int x, int y, int z, int radius) {
        long maxDistanceSquared = (long) radius * radius;
        for (int index = 0; index < context.logXs.length; index++) {
            int dx = x - context.logXs[index];
            if (dx > radius || dx < -radius) {
                continue;
            }
            int dy = y - context.logYs[index];
            if (dy > radius || dy < -radius) {
                continue;
            }
            int dz = z - context.logZs[index];
            if (dz > radius || dz < -radius) {
                continue;
            }
            if (TreeGeometry.isWithinSquaredDistance(dx, dy, dz, maxDistanceSquared)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a leaf borders a log that is not part of this tree.
     *
     * @param treeDefinitions The candidate tree definitions
     * @param context         The detected log geometry
     * @param block           The block to check
     * @return True if the leaf borders an invalid log, otherwise false
     */
    private boolean doesLeafBorderInvalidLog(Set<TreeDefinition> treeDefinitions, LeafSearchContext context, Block block) {
        for (int[] offset : TreeGeometry.TRUNK_OFFSETS) {
            Block targetBlock = getRelativeInCurrentRegion(block, offset[0], offset[1], offset[2]);
            if (isLogBlock(treeDefinitions, targetBlock) && !context.detectedLogPositions.contains(TreeGeometry.packBlockKey(targetBlock))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether a block is a matching log that is not too far from the trunk.
     *
     * @param treeDefinitions The candidate tree definitions
     * @param block           The block to check
     * @param trunkBlocks     The trunk blocks to measure against
     * @return True if the block is a valid branch log
     */
    private boolean isBranchLog(Set<TreeDefinition> treeDefinitions, Block block, List<Block> trunkBlocks) {
        if (!isLogBlock(treeDefinitions, block)) {
            return false;
        }

        double maxDistanceSquared = maxLogDistanceSquared(treeDefinitions);
        int axisLimit = TreeGeometry.axisLimit(maxDistanceSquared);
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        for (Block trunkBlock : trunkBlocks) {
            int dx = x - trunkBlock.getX();
            if (dx > axisLimit || dx < -axisLimit) {
                continue;
            }
            int dy = y - trunkBlock.getY();
            if (dy > axisLimit || dy < -axisLimit) {
                continue;
            }
            int dz = z - trunkBlock.getZ();
            if (dz > axisLimit || dz < -axisLimit) {
                continue;
            }
            if (TreeGeometry.isWithinSquaredDistance(dx, dy, dz, maxDistanceSquared)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether a block is a log material that was not placed by a player. The material is tested
     * before the placed block repository, so air and foliage candidates never probe the repository.
     *
     * @param treeDefinitions The candidate tree definitions
     * @param block           The block to check
     * @return True if the block is a usable log
     */
    private boolean isLogBlock(Set<TreeDefinition> treeDefinitions, Block block) {
        return isLogMaterial(treeDefinitions, this.getMaterialInCurrentRegion(block)) && !this.placedBlockManager.isBlockPlaced(block);
    }

    private Material getMaterialInCurrentRegion(Block block) {
        this.ensureCurrentRegion(block);
        return block.getType();
    }

    private void ensureCurrentRegion(Block block) {
        if (SchedulerUtils.isFolia() && !SchedulerUtils.isOwnedByCurrentRegion(block)) {
            throw new CrossRegionBlockAccessException();
        }
    }

    private boolean isLogMaterial(Set<TreeDefinition> treeDefinitions, Material material) {
        for (TreeDefinition definition : treeDefinitions) {
            if (definition.getLogMaterials().contains(material)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLeafMaterial(Set<TreeDefinition> treeDefinitions, Material material) {
        for (TreeDefinition definition : treeDefinitions) {
            if (definition.getLeafMaterials().contains(material)) {
                return true;
            }
        }
        return false;
    }

    private int maxLeafDistanceFromLog(Set<TreeDefinition> treeDefinitions) {
        int maxDistance = 0;
        for (TreeDefinition definition : treeDefinitions) {
            int distance = definition.getMaxLeafDistanceFromLog();
            if (distance > maxDistance) {
                maxDistance = distance;
            }
        }
        return maxDistance;
    }

    private double maxLogDistanceSquared(Set<TreeDefinition> treeDefinitions) {
        double maxDistanceSquared = 0.0;
        for (TreeDefinition definition : treeDefinitions) {
            double distance = definition.getMaxLogDistanceFromTrunk();
            double distanceSquared = distance * distance;
            if (!this.onlyDetectLogsUpwards) {
                distanceSquared *= 1.5;
            }
            if (distanceSquared > maxDistanceSquared) {
                maxDistanceSquared = distanceSquared;
            }
        }
        return maxDistanceSquared;
    }

    /**
     * Removes every candidate definition that does not list the given material.
     *
     * @param treeDefinitions The candidate tree definitions, narrowed in place
     * @param material        The material that was matched
     * @param treeBlockType   The type of tree block the material came from
     */
    private void narrowDefinitions(Set<TreeDefinition> treeDefinitions, Material material, TreeBlockType treeBlockType) {
        treeDefinitions.removeIf(treeDefinition -> !containsMaterial(treeDefinition, material, treeBlockType));
    }

    private boolean containsMaterial(TreeDefinition definition, Material material, TreeBlockType treeBlockType) {
        if (treeBlockType == TreeBlockType.LOG) {
            return definition.getLogMaterials().contains(material);
        }
        if (treeBlockType == TreeBlockType.LEAF) {
            return definition.getLeafMaterials().contains(material);
        }
        return false;
    }

    /**
     * Checks whether the tree base still rests on soil or another log, which blocks toppling when the
     * entire base is required to be broken.
     *
     * @param treeDefinitions    The candidate tree definitions
     * @param detectedTreeBlocks The detected tree blocks
     * @param treeDefinition     The resolved tree definition
     * @param initialBlockY      The Y coordinate of the initial block
     * @return True if the base is blocked
     */
    private boolean isTreeBaseBlocked(Set<TreeDefinition> treeDefinitions,
                                      TreeBlockSet<Block> detectedTreeBlocks,
                                      TreeDefinition treeDefinition,
                                      int initialBlockY) {
        Set<Block> groundBlocks = new HashSet<>();
        for (TreeBlock<Block> treeBlock : detectedTreeBlocks.getLogBlocks()) {
            if (treeBlock != detectedTreeBlocks.getInitialLogBlock() && treeBlock.getLocation().getBlockY() == initialBlockY) {
                groundBlocks.add(treeBlock.block());
            }
        }

        Set<Material> plantableSoil = this.treeDefinitionManager.getPlantableSoilMaterials(treeDefinition);
        for (Block block : groundBlocks) {
            Block blockBelow = getRelativeInCurrentRegion(block, 0, -1, 0);
            if (isLogBlock(treeDefinitions, blockBelow) || plantableSoil.contains(blockBelow.getType())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull Set<TreeDefinition> getTreeDefinitionsForLog(@NotNull Block block) {
        return this.treeDefinitionManager.getTreeDefinitionsForLog(block);
    }

    @Override
    public @NotNull Set<TreeDefinition> narrowTreeDefinition(@NotNull Set<TreeDefinition> possibleTreeDefinitions,
                                                             @NotNull Block block,
                                                             @NotNull TreeBlockType treeBlockType) {
        return this.treeDefinitionManager.narrowTreeDefinition(possibleTreeDefinitions, block, treeBlockType);
    }

    @Override
    public @NotNull Set<TreeDefinition> narrowTreeDefinition(@NotNull Set<TreeDefinition> possibleTreeDefinitions,
                                                             @Nullable Material material,
                                                             @NotNull TreeBlockType treeBlockType) {
        return this.treeDefinitionManager.narrowTreeDefinition(possibleTreeDefinitions, material, treeBlockType);
    }

    /**
     * Holds the detected log geometry once the branch phase finished, so leaf scanning runs on
     * primitive arrays and packed position keys instead of block locations.
     */
    private static final class LeafSearchContext {
        private final TreeBlockSet<Block> treeBlocks;
        private final int[] logXs;
        private final int[] logYs;
        private final int[] logZs;
        private final LongSet detectedLogPositions = new LongSet();
        private final LongSet visitedLocations = new LongSet();
        private final boolean detectLeavesDiagonally;
        private final int leafLimit;
        private int leafCount;

        private LeafSearchContext(TreeBlockSet<Block> treeBlocks, boolean detectLeavesDiagonally, int leafLimit) {
            this.treeBlocks = treeBlocks;
            this.detectLeavesDiagonally = detectLeavesDiagonally;
            this.leafLimit = leafLimit;

            List<TreeBlock<Block>> logBlocks = treeBlocks.getLogBlocks();
            int logCount = logBlocks.size();
            this.logXs = new int[logCount];
            this.logYs = new int[logCount];
            this.logZs = new int[logCount];
            for (int index = 0; index < logCount; index++) {
                Block block = logBlocks.get(index).block();
                this.logXs[index] = block.getX();
                this.logYs[index] = block.getY();
                this.logZs[index] = block.getZ();
                this.detectedLogPositions.add(TreeGeometry.packBlockKey(block));
            }
        }

        private boolean canAddLeaf() {
            return this.leafCount < this.leafLimit;
        }
    }

    private static final class CrossRegionBlockAccessException extends RuntimeException {
    }
}
