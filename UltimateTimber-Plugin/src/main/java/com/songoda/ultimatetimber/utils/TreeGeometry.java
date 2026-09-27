package com.songoda.ultimatetimber.utils;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Offset tables and block coordinate math shared by tree detection.
 *
 * <p>Every distance check works on integer block coordinates and is boundary inclusive, so a block
 * exactly at the configured radius is treated as part of the tree.
 */
public final class TreeGeometry {

    /**
     * 3x3x3 offsets centered around a block, used for the trunk and for diagonal leaf search.
     */
    public static final int[][] TRUNK_OFFSETS = cube(-1, 1, -1, 1, -1, 1);
    /**
     * 3x2x3 offsets centered around a block, excluding the layer below, used for branch search.
     */
    public static final int[][] BRANCH_OFFSETS = cube(0, 1, -1, 1, -1, 1);
    /**
     * Face adjacent offsets, used for leaf search without diagonal detection.
     */
    public static final int[][] LEAF_OFFSETS = {
            {-1, 0, 0},
            {1, 0, 0},
            {0, -1, 0},
            {0, 1, 0},
            {0, 0, -1},
            {0, 0, 1}
    };
    private static final int XZ_BITS = 26;
    private static final int XZ_MASK = (1 << XZ_BITS) - 1;
    private static final int Y_BITS = 12;
    private static final int Y_MASK = (1 << Y_BITS) - 1;
    private static final int Y_SHIFT = XZ_BITS;
    private static final int X_SHIFT = XZ_BITS + Y_BITS;

    private TreeGeometry() {
    }

    /**
     * Packs block coordinates into a single key. Covers the full world height and roughly 67 million
     * blocks horizontally.
     *
     * @param x The block x coordinate
     * @param y The block y coordinate
     * @param z The block z coordinate
     * @return The packed key
     */
    public static long packBlockKey(int x, int y, int z) {
        return ((long) (x & XZ_MASK) << X_SHIFT) | ((long) (y & Y_MASK) << Y_SHIFT) | (z & XZ_MASK);
    }

    /**
     * Packs the coordinates of a block into a single key.
     *
     * @param block The block to pack
     * @return The packed key
     */
    public static long packBlockKey(@NotNull Block block) {
        return packBlockKey(block.getX(), block.getY(), block.getZ());
    }

    /**
     * Checks whether an offset is inside a radius, boundary inclusive.
     *
     * @param dx                 The x offset
     * @param dy                 The y offset
     * @param dz                 The z offset
     * @param maxDistanceSquared The squared radius to compare against
     * @return True if the offset is within the radius
     */
    public static boolean isWithinSquaredDistance(int dx, int dy, int dz, long maxDistanceSquared) {
        return (long) dx * dx + (long) dy * dy + (long) dz * dz <= maxDistanceSquared;
    }

    /**
     * Checks whether an offset is inside a radius, boundary inclusive.
     *
     * @param dx                 The x offset
     * @param dy                 The y offset
     * @param dz                 The z offset
     * @param maxDistanceSquared The squared, possibly scaled, radius to compare against
     * @return True if the offset is within the radius
     */
    public static boolean isWithinSquaredDistance(int dx, int dy, int dz, double maxDistanceSquared) {
        return (double) dx * dx + (double) dy * dy + (double) dz * dz <= maxDistanceSquared;
    }

    /**
     * Gets the largest single axis offset that can still be inside a squared radius. Used to reject
     * far away candidates with one integer comparison per axis before any distance math runs.
     *
     * @param maxDistanceSquared The squared radius
     * @return The inclusive axis limit
     */
    public static int axisLimit(double maxDistanceSquared) {
        return (int) Math.floor(Math.sqrt(maxDistanceSquared));
    }

    private static int[][] cube(int minY, int maxY, int minX, int maxX, int minZ, int maxZ) {
        int[][] offsets = new int[(maxY - minY + 1) * (maxX - minX + 1) * (maxZ - minZ + 1)][];
        int index = 0;
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    offsets[index++] = new int[]{x, y, z};
                }
            }
        }
        return offsets;
    }
}
