package com.songoda.ultimatetimber.api.manager;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Service tracking player-placed blocks to prevent player builds from being toppled as trees.
 */
public interface PlacedBlockManager {

    /**
     * Checks if a block is recorded as placed by a player.
     *
     * @param block The block to test
     * @return True if the block is placed
     */
    boolean isBlockPlaced(@NotNull Block block);

    /**
     * Updates the placed state of a block.
     *
     * @param block The block to update
     * @param isPlaced True to mark as placed, false to remove
     */
    void protectBlock(@NotNull Block block, boolean isPlaced);
}
