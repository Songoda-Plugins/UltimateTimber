package com.songoda.ultimatetimber.api.manager;

import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Service managing sapling replanting and temporary protection after tree felling.
 */
public interface SaplingManager {

    /**
     * Replants a sapling at the position of the given tree block based on tree definition rules.
     *
     * @param treeDefinition The tree definition
     * @param treeBlock The tree block to replant at
     */
    void replantSapling(@NotNull TreeDefinition treeDefinition, @NotNull TreeBlock<?> treeBlock);

    /**
     * Replants a sapling with configured chance (e.g. for fallen leaf blocks hitting ground).
     *
     * @param treeDefinition The tree definition
     * @param treeBlock The tree block to replant at
     */
    void replantSaplingWithChance(@NotNull TreeDefinition treeDefinition, @NotNull TreeBlock<?> treeBlock);

    /**
     * Checks if a sapling block at the given position is currently protected from breaking.
     *
     * @param block The block to test
     * @return True if the sapling is protected
     */
    boolean isSaplingProtected(@NotNull Block block);
}
