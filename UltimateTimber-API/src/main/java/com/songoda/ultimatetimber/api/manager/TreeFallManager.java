package com.songoda.ultimatetimber.api.manager;

import com.songoda.ultimatetimber.api.tree.DetectedTree;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Service managing tree felling execution, tool durability validation,
 * permissions, and triggering topple animations.
 */
public interface TreeFallManager {

    /**
     * Evaluates whether all conditions are met for a player to fell a tree from the broken block.
     *
     * @param player The player attempting to fell the tree
     * @param block The block being broken
     * @param tool The tool being used, if any
     * @return True if the tree can topple
     */
    boolean canTopple(@NotNull Player player, @NotNull Block block, @Nullable ItemStack tool);

    /**
     * Executes the tree felling process for a detected tree.
     *
     * @param player The player felling the tree
     * @param detectedTree The detected tree to fell
     * @param tool The tool used to fell the tree, if any
     */
    void toppleTree(@NotNull Player player, @NotNull DetectedTree detectedTree, @Nullable ItemStack tool);
}
