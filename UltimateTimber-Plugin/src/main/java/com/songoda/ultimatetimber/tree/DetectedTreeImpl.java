package com.songoda.ultimatetimber.tree;

import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlockSet;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Concrete implementation of {@link DetectedTree}.
 */
public record DetectedTreeImpl(TreeDefinition treeDefinition,
                               TreeBlockSet<Block> detectedTreeBlocks) implements DetectedTree {
    public DetectedTreeImpl(@NotNull TreeDefinition treeDefinition, @NotNull TreeBlockSet<Block> detectedTreeBlocks) {
        this.treeDefinition = treeDefinition;
        this.detectedTreeBlocks = detectedTreeBlocks;
    }
}
