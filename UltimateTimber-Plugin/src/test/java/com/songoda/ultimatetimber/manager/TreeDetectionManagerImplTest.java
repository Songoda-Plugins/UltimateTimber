package com.songoda.ultimatetimber.manager;

import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.test.DetectionTestSupport;
import com.songoda.ultimatetimber.test.FakeBlockWorld;
import com.songoda.ultimatetimber.test.FakeTrees;
import com.songoda.ultimatetimber.test.LegacyTreeDetector;
import com.songoda.ultimatetimber.utils.TreeGeometry;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeDetectionManagerImplTest {

    private static final Set<Material> LOG_MATERIALS = Set.of(Material.OAK_LOG);
    private static final Set<Material> LEAF_MATERIALS = Set.of(Material.OAK_LEAVES);
    private static final Set<Material> PLANTABLE_SOIL = Set.of(Material.DIRT);

    /**
     * Builds an oak with a six block trunk, two branches and a canopy that includes a leaf exactly five
     * blocks away from the trunk.
     *
     * @return The test world
     */
    private static FakeBlockWorld createOakTree() {
        FakeBlockWorld world = new FakeBlockWorld();
        world.set(0, -1, 0, Material.DIRT);
        for (int y = 0; y <= 5; y++) {
            world.set(0, y, 0, Material.OAK_LOG);
        }
        world.set(1, 3, 0, Material.OAK_LOG);
        world.set(2, 3, 0, Material.OAK_LOG);
        world.set(1, 4, 1, Material.OAK_LOG);

        for (int y = 4; y <= 7; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.abs(x) + Math.abs(z) <= 3) {
                        world.setIfAir(x, y, z, Material.OAK_LEAVES);
                    }
                }
            }
        }

        for (int x = -5; x <= -3; x++) {
            world.set(x, 5, 0, Material.OAK_LEAVES);
        }
        return world;
    }

    private static Set<Long> positions(Collection<TreeBlock<Block>> blocks) {
        Set<Long> positions = new LinkedHashSet<>();
        for (TreeBlock<Block> block : blocks) {
            positions.add(TreeGeometry.packBlockKey(block.block()));
        }
        return positions;
    }

    private static Set<Long> without(Set<Long> positions, long excluded) {
        Set<Long> remaining = new LinkedHashSet<>(positions);
        remaining.remove(excluded);
        return remaining;
    }

    @Test
    void testDetectionMatchesLegacyAlgorithmApartFromBoundaryBlocks() throws Exception {
        FakeBlockWorld world = createOakTree();
        Fixture fixture = new Fixture();

        LegacyTreeDetector.Result legacy = fixture.legacy(5, true, true).detect(world.at(0, 0, 0));
        DetectedTree detected = fixture.manager(150, 5, true, true, false).detectTree(world.at(0, 0, 0));

        assertTrue(legacy.detected());
        assertNotNull(detected);

        long boundaryLeaf = TreeGeometry.packBlockKey(-5, 5, 0);
        assertEquals(legacy.logKeys(), positions(detected.detectedTreeBlocks().getLogBlocks()));
        assertEquals(legacy.leafKeys(), without(positions(detected.detectedTreeBlocks().getLeafBlocks()), boundaryLeaf));
        assertFalse(legacy.leafKeys().contains(boundaryLeaf), "the legacy algorithm rejected a leaf exactly at the configured radius");
    }

    @Test
    void testBoundaryBlocksAreIncluded() throws Exception {
        FakeBlockWorld world = new FakeBlockWorld();
        world.set(0, -1, 0, Material.DIRT);
        for (int y = 0; y <= 2; y++) {
            world.set(0, y, 0, Material.OAK_LOG);
        }
        world.set(1, 2, 0, Material.OAK_LOG);
        world.set(2, 2, 0, Material.OAK_LOG);
        world.set(3, 2, 0, Material.OAK_LOG);

        Fixture fixture = new Fixture(FakeTrees.definition("oak", LOG_MATERIALS, LEAF_MATERIALS, 2.0, 2, false));
        LegacyTreeDetector.Result legacy = fixture.legacy(0, true, true).detect(world.at(0, 0, 0));
        DetectedTree detected = fixture.manager(150, 0, true, true, false).detectTree(world.at(0, 0, 0));

        assertNotNull(detected);
        Set<Long> logKeys = positions(detected.detectedTreeBlocks().getLogBlocks());
        assertTrue(logKeys.contains(TreeGeometry.packBlockKey(1, 2, 0)));
        assertTrue(logKeys.contains(TreeGeometry.packBlockKey(2, 2, 0)), "a log exactly at the trunk radius belongs to the tree");
        assertFalse(logKeys.contains(TreeGeometry.packBlockKey(3, 2, 0)), "a log past the trunk radius must not belong to the tree");
        assertFalse(legacy.logKeys().contains(TreeGeometry.packBlockKey(2, 2, 0)), "the legacy algorithm rejected the boundary log");
    }

    @Test
    void testDetectionDoesNotBuildLocations() throws Exception {
        FakeBlockWorld world = createOakTree();
        Fixture fixture = new Fixture();

        world.resetCounters();
        DetectedTree detected = fixture.manager(150, 5, true, true, false).detectTree(world.at(0, 0, 0));

        assertNotNull(detected);
        assertEquals(0, world.locationCalls(), "detection should work on block coordinates only");
        assertTrue(world.relativeCalls() > 0);
    }

    @Test
    void testLogLimitStopsTheTrunkScan() throws Exception {
        FakeBlockWorld world = new FakeBlockWorld();
        for (int y = 0; y < 30; y++) {
            world.set(0, y, 0, Material.OAK_LOG);
        }

        Fixture fixture = new Fixture();
        DetectedTree detected = fixture.manager(10, 0, true, true, false).detectTree(world.at(0, 0, 0));

        assertNotNull(detected);
        Set<Long> logKeys = positions(detected.detectedTreeBlocks().getLogBlocks());
        assertEquals(10, logKeys.size());
        assertTrue(logKeys.contains(TreeGeometry.packBlockKey(0, 9, 0)));
        assertFalse(logKeys.contains(TreeGeometry.packBlockKey(0, 10, 0)));
        assertEquals(30, fixture.legacy(0, true, true).detect(world.at(0, 0, 0)).logKeys().size());
    }

    @Test
    void testLeafLimitBoundsOversizedCanopies() throws Exception {
        FakeBlockWorld world = new FakeBlockWorld();
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                world.set(x, 0, z, Material.OAK_LEAVES);
            }
        }
        world.set(0, 0, 0, Material.OAK_LOG);

        Fixture fixture = new Fixture();
        DetectedTree detected = fixture.manager(1, 5, true, true, false).detectTree(world.at(0, 0, 0));

        assertNotNull(detected);
        int detectedLeaves = detected.detectedTreeBlocks().getLeafBlocks().size();
        assertTrue(detectedLeaves >= 5, "the tree still needs its minimum leaves, got " + detectedLeaves);
        assertTrue(detectedLeaves <= 8, "the leaf limit should bound the canopy, got " + detectedLeaves);
        assertTrue(fixture.legacy(5, true, true).detect(world.at(0, 0, 0)).leafKeys().size() > 8);
    }

    @Test
    void testPlacedBlocksAreExcludedFromTheTree() throws Exception {
        FakeBlockWorld world = createOakTree();
        Fixture fixture = new Fixture();
        fixture.placedBlockManager.markPlaced(world.at(0, 5, 0));

        DetectedTree detected = fixture.manager(150, 5, true, true, false).detectTree(world.at(0, 0, 0));

        assertNotNull(detected);
        assertFalse(positions(detected.detectedTreeBlocks().getLogBlocks()).contains(TreeGeometry.packBlockKey(0, 5, 0)));
        assertNull(fixture.manager(150, 5, true, true, false).detectTree(world.at(0, 5, 0)));
    }

    @Test
    void testDownwardTrunkSearchIsConfigurable() throws Exception {
        FakeBlockWorld world = new FakeBlockWorld();
        for (int y = -3; y <= 3; y++) {
            world.set(0, y, 0, Material.OAK_LOG);
        }
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                world.setIfAir(x, 3, z, Material.OAK_LEAVES);
            }
        }

        Fixture fixture = new Fixture();
        DetectedTree upwardsOnly = fixture.manager(150, 5, true, true, false).detectTree(world.at(0, 0, 0));
        DetectedTree bothWays = fixture.manager(150, 5, false, true, false).detectTree(world.at(0, 0, 0));

        assertNotNull(upwardsOnly);
        assertNotNull(bothWays);
        assertEquals(4, upwardsOnly.detectedTreeBlocks().getLogBlocks().size());
        assertEquals(7, bothWays.detectedTreeBlocks().getLogBlocks().size());
    }

    @Test
    void testLeavesAreDroppedWhenDestroyLeavesIsDisabled() throws Exception {
        FakeBlockWorld world = createOakTree();
        Fixture fixture = new Fixture();

        DetectedTree detected = fixture.manager(150, 5, true, false, false).detectTree(world.at(0, 0, 0));

        assertNotNull(detected);
        assertTrue(detected.detectedTreeBlocks().getLeafBlocks().isEmpty());
        assertFalse(detected.detectedTreeBlocks().getLogBlocks().isEmpty());
    }

    @Test
    void testBaseCheckRejectsTreesRestingOnSoil() throws Exception {
        FakeBlockWorld world = createOakTree();
        world.set(1, 0, 0, Material.OAK_LOG);
        world.set(1, -1, 0, Material.DIRT);

        Fixture fixture = new Fixture();

        assertNotNull(fixture.manager(150, 5, true, true, false).detectTree(world.at(0, 0, 0)));
        assertNull(fixture.manager(150, 5, true, true, true).detectTree(world.at(0, 0, 0)));
    }

    @Test
    void testNonLogBlocksAreNotDetectedAsTrees() throws Exception {
        FakeBlockWorld world = createOakTree();
        Fixture fixture = new Fixture();

        assertNull(fixture.manager(150, 5, true, true, false).detectTree(world.at(2, 6, 0)));
    }

    /**
     * Wires a detection manager and the legacy oracle to the same doubles.
     */
    private static final class Fixture {

        private final FakeTrees.FakePlacedBlockManager placedBlockManager = new FakeTrees.FakePlacedBlockManager();
        private final TreeDefinitionManager definitionManager;

        private Fixture() {
            this(FakeTrees.definition("oak", LOG_MATERIALS, LEAF_MATERIALS, 6.0, 5, false));
        }

        private Fixture(TreeDefinition definition) {
            this.definitionManager = FakeTrees.definitionManager(List.of(definition), PLANTABLE_SOIL);
        }

        private TreeDetectionManagerImpl manager(int maxLogsPerChop, int leavesRequiredForTree, boolean onlyDetectLogsUpwards,
                                                 boolean destroyLeaves, boolean breakEntireTreeBase) throws ReflectiveOperationException {
            TimberConfig config = DetectionTestSupport.config(maxLogsPerChop, leavesRequiredForTree, onlyDetectLogsUpwards, destroyLeaves, breakEntireTreeBase);
            return DetectionTestSupport.manager(config, this.definitionManager, this.placedBlockManager);
        }

        private LegacyTreeDetector legacy(int leavesRequiredForTree, boolean onlyDetectLogsUpwards, boolean destroyLeaves) {
            return new LegacyTreeDetector(this.definitionManager, this.placedBlockManager, leavesRequiredForTree, onlyDetectLogsUpwards, destroyLeaves);
        }
    }

}
