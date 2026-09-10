package com.songoda.ultimatetimber.api.tree;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a single block within a detected or falling tree structure.
 *
 * @param <T> the underlying block type (typically {@link org.bukkit.block.Block} or {@link org.bukkit.entity.FallingBlock})
 */
public interface TreeBlock<T> {

    /**
     * Gets the block this TreeBlock represents.
     * This is either a Block or a FallingBlock.
     *
     * @return The Block for this TreeBlock
     */
    @NotNull T getBlock();

    /**
     * Gets the location of this TreeBlock.
     *
     * @return The Location of this TreeBlock
     */
    @NotNull Location getLocation();

    /**
     * Gets what type of TreeBlock this is.
     *
     * @return The TreeBlockType
     */
    @NotNull TreeBlockType getTreeBlockType();
}
