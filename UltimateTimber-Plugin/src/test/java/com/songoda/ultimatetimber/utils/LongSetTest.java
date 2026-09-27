package com.songoda.ultimatetimber.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LongSetTest {

    @Test
    void testAddReportsWhetherTheValueWasAbsent() {
        LongSet set = new LongSet();

        assertTrue(set.add(42L));
        assertFalse(set.add(42L));
        assertEquals(1, set.size());
    }

    @Test
    void testContainsCoversZeroNegativeAndExtremeValues() {
        LongSet set = new LongSet();
        long[] values = {0L, -1L, Long.MIN_VALUE, Long.MAX_VALUE, 1L << 40, -(1L << 40)};

        for (long value : values) {
            assertTrue(set.add(value), "value " + value + " should be new");
            assertTrue(set.contains(value), "value " + value + " should be present");
        }

        assertEquals(values.length, set.size());
        assertFalse(set.contains(7L));
    }

    @Test
    void testGrowthKeepsEveryEntry() {
        LongSet set = new LongSet();

        for (int value = 0; value < 10_000; value++) {
            assertTrue(set.add(value));
        }

        assertEquals(10_000, set.size());
        for (int value = 0; value < 10_000; value++) {
            assertTrue(set.contains(value), "value " + value + " should survive a rehash");
        }
    }

    @Test
    void testRemoveKeepsFollowingProbeEntriesReachable() {
        LongSet set = new LongSet();

        for (int value = 0; value < 1_000; value++) {
            set.add(value);
        }

        for (int value = 0; value < 1_000; value += 2) {
            assertTrue(set.remove(value), "even value " + value + " should be removed");
        }

        assertEquals(500, set.size());
        for (int value = 0; value < 1_000; value++) {
            assertEquals(value % 2 != 0, set.contains(value), "unexpected state for value " + value);
        }
    }

    @Test
    void testRemoveOfMissingValueReportsFalse() {
        LongSet set = new LongSet();
        set.add(5L);

        assertFalse(set.remove(6L));
        assertEquals(1, set.size());
        assertTrue(set.contains(5L));
    }

    @Test
    void testClearEmptiesTheSetAndKeepsItUsable() {
        LongSet set = new LongSet();
        for (int value = 0; value < 100; value++) {
            set.add(value);
        }

        set.clear();

        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
        assertFalse(set.contains(50L));
        assertTrue(set.add(50L));
        assertTrue(set.contains(50L));
    }
}
