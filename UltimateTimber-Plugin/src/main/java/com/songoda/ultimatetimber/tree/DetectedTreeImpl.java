package com.songoda.ultimatetimber.tree;

import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlockSet;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import lombok.Getter;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Concrete implementation of {@link DetectedTree}.
 */
@Getter
public class DetectedTreeImpl implements DetectedTree {
    private final TreeDefinition treeDefinition;
    private final TreeBlockSet<Block> detectedTreeBlocks;

    public DetectedTreeImpl(@NotNull TreeDefinition treeDefinition, @NotNull TreeBlockSet<Block> detectedTreeBlocks) {
        this.treeDefinition = treeDefinition;
        this.detectedTreeBlocks = detectedTreeBlocks;
    }
}
