package com.songoda.ultimatetimber.test;

import com.songoda.ultimatetimber.api.manager.PlacedBlockManager;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.tree.TreeBlockType;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import com.songoda.ultimatetimber.utils.TreeGeometry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mirror of the detection algorithm as it behaved before the performance rework, kept as a test oracle.
 *
 * <p>It keeps the original strict radius comparison, has no log or leaf cap, and performs the same
 * {@code Block#getLocation} calls the original implementation made, so counting calls through
 * {@link FakeBlockWorld} compares the two implementations fairly. The entire base check is left out
 * because the tests that need it assert the expected result directly.
 */
public final class LegacyTreeDetector {

    private final TreeDefinitionManager definitionManager;
    private final PlacedBlockManager placedBlockManager;
    private final int leavesRequiredForTree;
    private final boolean onlyDetectLogsUpwards;
    private final boolean destroyLeaves;
    public LegacyTreeDetector(TreeDefinitionManager definitionManager, PlacedBlockManager placedBlockManager, int leavesRequiredForTree, boolean onlyDetectLogsUpwards, boolean destroyLeaves) {
        this.definitionManager = definitionManager;
        this.placedBlockManager = placedBlockManager;
        this.leavesRequiredForTree = leavesRequiredForTree;
        this.onlyDetectLogsUpwards = onlyDetectLogsUpwards;
        this.destroyLeaves = destroyLeaves;
    }

    /**
     * Mirrors {@code Location#distanceSquared} for block coordinates, which the original implementation
     * used while allocating a location per comparison.
     */
    private static double squaredDistance(Location first, Location second) {
        double dx = first.getBlockX() - second.getBlockX();
        double dy = first.getBlockY() - second.getBlockY();
        double dz = first.getBlockZ() - second.getBlockZ();
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Detects a tree starting from the given block.
     *
     * @param initialBlock The starting block
     * @return The legacy detection result
     */
    public Result detect(Block initialBlock) {
        Map<Long, Block> logBlocks = new LinkedHashMap<>();
        Map<Long, Block> leafBlocks = new LinkedHashMap<>();

        Set<TreeDefinition> possibleTreeDefinitions = new HashSet<>(this.definitionManager.getTreeDefinitionsForLog(initialBlock));
        if (possibleTreeDefinitions.isEmpty()) {
            return new Result(false, Set.of(), Set.of());
        }

        logBlocks.put(TreeGeometry.packBlockKey(initialBlock), initialBlock);

        List<Block> trunkBlocks = new ArrayList<>();
        trunkBlocks.add(initialBlock);

        Block targetBlock = initialBlock;
        while (isValidLogType(possibleTreeDefinitions, null, targetBlock = targetBlock.getRelative(BlockFace.UP))) {
            trunkBlocks.add(targetBlock);
            possibleTreeDefinitions.retainAll(this.definitionManager.narrowTreeDefinition(possibleTreeDefinitions, targetBlock.getType(), TreeBlockType.LOG));
        }

        if (!this.onlyDetectLogsUpwards) {
            targetBlock = initialBlock;
            while (isValidLogType(possibleTreeDefinitions, null, targetBlock = targetBlock.getRelative(BlockFace.DOWN))) {
                trunkBlocks.add(targetBlock);
                possibleTreeDefinitions.retainAll(this.definitionManager.narrowTreeDefinition(possibleTreeDefinitions, targetBlock.getType(), TreeBlockType.LOG));
            }
        }

        Collections.reverse(trunkBlocks);

        Set<Long> visitedBranchLocations = new HashSet<>();
        for (Block trunkBlock : trunkBlocks) {
            recursiveBranchSearch(possibleTreeDefinitions, trunkBlocks, logBlocks, trunkBlock, initialBlock.getY(), visitedBranchLocations);
        }

        boolean detectLeavesDiagonally = possibleTreeDefinitions.stream().anyMatch(TreeDefinition::shouldDetectLeavesDiagonally);
        Set<Long> branchBlocks = new LinkedHashSet<>(logBlocks.keySet());
        Set<Long> visitedLeafLocations = new HashSet<>();
        for (long branchKey : branchBlocks) {
            recursiveLeafSearch(possibleTreeDefinitions, logBlocks, leafBlocks, logBlocks.get(branchKey), visitedLeafLocations, detectLeavesDiagonally);
        }

        if (possibleTreeDefinitions.isEmpty() || leafBlocks.size() < this.leavesRequiredForTree) {
            return new Result(false, Set.of(), Set.of());
        }

        if (!this.destroyLeaves) {
            leafBlocks.clear();
        }

        return new Result(true, new LinkedHashSet<>(logBlocks.keySet()), new LinkedHashSet<>(leafBlocks.keySet()));
    }

    private void recursiveBranchSearch(Set<TreeDefinition> treeDefinitions, List<Block> trunkBlocks, Map<Long, Block> logBlocks,
                                       Block block, int startingBlockY, Set<Long> visitedBranchLocations) {
        for (int[] offset : this.onlyDetectLogsUpwards ? TreeGeometry.BRANCH_OFFSETS : TreeGeometry.TRUNK_OFFSETS) {
            Block targetBlock = block.getRelative(offset[0], offset[1], offset[2]);
            long targetKey = TreeGeometry.packBlockKey(targetBlock);
            if (visitedBranchLocations.contains(targetKey)) {
                continue;
            }
            visitedBranchLocations.add(targetKey);

            if (isValidLogType(treeDefinitions, trunkBlocks, targetBlock)) {
                logBlocks.put(targetKey, targetBlock);
                treeDefinitions.retainAll(this.definitionManager.narrowTreeDefinition(treeDefinitions, targetBlock.getType(), TreeBlockType.LOG));
                if (!this.onlyDetectLogsUpwards || targetBlock.getY() > startingBlockY) {
                    recursiveBranchSearch(treeDefinitions, trunkBlocks, logBlocks, targetBlock, startingBlockY, visitedBranchLocations);
                }
            }
        }
    }

    private void recursiveLeafSearch(Set<TreeDefinition> treeDefinitions, Map<Long, Block> logBlocks, Map<Long, Block> leafBlocks,
                                     Block block, Set<Long> visitedLeafLocations, boolean detectLeavesDiagonally) {
        for (int[] offset : !detectLeavesDiagonally ? TreeGeometry.LEAF_OFFSETS : TreeGeometry.TRUNK_OFFSETS) {
            Block targetBlock = block.getRelative(offset[0], offset[1], offset[2]);
            long targetKey = TreeGeometry.packBlockKey(targetBlock);
            if (visitedLeafLocations.contains(targetKey)) {
                continue;
            }
            visitedLeafLocations.add(targetKey);

            if (isValidLeafType(treeDefinitions, logBlocks, targetBlock) && !doesLeafBorderInvalidLog(treeDefinitions, logBlocks, targetBlock)) {
                leafBlocks.put(targetKey, targetBlock);
                treeDefinitions.retainAll(this.definitionManager.narrowTreeDefinition(treeDefinitions, targetBlock.getType(), TreeBlockType.LEAF));
                recursiveLeafSearch(treeDefinitions, logBlocks, leafBlocks, targetBlock, visitedLeafLocations, detectLeavesDiagonally);
            }
        }
    }

    private boolean doesLeafBorderInvalidLog(Set<TreeDefinition> treeDefinitions, Map<Long, Block> logBlocks, Block block) {
        for (int[] offset : TreeGeometry.TRUNK_OFFSETS) {
            Block targetBlock = block.getRelative(offset[0], offset[1], offset[2]);
            if (isValidLogType(treeDefinitions, null, targetBlock) && !logBlocks.containsKey(TreeGeometry.packBlockKey(targetBlock))) {
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
                if (squaredDistance(location, trunkBlock.getLocation()) < maxDistance) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidLeafType(Set<TreeDefinition> treeDefinitions, Map<Long, Block> logBlocks, Block block) {
        if (this.placedBlockManager.isBlockPlaced(block)) {
            return false;
        }

        Material material = block.getType();
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

        if (logBlocks.isEmpty()) {
            return true;
        }

        int maxDistanceFromLog = 0;
        for (TreeDefinition definition : treeDefinitions) {
            int distance = definition.getMaxLeafDistanceFromLog();
            if (distance > maxDistanceFromLog) {
                maxDistanceFromLog = distance;
            }
        }
        double maxDistanceSquared = (double) maxDistanceFromLog * maxDistanceFromLog;
        Location blockLocation = block.getLocation();
        for (Block logBlock : logBlocks.values()) {
            if (squaredDistance(blockLocation, logBlock.getLocation()) < maxDistanceSquared) {
                return true;
            }
        }
        return false;
    }

    /**
     * Result of a legacy detection run.
     *
     * @param detected Whether the sequence of blocks was accepted as a tree
     * @param logKeys  The packed positions of the detected logs
     * @param leafKeys The packed positions of the detected leaves
     */
    public record Result(boolean detected, Set<Long> logKeys, Set<Long> leafKeys) {
    }
}
