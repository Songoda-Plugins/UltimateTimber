package com.songoda.ultimatetimber.manager;

import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
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
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Detection behaviour on a tree big enough to stress the leaf scan, using the shipped mangrove
 * distances (trunk radius 30, leaf radius 10, diagonal leaf search).
 */
class LargeTreeDetectionTest {

    private static final Set<Material> LOG_MATERIALS = Set.of(Material.MANGROVE_LOG);
    private static final Set<Material> LEAF_MATERIALS = Set.of(Material.MANGROVE_LEAVES);
    private static final int LEAF_LIMIT = 150 * 8;

    private static Set<Long> positions(Collection<TreeBlock<Block>> blocks) {
        Set<Long> positions = new LinkedHashSet<>();
        for (TreeBlock<Block> block : blocks) {
            positions.add(TreeGeometry.packBlockKey(block.block()));
        }
        return positions;
    }

    private static FakeBlockWorld createLargeTree() {
        FakeBlockWorld world = new FakeBlockWorld();
        Random random = new Random(1234L);

        world.set(0, -1, 0, Material.DIRT);
        for (int y = 0; y < 24; y++) {
            world.set(0, y, 0, Material.MANGROVE_LOG);
        }

        int[][] directions = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int branch = 0; branch < 14; branch++) {
            int[] direction = directions[branch % 4];
            int y = 6 + random.nextInt(17);
            int length = 4 + random.nextInt(3);
            for (int step = 1; step <= length; step++) {
                world.set(direction[0] * step, y, direction[1] * step, Material.MANGROVE_LOG);
                if (step > 2) {
                    world.set(direction[0] * step, y + 1, direction[1] * step, Material.MANGROVE_LOG);
                }
            }
        }

        for (int y = 18; y <= 28; y++) {
            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    if (x * x + z * z <= 36) {
                        world.setIfAir(x, y, z, Material.MANGROVE_LEAVES);
                    }
                }
            }
        }
        return world;
    }

    @Test
    void testLargeTreeKeepsLegacyLogsAndStaysWithinLimits() throws Exception {
        FakeBlockWorld world = createLargeTree();
        TreeDefinition definition = FakeTrees.definition("mangrove", LOG_MATERIALS, LEAF_MATERIALS, 30.0, 10, true);
        TreeDefinitionManager definitionManager = FakeTrees.definitionManager(List.of(definition), Set.of(Material.DIRT));
        FakeTrees.FakePlacedBlockManager placedBlockManager = new FakeTrees.FakePlacedBlockManager();
        LegacyTreeDetector legacyDetector = new LegacyTreeDetector(definitionManager, placedBlockManager, 5, true, true);

        placedBlockManager.resetProbes();
        LegacyTreeDetector.Result legacy = legacyDetector.detect(world.at(0, 0, 0));
        int legacyProbes = placedBlockManager.probes();

        TreeDetectionManagerImpl manager = DetectionTestSupport.manager(
                DetectionTestSupport.config(150, 5, true, true, false), definitionManager, placedBlockManager);

        world.resetCounters();
        placedBlockManager.resetProbes();
        DetectedTree detected = manager.detectTree(world.at(0, 0, 0));
        int newLocations = world.locationCalls();
        int newProbes = placedBlockManager.probes();

        assertTrue(legacy.detected());
        assertNotNull(detected);
        assertEquals(legacy.logKeys(), positions(detected.detectedTreeBlocks().getLogBlocks()));
        assertTrue(detected.detectedTreeBlocks().getLeafBlocks().size() <= LEAF_LIMIT, "leaf detection must stay within the limit derived from Max Logs Per Chop");
        assertEquals(0, newLocations, "detection should work on block coordinates only");
        assertTrue(newProbes * 4 < legacyProbes, "material first validation should avoid most placed block probes, was " + newProbes + " against " + legacyProbes);
    }
}
