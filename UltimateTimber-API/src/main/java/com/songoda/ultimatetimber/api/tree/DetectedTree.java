package com.songoda.ultimatetimber.api.tree;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a successfully identified tree in the world, linking its
 * {@link TreeDefinition} with the specific set of detected blocks.
 */
public interface DetectedTree {

    /**
     * Gets the TreeDefinition of this detected tree.
     *
     * @return The TreeDefinition of this detected tree
     */
    @NotNull TreeDefinition getTreeDefinition();

    /**
     * Gets the blocks that were detected as part of this tree.
     *
     * @return A TreeBlockSet of detected Blocks
     */
    @NotNull TreeBlockSet<Block> getDetectedTreeBlocks();
}
