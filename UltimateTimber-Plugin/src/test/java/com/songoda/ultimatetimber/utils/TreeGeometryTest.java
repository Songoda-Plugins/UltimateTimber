package com.songoda.ultimatetimber.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeGeometryTest {

    @Test
    void testOffsetTablesMatchTheLegacyShapes() {
        assertEquals(27, TreeGeometry.TRUNK_OFFSETS.length);
        assertEquals(18, TreeGeometry.BRANCH_OFFSETS.length);
        assertEquals(6, TreeGeometry.LEAF_OFFSETS.length);
    }

    @Test
    void testTrunkOffsetsCoverTheCenteredCube() {
        Set<String> distinct = new HashSet<>();
        for (int[] offset : TreeGeometry.TRUNK_OFFSETS) {
            assertTrue(Math.abs(offset[0]) <= 1);
            assertTrue(Math.abs(offset[1]) <= 1);
            assertTrue(Math.abs(offset[2]) <= 1);
            assertTrue(distinct.add(offset[0] + ":" + offset[1] + ":" + offset[2]));
        }

        assertEquals(27, distinct.size());
    }

    @Test
    void testBranchOffsetsExcludeTheLayerBelow() {
        Set<String> distinct = new HashSet<>();
        for (int[] offset : TreeGeometry.BRANCH_OFFSETS) {
            assertTrue(offset[1] >= 0 && offset[1] <= 1);
            assertTrue(Math.abs(offset[0]) <= 1);
            assertTrue(Math.abs(offset[2]) <= 1);
            assertTrue(distinct.add(offset[0] + ":" + offset[1] + ":" + offset[2]));
        }

        assertEquals(18, distinct.size());
    }

    @Test
    void testLeafOffsetsAreFaceAdjacent() {
        for (int[] offset : TreeGeometry.LEAF_OFFSETS) {
            int nonZero = 0;
            for (int axis : offset) {
                if (axis != 0) {
                    assertEquals(1, Math.abs(axis));
                    nonZero++;
                }
            }
            assertEquals(1, nonZero);
        }
    }

    @Test
    void testPackBlockKeyDistinguishesCoordinates() {
        Set<Long> keys = new HashSet<>();

        assertTrue(keys.add(TreeGeometry.packBlockKey(0, 0, 0)));
        assertTrue(keys.add(TreeGeometry.packBlockKey(0, 0, 1)));
        assertTrue(keys.add(TreeGeometry.packBlockKey(0, 1, 0)));
        assertTrue(keys.add(TreeGeometry.packBlockKey(1, 0, 0)));
        assertTrue(keys.add(TreeGeometry.packBlockKey(-1, 0, 0)));
        assertTrue(keys.add(TreeGeometry.packBlockKey(0, -1, 0)));
        assertTrue(keys.add(TreeGeometry.packBlockKey(0, 0, -1)));
        assertTrue(keys.add(TreeGeometry.packBlockKey(-30_000_000, 320, 30_000_000)));
        assertTrue(keys.add(TreeGeometry.packBlockKey(30_000_000, -64, -30_000_000)));
        assertEquals(9, keys.size());

        assertEquals(TreeGeometry.packBlockKey(12, -60, 345), TreeGeometry.packBlockKey(12, -60, 345));
        assertNotEquals(TreeGeometry.packBlockKey(12, -60, 345), TreeGeometry.packBlockKey(12, -60, 346));
    }

    @Test
    void testDistanceIsBoundaryInclusive() {
        assertTrue(TreeGeometry.isWithinSquaredDistance(3, 4, 0, 25L));
        assertTrue(TreeGeometry.isWithinSquaredDistance(0, 0, 5, 25L));
        assertFalse(TreeGeometry.isWithinSquaredDistance(0, 0, 6, 25L));
        assertFalse(TreeGeometry.isWithinSquaredDistance(3, 4, 0, 24L));
        assertTrue(TreeGeometry.isWithinSquaredDistance(0, 0, 0, 0L));
        assertFalse(TreeGeometry.isWithinSquaredDistance(1, 0, 0, 0L));
    }

    @Test
    void testScaledDistanceIsBoundaryInclusive() {
        // 2.0 blocks of trunk distance scaled by 1.5 gives a squared limit of 6.0.
        assertTrue(TreeGeometry.isWithinSquaredDistance(2, 1, 1, 6.0));
        assertTrue(TreeGeometry.isWithinSquaredDistance(2, 1, 0, 6.0));
        assertFalse(TreeGeometry.isWithinSquaredDistance(3, 0, 0, 6.0));
    }

    @Test
    void testAxisLimitNeverRejectsAnInRangeOffset() {
        int radius = 5;
        int axisLimit = TreeGeometry.axisLimit((double) radius * radius);
        assertEquals(radius, axisLimit);

        for (int dx = -radius - 2; dx <= radius + 2; dx++) {
            for (int dy = -radius - 2; dy <= radius + 2; dy++) {
                for (int dz = -radius - 2; dz <= radius + 2; dz++) {
                    if (TreeGeometry.isWithinSquaredDistance(dx, dy, dz, (long) radius * radius)) {
                        assertTrue(Math.abs(dx) <= axisLimit);
                        assertTrue(Math.abs(dy) <= axisLimit);
                        assertTrue(Math.abs(dz) <= axisLimit);
                    }
                }
            }
        }
    }
}
