package com.songoda.ultimatetimber.api.manager;

import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Service managing instant or queued multi-tick block replacement to minimize server lag.
 */
public interface BlockReplacementManager {

    /**
     * Replaces a tree block with air and triggers sapling replanting according to queuing mode.
     *
     * @param treeBlock      The tree block to replace
     * @param treeDefinition The associated tree definition
     */
    void replaceBlock(@NotNull TreeBlock<Block> treeBlock, @NotNull TreeDefinition treeDefinition);

    /**
     * Replaces a tree block and attributes integrations to the player causing the replacement.
     *
     * @param treeBlock      The tree block to replace
     * @param treeDefinition The associated tree definition
     * @param actor          The player or plugin responsible for the replacement
     */
    default void replaceBlock(@NotNull TreeBlock<Block> treeBlock,
                              @NotNull TreeDefinition treeDefinition,
                              @NotNull String actor) {
        replaceBlock(treeBlock, treeDefinition);
    }

    /**
     * Immediately processes all remaining queued block replacements.
     */
    void processAll();
}
