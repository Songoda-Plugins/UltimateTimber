package com.songoda.ultimatetimber.tree;

import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockSet;
import com.songoda.ultimatetimber.api.tree.TreeBlockType;
import com.songoda.ultimatetimber.test.FakeBlockWorld;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeBlockSetTest {

    private static Set<String> keys(Set<TreeBlock<Block>> blocks) {
        return blocks.stream()
                .map(block -> block.block().getX() + ":" + block.block().getY() + ":" + block.block().getZ())
                .collect(Collectors.toSet());
    }

    private static TreeBlock<Block> logBlock(FakeBlockWorld world, int x, int y, int z) {
        return new TreeBlockImpl(world.at(x, y, z), TreeBlockType.LOG);
    }

    private static TreeBlock<Block> leafBlock(FakeBlockWorld world, int x, int y, int z) {
        return new TreeBlockImpl(world.at(x, y, z), TreeBlockType.LEAF);
    }

    @Test
    void testAddRejectsDuplicates() {
        FakeBlockWorld world = new FakeBlockWorld();
        TreeBlockSet<Block> treeBlocks = new TreeBlockSet<>(logBlock(world, 0, 0, 0));

        assertFalse(treeBlocks.add(logBlock(world, 0, 0, 0)));
        assertTrue(treeBlocks.add(logBlock(world, 0, 1, 0)));
        assertEquals(2, treeBlocks.size());
        assertEquals(2, treeBlocks.getLogBlocks().size());
    }

    @Test
    void testSortAndLimitKeepsViewsInSync() {
        FakeBlockWorld world = new FakeBlockWorld();
        TreeBlockSet<Block> treeBlocks = new TreeBlockSet<>(logBlock(world, 0, 4, 0));
        for (int y = 0; y <= 3; y++) {
            treeBlocks.add(logBlock(world, 0, y, 0));
        }
        treeBlocks.add(leafBlock(world, 1, 1, 0));
        treeBlocks.add(leafBlock(world, 1, 9, 0));

        List<TreeBlock<Block>> logView = treeBlocks.getLogBlocks();
        List<TreeBlock<Block>> leafView = treeBlocks.getLeafBlocks();

        treeBlocks.sortAndLimit(3);

        assertEquals(List.of(0, 1, 2), logView.stream().map(block -> block.getLocation().getBlockY()).collect(Collectors.toList()));
        assertEquals(List.of(1), leafView.stream().map(block -> block.getLocation().getBlockY()).collect(Collectors.toList()));
        assertEquals(4, treeBlocks.getAllTreeBlocks().size());
        assertEquals(4, treeBlocks.size());
    }

    @Test
    void testSortAndLimitLeavesSmallTreesUntouched() {
        FakeBlockWorld world = new FakeBlockWorld();
        TreeBlockSet<Block> treeBlocks = new TreeBlockSet<>(logBlock(world, 0, 0, 0));
        treeBlocks.add(leafBlock(world, 1, 9, 0));

        treeBlocks.sortAndLimit(150);

        assertEquals(1, treeBlocks.getLogBlocks().size());
        assertEquals(1, treeBlocks.getLeafBlocks().size());
    }

    @Test
    void testRemoveAllByTypeKeepsViewsInSync() {
        FakeBlockWorld world = new FakeBlockWorld();
        TreeBlockSet<Block> treeBlocks = new TreeBlockSet<>(logBlock(world, 0, 0, 0));
        treeBlocks.add(leafBlock(world, 1, 0, 0));
        treeBlocks.add(leafBlock(world, 1, 1, 0));

        assertTrue(treeBlocks.removeAll(TreeBlockType.LEAF));

        assertTrue(treeBlocks.getLeafBlocks().isEmpty());
        assertEquals(1, treeBlocks.getLogBlocks().size());
        assertEquals(Set.of("0:0:0"), keys(treeBlocks.getAllTreeBlocks()));
    }
}
