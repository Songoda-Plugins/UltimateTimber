package com.songoda.ultimatetimber.manager;

import com.songoda.core.vortexcore.compatibility.folia.SchedulerUtils;
import com.songoda.ultimatetimber.api.manager.PlacedBlockManager;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockSet;
import com.songoda.ultimatetimber.api.tree.TreeBlockType;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import com.songoda.ultimatetimber.tree.DetectedTreeImpl;
import com.songoda.ultimatetimber.tree.TreeBlockImpl;
import com.songoda.ultimatetimber.utils.LongSet;
import com.songoda.ultimatetimber.utils.RegionBatchProcessor;
import com.songoda.ultimatetimber.utils.TreeGeometry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Region-scheduled tree scan used when a tree may cross Folia region boundaries.
 */
final class RegionAwareTreeDetector {

    private static final int LEAF_LIMIT_RATIO = 8;

    private final Plugin plugin;
    private final World world;
    private final Block initialBlock;
    private final Position initialPosition;
    private final int initialY;
    private final int minHeight;
    private final int maxHeight;
    private final Set<TreeDefinition> treeDefinitions;
    private final TreeDefinitionManager treeDefinitionManager;
    private final PlacedBlockManager placedBlockManager;
    private final Consumer<DetectedTree> callback;
    private final Map<Long, BlockSample> samples = new HashMap<>();
    private final TreeBlockSet<Block> detectedTreeBlocks;
    private final List<Position> trunkBlocks = new ArrayList<>();
    private final List<Position> detectedLogPositions = new ArrayList<>();
    private final LongSet visitedBranchLocations = new LongSet();
    private final List<Position> branchFrontier = new ArrayList<>();
    private final List<Position> leafFrontier = new ArrayList<>();
    private final LongSet visitedLeafLocations = new LongSet();
    private final LongSet detectedLogLocations = new LongSet();
    private final int leavesRequiredForTree;
    private final boolean onlyDetectLogsUpwards;
    private final boolean breakEntireTreeBase;
    private final boolean destroyLeaves;
    private final int maxLogsPerChop;
    private final int maxLeavesPerTree;
    private boolean detectLeavesDiagonally;
    private int leafCount;
    private boolean completed;

    RegionAwareTreeDetector(@NotNull Plugin plugin,
                            @NotNull Block initialBlock,
                            @NotNull Location initialLocation,
                            @NotNull Material initialMaterial,
                            @NotNull Set<TreeDefinition> treeDefinitions,
                            @NotNull TreeDefinitionManager treeDefinitionManager,
                            @NotNull PlacedBlockManager placedBlockManager,
                            int leavesRequiredForTree,
                            boolean onlyDetectLogsUpwards,
                            boolean breakEntireTreeBase,
                            boolean destroyLeaves,
                            int maxLogsPerChop,
                            @NotNull Consumer<DetectedTree> callback) {
        this.plugin = plugin;
        this.world = initialLocation.getWorld();
        this.initialBlock = initialBlock;
        this.initialPosition = new Position(initialLocation.getBlockX(), initialLocation.getBlockY(), initialLocation.getBlockZ());
        this.initialY = this.initialPosition.y();
        this.minHeight = this.world.getMinHeight();
        this.maxHeight = this.world.getMaxHeight();
        this.treeDefinitions = new HashSet<>(treeDefinitions);
        this.treeDefinitionManager = treeDefinitionManager;
        this.placedBlockManager = placedBlockManager;
        this.callback = callback;
        this.leavesRequiredForTree = leavesRequiredForTree;
        this.onlyDetectLogsUpwards = onlyDetectLogsUpwards;
        this.breakEntireTreeBase = breakEntireTreeBase;
        this.destroyLeaves = destroyLeaves;
        this.maxLogsPerChop = Math.max(1, maxLogsPerChop);
        this.maxLeavesPerTree = (int) Math.min(Integer.MAX_VALUE, (long) this.maxLogsPerChop * LEAF_LIMIT_RATIO);

        BlockSample initialSample = new BlockSample(initialBlock, initialMaterial, false);
        this.samples.put(this.initialPosition.key(), initialSample);
        TreeBlock<Block> initialTreeBlock = new TreeBlockImpl(initialBlock, TreeBlockType.LOG, initialLocation);
        this.detectedTreeBlocks = new TreeBlockSet<>(initialTreeBlock);
        this.trunkBlocks.add(this.initialPosition);
        this.detectedLogPositions.add(this.initialPosition);
        this.detectedLogLocations.add(this.initialPosition.key());
    }

    void start() {
        SchedulerUtils.runLocationTask(this.plugin, this.initialLocation(), () -> {
            if (!this.initialBlock.getType().isAir()) {
                this.finish(null);
                return;
            }

            this.collectTrunkBlocks();
        });
    }

    private void collectTrunkBlocks() {
        List<Position> candidates = new ArrayList<>((this.maxLogsPerChop - 1) * (this.onlyDetectLogsUpwards ? 1 : 2));
        for (int distance = 1; distance < this.maxLogsPerChop; distance++) {
            candidates.add(this.initialPosition.offset(0, distance, 0));
        }
        if (!this.onlyDetectLogsUpwards) {
            for (int distance = 1; distance < this.maxLogsPerChop; distance++) {
                candidates.add(this.initialPosition.offset(0, -distance, 0));
            }
        }

        this.loadSamples(candidates, this::finishTrunkScan);
    }

    private void finishTrunkScan() {
        List<Position> upward = new ArrayList<>();
        List<Position> downward = new ArrayList<>();
        for (int distance = 1; distance < this.maxLogsPerChop; distance++) {
            upward.add(this.initialPosition.offset(0, distance, 0));
            if (!this.onlyDetectLogsUpwards) {
                downward.add(this.initialPosition.offset(0, -distance, 0));
            }
        }

        for (Position position : upward) {
            if (!this.hasLogBudget() || this.treeDefinitions.isEmpty()) {
                break;
            }

            BlockSample sample = this.sample(position);
            if (!this.isLogBlock(sample)) {
                break;
            }

            this.addTrunkBlock(position, sample);
        }

        if (!this.onlyDetectLogsUpwards && this.hasLogBudget() && !this.treeDefinitions.isEmpty()) {
            for (Position position : downward) {
                if (!this.hasLogBudget() || this.treeDefinitions.isEmpty()) {
                    break;
                }

                BlockSample sample = this.sample(position);
                if (!this.isLogBlock(sample)) {
                    break;
                }

                this.addTrunkBlock(position, sample);
            }
        }

        this.trunkBlocks.sort((left, right) -> Integer.compare(left.y(), right.y()));
        this.startBranchSearch();
    }

    private void addTrunkBlock(Position position, BlockSample sample) {
        this.trunkBlocks.add(position);
        this.detectedLogPositions.add(position);
        this.detectedLogLocations.add(position.key());
        this.detectedTreeBlocks.add(this.createTreeBlock(position, sample, TreeBlockType.LOG));
        this.narrowDefinitions(sample.material(), TreeBlockType.LOG);
    }

    private void startBranchSearch() {
        this.branchFrontier.addAll(this.trunkBlocks);
        this.advanceBranchSearch();
    }

    private void advanceBranchSearch() {
        if (this.branchFrontier.isEmpty() || !this.hasLogBudget() || this.treeDefinitions.isEmpty()) {
            this.startLeafSearch();
            return;
        }

        int[][] offsets = this.onlyDetectLogsUpwards ? TreeGeometry.BRANCH_OFFSETS : TreeGeometry.TRUNK_OFFSETS;
        Set<Position> samplesToLoad = new LinkedHashSet<>();
        for (Position position : this.branchFrontier) {
            samplesToLoad.addAll(this.offsetPositions(position, offsets));
        }

        if (this.ensureSamples(samplesToLoad, this::processBranchFrontier)) {
            this.processBranchFrontier();
        }
    }

    private void processBranchFrontier() {
        int[][] offsets = this.onlyDetectLogsUpwards ? TreeGeometry.BRANCH_OFFSETS : TreeGeometry.TRUNK_OFFSETS;
        List<Position> nextFrontier = new ArrayList<>();
        LongSet nextFrontierLocations = new LongSet();
        boolean logLimitReached = false;

        for (Position position : this.branchFrontier) {
            for (int[] offset : offsets) {
                Position target = position.offset(offset[0], offset[1], offset[2]);
                if (!this.visitedBranchLocations.add(target.key())) {
                    continue;
                }

                BlockSample sample = this.sample(target);
                if (!this.isBranchLog(sample, target) || !this.hasLogBudget()) {
                    continue;
                }

                if (this.detectedTreeBlocks.add(this.createTreeBlock(target, sample, TreeBlockType.LOG))) {
                    this.detectedLogPositions.add(target);
                    this.detectedLogLocations.add(target.key());
                }
                this.narrowDefinitions(sample.material(), TreeBlockType.LOG);
                if ((!this.onlyDetectLogsUpwards || target.y() > this.initialY)
                        && nextFrontierLocations.add(target.key())) {
                    nextFrontier.add(target);
                }

                if (!this.hasLogBudget()) {
                    logLimitReached = true;
                    break;
                }
            }

            if (logLimitReached) {
                break;
            }
        }

        this.branchFrontier.clear();
        this.branchFrontier.addAll(nextFrontier);
        this.advanceBranchSearch();
    }

    private void startLeafSearch() {
        if (this.treeDefinitions.isEmpty()) {
            this.finish(null);
            return;
        }

        this.detectLeavesDiagonally = this.detectLeavesDiagonally();
        this.leafFrontier.addAll(this.detectedLogPositions);
        this.advanceLeafSearch();
    }

    private void advanceLeafSearch() {
        if (this.leafCount >= this.maxLeavesPerTree || this.leafFrontier.isEmpty()) {
            this.finishLeafScan();
            return;
        }

        int[][] offsets = this.detectLeavesDiagonally ? TreeGeometry.TRUNK_OFFSETS : TreeGeometry.LEAF_OFFSETS;
        Set<Position> samplesToLoad = new LinkedHashSet<>();
        for (Position position : this.leafFrontier) {
            for (int[] offset : offsets) {
                Position neighbor = position.offset(offset[0], offset[1], offset[2]);
                samplesToLoad.add(neighbor);
                samplesToLoad.addAll(this.offsetPositions(neighbor, TreeGeometry.TRUNK_OFFSETS));
            }
        }

        if (this.ensureSamples(samplesToLoad, this::processLeafFrontier)) {
            this.processLeafFrontier();
        }
    }

    private void processLeafFrontier() {
        int[][] offsets = this.detectLeavesDiagonally ? TreeGeometry.TRUNK_OFFSETS : TreeGeometry.LEAF_OFFSETS;
        List<Position> nextFrontier = new ArrayList<>();
        LongSet nextFrontierLocations = new LongSet();

        for (Position position : this.leafFrontier) {
            for (int[] offset : offsets) {
                Position target = position.offset(offset[0], offset[1], offset[2]);
                if (!this.visitedLeafLocations.add(target.key())) {
                    continue;
                }

                BlockSample sample = this.sample(target);
                if (!this.isValidLeaf(sample, target) || this.doesLeafBorderInvalidLog(target)) {
                    continue;
                }

                if (this.leafCount >= this.maxLeavesPerTree) {
                    this.finishLeafScan();
                    return;
                }

                this.detectedTreeBlocks.add(this.createTreeBlock(target, sample, TreeBlockType.LEAF));
                this.leafCount++;
                this.narrowDefinitions(sample.material(), TreeBlockType.LEAF);
                if (nextFrontierLocations.add(target.key())) {
                    nextFrontier.add(target);
                }
            }
        }

        this.leafFrontier.clear();
        this.leafFrontier.addAll(nextFrontier);
        this.advanceLeafSearch();
    }

    private void finishLeafScan() {
        if (this.treeDefinitions.isEmpty() || this.leafCount < this.leavesRequiredForTree) {
            this.finish(null);
            return;
        }

        TreeDefinition treeDefinition = this.treeDefinitions.iterator().next();
        if (!this.destroyLeaves) {
            this.detectedTreeBlocks.removeAll(TreeBlockType.LEAF);
        }

        if (!this.breakEntireTreeBase) {
            this.finish(new DetectedTreeImpl(treeDefinition, this.detectedTreeBlocks));
            return;
        }

        List<Position> groundLogs = new ArrayList<>();
        for (Position position : this.detectedLogPositions) {
            if (!position.equals(this.initialPosition) && position.y() == this.initialY) {
                groundLogs.add(position.offset(0, -1, 0));
            }
        }

        this.loadSamples(groundLogs, () -> {
            Set<Material> plantableSoil = this.treeDefinitionManager.getPlantableSoilMaterials(treeDefinition);
            for (Position position : groundLogs) {
                BlockSample sample = this.sample(position);
                if (this.isLogBlock(sample) || plantableSoil.contains(sample.material())) {
                    this.finish(null);
                    return;
                }
            }

            this.finish(new DetectedTreeImpl(treeDefinition, this.detectedTreeBlocks));
        });
    }

    private boolean isValidLeaf(BlockSample sample, Position position) {
        if (sample.block() == null || !this.isLeafMaterial(sample.material()) || sample.placed()) {
            return false;
        }

        return this.isLogWithinDistance(position, this.maxLeafDistanceFromLog());
    }

    private boolean doesLeafBorderInvalidLog(Position position) {
        for (int[] offset : TreeGeometry.TRUNK_OFFSETS) {
            Position neighbor = position.offset(offset[0], offset[1], offset[2]);
            if (this.isLogBlock(this.sample(neighbor)) && !this.detectedLogLocations.contains(neighbor.key())) {
                return true;
            }
        }
        return false;
    }

    private boolean isBranchLog(BlockSample sample, Position position) {
        if (!this.isLogBlock(sample)) {
            return false;
        }

        double maxDistanceSquared = this.maxLogDistanceSquared();
        int axisLimit = TreeGeometry.axisLimit(maxDistanceSquared);
        for (Position trunkBlock : this.trunkBlocks) {
            int dx = position.x() - trunkBlock.x();
            if (dx > axisLimit || dx < -axisLimit) {
                continue;
            }
            int dy = position.y() - trunkBlock.y();
            if (dy > axisLimit || dy < -axisLimit) {
                continue;
            }
            int dz = position.z() - trunkBlock.z();
            if (dz > axisLimit || dz < -axisLimit) {
                continue;
            }
            if (TreeGeometry.isWithinSquaredDistance(dx, dy, dz, maxDistanceSquared)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLogWithinDistance(Position position, int radius) {
        long maxDistanceSquared = (long) radius * radius;
        for (Position logPosition : this.detectedLogPositions) {
            int dx = position.x() - logPosition.x();
            if (dx > radius || dx < -radius) {
                continue;
            }
            int dy = position.y() - logPosition.y();
            if (dy > radius || dy < -radius) {
                continue;
            }
            int dz = position.z() - logPosition.z();
            if (dz > radius || dz < -radius) {
                continue;
            }
            if (TreeGeometry.isWithinSquaredDistance(dx, dy, dz, maxDistanceSquared)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLogBlock(BlockSample sample) {
        return sample.block() != null && !sample.placed() && this.isLogMaterial(sample.material());
    }

    private boolean isLogMaterial(Material material) {
        for (TreeDefinition treeDefinition : this.treeDefinitions) {
            if (treeDefinition.getLogMaterials().contains(material)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLeafMaterial(Material material) {
        for (TreeDefinition treeDefinition : this.treeDefinitions) {
            if (treeDefinition.getLeafMaterials().contains(material)) {
                return true;
            }
        }
        return false;
    }

    private boolean detectLeavesDiagonally() {
        return this.treeDefinitions.stream().anyMatch(TreeDefinition::shouldDetectLeavesDiagonally);
    }

    private int maxLeafDistanceFromLog() {
        int maxDistance = 0;
        for (TreeDefinition treeDefinition : this.treeDefinitions) {
            maxDistance = Math.max(maxDistance, treeDefinition.getMaxLeafDistanceFromLog());
        }
        return maxDistance;
    }

    private double maxLogDistanceSquared() {
        double maxDistanceSquared = 0.0;
        for (TreeDefinition treeDefinition : this.treeDefinitions) {
            double distance = treeDefinition.getMaxLogDistanceFromTrunk();
            double distanceSquared = distance * distance;
            if (!this.onlyDetectLogsUpwards) {
                distanceSquared *= 1.5;
            }
            maxDistanceSquared = Math.max(maxDistanceSquared, distanceSquared);
        }
        return maxDistanceSquared;
    }

    private void narrowDefinitions(Material material, TreeBlockType type) {
        this.treeDefinitions.removeIf(treeDefinition -> type == TreeBlockType.LOG
                ? !treeDefinition.getLogMaterials().contains(material)
                : !treeDefinition.getLeafMaterials().contains(material));
    }

    private List<Position> offsetPositions(Position position, int[][] offsets) {
        List<Position> positions = new ArrayList<>(offsets.length);
        for (int[] offset : offsets) {
            positions.add(position.offset(offset[0], offset[1], offset[2]));
        }
        return positions;
    }

    private boolean ensureSamples(Collection<Position> positions, Runnable whenReady) {
        List<Position> missing = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (Position position : positions) {
            if (!this.samples.containsKey(position.key()) && seen.add(position.key())) {
                missing.add(position);
            }
        }

        if (missing.isEmpty()) {
            return true;
        }

        this.loadSamples(missing, whenReady);
        return false;
    }

    private void loadSamples(Collection<Position> positions, Runnable whenReady) {
        List<Position> missing = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (Position position : positions) {
            if (!this.samples.containsKey(position.key()) && seen.add(position.key())) {
                missing.add(position);
            }
        }

        if (missing.isEmpty()) {
            whenReady.run();
            return;
        }

        RegionBatchProcessor.processByRegion(
                this.plugin,
                missing,
                position -> position.toLocation(this.world),
                this::samplePosition,
                whenReady
        );
    }

    private void samplePosition(Position position) {
        if (position.y() < this.minHeight || position.y() >= this.maxHeight
                || !this.world.isChunkLoaded(position.x() >> 4, position.z() >> 4)) {
            this.samples.put(position.key(), BlockSample.air());
            return;
        }

        try {
            Block block = this.world.getBlockAt(position.x(), position.y(), position.z());
            Material material = block.getType();
            boolean placed = this.placedBlockManager.isBlockPlaced(block);
            this.samples.put(position.key(), new BlockSample(block, material, placed));
        } catch (RuntimeException ignored) {
            this.samples.put(position.key(), BlockSample.air());
        }
    }

    private BlockSample sample(Position position) {
        return this.samples.getOrDefault(position.key(), BlockSample.air());
    }

    private TreeBlock<Block> createTreeBlock(Position position, BlockSample sample, TreeBlockType type) {
        return new TreeBlockImpl(sample.block(), type, position.toLocation(this.world));
    }

    private boolean hasLogBudget() {
        return this.detectedTreeBlocks.getLogBlocks().size() < this.maxLogsPerChop;
    }

    private Location initialLocation() {
        return this.initialPosition.toLocation(this.world);
    }

    private void finish(DetectedTree detectedTree) {
        if (this.completed) {
            return;
        }
        this.completed = true;
        this.callback.accept(detectedTree);
    }

    private record Position(int x, int y, int z) {

        private Position offset(int offsetX, int offsetY, int offsetZ) {
            return new Position(this.x + offsetX, this.y + offsetY, this.z + offsetZ);
        }

        private long key() {
            return TreeGeometry.packBlockKey(this.x, this.y, this.z);
        }

        private Location toLocation(World world) {
            return new Location(world, this.x, this.y, this.z);
        }
    }

    private record BlockSample(Block block, Material material, boolean placed) {

        private static BlockSample air() {
            return new BlockSample(null, Material.AIR, false);
        }
    }

}
