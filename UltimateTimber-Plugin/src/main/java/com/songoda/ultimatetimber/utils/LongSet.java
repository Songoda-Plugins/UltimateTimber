package com.songoda.ultimatetimber.utils;

import java.util.Arrays;

/**
 * Minimal open addressing set of {@code long} values.
 *
 * <p>Tree detection probes sets of block positions hundreds of thousands of times per chop, where
 * {@code HashSet<Long>} boxing and node allocation dominate the cost. This set stores primitives in
 * an open addressed table, so lookups and inserts allocate nothing once the table is sized.
 *
 * <p>Instances are not thread safe and are intended to be used from the main server thread.
 */
public final class LongSet {

    private static final int MIN_TABLE_SIZE = 16;
    private static final float LOAD_FACTOR = 0.6F;
    private static final long HASH_MULTIPLIER = -7046029254386353131L;

    private long[] table;
    private boolean[] used;
    private int size;
    private int resizeThreshold;

    /**
     * Creates an empty set with the default table size.
     */
    public LongSet() {
        this(MIN_TABLE_SIZE);
    }

    /**
     * Creates an empty set sized for the expected number of entries.
     *
     * @param expectedSize The number of entries the set is expected to hold
     */
    public LongSet(int expectedSize) {
        int capacity = MIN_TABLE_SIZE;
        while (capacity * LOAD_FACTOR < expectedSize) {
            capacity <<= 1;
        }

        this.table = new long[capacity];
        this.used = new boolean[capacity];
        this.resizeThreshold = (int) (capacity * LOAD_FACTOR);
    }

    private static int hash(long value) {
        return Long.hashCode(value * HASH_MULTIPLIER);
    }

    /**
     * Adds a value to the set.
     *
     * @param value The value to add
     * @return True if the value was absent, false when it was already present
     */
    public boolean add(long value) {
        int mask = this.table.length - 1;
        int index = hash(value) & mask;
        while (this.used[index]) {
            if (this.table[index] == value) {
                return false;
            }
            index = (index + 1) & mask;
        }

        this.used[index] = true;
        this.table[index] = value;
        this.size++;
        if (this.size > this.resizeThreshold) {
            rehash(this.table.length << 1);
        }
        return true;
    }

    /**
     * Checks whether the set contains a value.
     *
     * @param value The value to look for
     * @return True if the value is present
     */
    public boolean contains(long value) {
        return indexOf(value) >= 0;
    }

    /**
     * Removes a value from the set.
     *
     * @param value The value to remove
     * @return True if the value was present
     */
    public boolean remove(long value) {
        int index = indexOf(value);
        if (index < 0) {
            return false;
        }

        this.used[index] = false;
        this.size--;
        relocateFollowingEntries(index);
        return true;
    }

    /**
     * Gets the number of entries in the set.
     *
     * @return The entry count
     */
    public int size() {
        return this.size;
    }

    /**
     * Checks whether the set holds no entries.
     *
     * @return True if the set is empty
     */
    public boolean isEmpty() {
        return this.size == 0;
    }

    /**
     * Removes every entry while keeping the current table size.
     */
    public void clear() {
        if (this.size == 0) {
            return;
        }

        Arrays.fill(this.used, false);
        this.size = 0;
    }

    private int indexOf(long value) {
        int mask = this.table.length - 1;
        int index = hash(value) & mask;
        while (this.used[index]) {
            if (this.table[index] == value) {
                return index;
            }
            index = (index + 1) & mask;
        }
        return -1;
    }

    /**
     * Re-inserts the probe cluster that followed a removed slot, which keeps later lookups working.
     */
    private void relocateFollowingEntries(int removedIndex) {
        int index = (removedIndex + 1) & (this.table.length - 1);
        while (this.used[index]) {
            long value = this.table[index];
            this.used[index] = false;
            this.size--;
            add(value);
            index = (index + 1) & (this.table.length - 1);
        }
    }

    private void rehash(int newCapacity) {
        long[] previousTable = this.table;
        boolean[] previousUsed = this.used;

        this.table = new long[newCapacity];
        this.used = new boolean[newCapacity];
        this.resizeThreshold = (int) (newCapacity * LOAD_FACTOR);
        this.size = 0;

        for (int index = 0; index < previousTable.length; index++) {
            if (previousUsed[index]) {
                add(previousTable[index]);
            }
        }
    }
}
