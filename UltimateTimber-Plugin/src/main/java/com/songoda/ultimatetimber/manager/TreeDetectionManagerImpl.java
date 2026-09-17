package com.songoda.ultimatetimber.manager;

import com.songoda.core.vortexcore.hooks.internal.ReloadHook;
import com.songoda.core.vortexcore.vinject.annotation.RegisterReloadHook;
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
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.annotation.component.Component;
import net.vortexdevelopment.vinject.annotation.lifecycle.PostConstruct;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service scanning world blocks to detect and validate trees.
 */
@Component
@RegisterReloadHook
public class TreeDetectionManagerImpl implements TreeDetectionManager, ReloadHook {

    @Inject
    private TimberConfig config;

    @Inject
    private TreeDefinitionManager treeDefinitionManager;

    @Inject
    private PlacedBlockManager placedBlockManager;

    private final Set<Vector> validTrunkOffsets = new HashSet<>();
    private final Set<Vector> validBranchOffsets = new HashSet<>();
    private final Set<Vector> validLeafOffsets = new HashSet<>();

    private int leavesRequiredForTree = 5;
    private boolean onlyDetectLogsUpwards = true;
    private boolean breakEntireTreeBase = false;
    private boolean destroyLeaves = true;

    public TreeDetectionManagerImpl() {
        for (int y = 0; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    this.validBranchOffsets.add(new Vector(x, y, z));
                }
            }
        }

        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    this.validTrunkOffsets.add(new Vector(x, y, z));
                }
            }
        }

        for (int i = -1; i <= 1; i += 2) {
            this.validLeafOffsets.add(new Vector(i, 0, 0));
            this.validLeafOffsets.add(new Vector(0, i, 0));
            this.validLeafOffsets.add(new Vector(0, 0, i));
        }
    }

    @PostConstruct
    public void initialize() {
        onReload();
    }

    @Override
    public void onReload() {
        if (this.config != null) {
            this.leavesRequiredForTree = this.config.getLeavesRequiredForTree();
            this.onlyDetectLogsUpwards = this.config.isOnlyDetectLogsUpwards();
            this.breakEntireTreeBase = this.config.isBreakEntireTreeBase();
            this.destroyLeaves = this.config.isDestroyLeaves();
        }
    }

    @Override
    public @Nullable DetectedTree detectTree(@NotNull Block initialBlock) {
        if (this.placedBlockManager.isBlockPlaced(initialBlock)) {
            return null;
        }

        Set<TreeDefinition> possibleTreeDefinitions = new HashSet<>(this.treeDefinitionManager.getTreeDefinitionsForLog(initialBlock));
        if (possibleTreeDefinitions.isEmpty()) {
            return null;
        }

        TreeBlock<Block> initialTreeBlock = new TreeBlockImpl(initialBlock, TreeBlockType.LOG);
        TreeBlockSet<Block> detectedTreeBlocks = new TreeBlockSet<>(initialTreeBlock);

        List<Block> trunkBlocks = new ArrayList<>();
        trunkBlocks.add(initialBlock);

        Block targetBlock = initialBlock;
        while (isValidLogType(possibleTreeDefinitions, null, (targetBlock = targetBlock.getRelative(BlockFace.UP)))) {
            trunkBlocks.add(targetBlock);
            possibleTreeDefinitions.retainAll(this.treeDefinitionManager.narrowTreeDefinition(possibleTreeDefinitions, targetBlock.getType(), TreeBlockType.LOG));
        }

        if (!this.onlyDetectLogsUpwards) {
            targetBlock = initialBlock;
            while (isValidLogType(possibleTreeDefinitions, null, (targetBlock = targetBlock.getRelative(BlockFace.DOWN)))) {
                trunkBlocks.add(targetBlock);
                possibleTreeDefinitions.retainAll(this.treeDefinitionManager.narrowTreeDefinition(possibleTreeDefinitions, targetBlock.getType(), TreeBlockType.LOG));
            }
        }

        Collections.reverse(trunkBlocks);

        Set<Location> visitedBranchLocations = new HashSet<>();
        for (Block trunkBlock : trunkBlocks) {
            recursiveBranchSearch(possibleTreeDefinitions, trunkBlocks, detectedTreeBlocks, trunkBlock, initialBlock.getY(), visitedBranchLocations);
        }

        boolean detectLeavesDiagonally = possibleTreeDefinitions.stream().anyMatch(TreeDefinition::shouldDetectLeavesDiagonally);

        Set<TreeBlock<Block>> branchBlocks = new HashSet<>(detectedTreeBlocks.getLogBlocks());
        Set<Location> visitedLeafLocations = new HashSet<>();
        for (TreeBlock<Block> branchBlock : branchBlocks) {
            recursiveLeafSearch(possibleTreeDefinitions, detectedTreeBlocks, branchBlock.getBlock(), visitedLeafLocations, detectLeavesDiagonally);
        }

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

        if (this.breakEntireTreeBase) {
            Set<Block> groundBlocks = new HashSet<>();
            for (TreeBlock<Block> treeBlock : detectedTreeBlocks.getLogBlocks()) {
                if (treeBlock != detectedTreeBlocks.getInitialLogBlock() && treeBlock.getLocation().getBlockY() == initialBlock.getY()) {
                    groundBlocks.add(treeBlock.getBlock());
                }
            }

            for (Block block : groundBlocks) {
                Block blockBelow = block.getRelative(BlockFace.DOWN);
                boolean blockBelowIsLog = isValidLogType(possibleTreeDefinitions, null, blockBelow);
                boolean blockBelowIsSoil = this.treeDefinitionManager.getPlantableSoilMaterials(actualTreeDefinition).contains(blockBelow.getType());

                if (blockBelowIsLog || blockBelowIsSoil) {
                    return null;
                }
            }
        }

        return new DetectedTreeImpl(actualTreeDefinition, detectedTreeBlocks);
    }

    private void recursiveBranchSearch(Set<TreeDefinition> treeDefinitions,
                                       List<Block> trunkBlocks,
                                       TreeBlockSet<Block> treeBlocks,
                                       Block block,
                                       int startingBlockY,
                                       Set<Location> visitedBranchLocations) {
        Set<Vector> offsets = this.onlyDetectLogsUpwards ? this.validBranchOffsets : this.validTrunkOffsets;
        for (Vector offset : offsets) {
            Block targetBlock = block.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
            Location targetLocation = targetBlock.getLocation();
            if (visitedBranchLocations.contains(targetLocation)) {
                continue;
            }
            visitedBranchLocations.add(targetLocation);

            if (isValidLogType(treeDefinitions, trunkBlocks, targetBlock)) {
                TreeBlock<Block> treeBlock = new TreeBlockImpl(targetBlock, TreeBlockType.LOG);
                treeBlocks.add(treeBlock);
                treeDefinitions.retainAll(this.treeDefinitionManager.narrowTreeDefinition(treeDefinitions, targetBlock.getType(), TreeBlockType.LOG));
                if (!this.onlyDetectLogsUpwards || targetBlock.getY() > startingBlockY) {
                    recursiveBranchSearch(treeDefinitions, trunkBlocks, treeBlocks, targetBlock, startingBlockY, visitedBranchLocations);
                }
            }
        }
    }

    private void recursiveLeafSearch(Set<TreeDefinition> treeDefinitions,
                                     TreeBlockSet<Block> treeBlocks,
                                     Block block,
                                     Set<Location> visitedLeafLocations,
                                     boolean detectLeavesDiagonally) {
        Set<Vector> offsets = !detectLeavesDiagonally ? this.validLeafOffsets : this.validTrunkOffsets;
        for (Vector offset : offsets) {
            Block targetBlock = block.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
            Location targetLocation = targetBlock.getLocation();
            if (visitedLeafLocations.contains(targetLocation)) {
                continue;
            }
            visitedLeafLocations.add(targetLocation);

            Material material = targetBlock.getType();
            if (isValidLeafType(treeDefinitions, treeBlocks, targetBlock, material) && !doesLeafBorderInvalidLog(treeDefinitions, treeBlocks, targetBlock)) {
                TreeBlock<Block> treeBlock = new TreeBlockImpl(targetBlock, TreeBlockType.LEAF);
                treeBlocks.add(treeBlock);
                treeDefinitions.retainAll(this.treeDefinitionManager.narrowTreeDefinition(treeDefinitions, material, TreeBlockType.LEAF));
                recursiveLeafSearch(treeDefinitions, treeBlocks, targetBlock, visitedLeafLocations, detectLeavesDiagonally);
            }
        }
    }

    private boolean doesLeafBorderInvalidLog(Set<TreeDefinition> treeDefinitions, TreeBlockSet<Block> treeBlocks, Block block) {
        for (Vector offset : this.validTrunkOffsets) {
            Block targetBlock = block.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
            if (isValidLogType(treeDefinitions, null, targetBlock) && !treeBlocks.contains(new TreeBlockImpl(targetBlock, TreeBlockType.LOG))) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidLogType(Set<TreeDefinition> treeDefinitions, List<Block> trunkBlocks, Block block) {
        if (this.placedBlockManager.isBlockPlaced(block)) {
            return false;
        }

        Material material = block.getType();
        boolean matches = false;
        for (TreeDefinition definition : treeDefinitions) {
            if (definition.getLogMaterials().contains(material)) {
                matches = true;
                break;
            }
        }

        if (!matches) {
            return false;
        }

        if (trunkBlocks == null || trunkBlocks.isEmpty()) {
            return true;
        }

        Location location = block.getLocation();
        for (TreeDefinition definition : treeDefinitions) {
            double maxDistance = definition.getMaxLogDistanceFromTrunk() * definition.getMaxLogDistanceFromTrunk();
            if (!this.onlyDetectLogsUpwards) {
                maxDistance *= 1.5;
            }
            for (Block trunkBlock : trunkBlocks) {
                if (location.distanceSquared(trunkBlock.getLocation()) < maxDistance) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isValidLeafType(Set<TreeDefinition> treeDefinitions, TreeBlockSet<Block> treeBlocks, Block block, Material material) {
        if (this.placedBlockManager.isBlockPlaced(block)) {
            return false;
        }

        boolean matches = false;
        for (TreeDefinition definition : treeDefinitions) {
            if (definition.getLeafMaterials().contains(material)) {
                matches = true;
                break;
            }
        }

        if (!matches) {
            return false;
        }

        if (treeBlocks == null || treeBlocks.isEmpty()) {
            return true;
        }

        int maxDistanceFromLog = 0;
        for (TreeDefinition definition : treeDefinitions) {
            int dist = definition.getMaxLeafDistanceFromLog();
            if (dist > maxDistanceFromLog) {
                maxDistanceFromLog = dist;
            }
        }

        double maxDistanceSquared = (double) maxDistanceFromLog * maxDistanceFromLog;
        Location blockLocation = block.getLocation();
        for (TreeBlock<Block> logBlock : treeBlocks.getLogBlocks()) {
            if (logBlock.getLocation().distanceSquared(blockLocation) < maxDistanceSquared) {
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
}
