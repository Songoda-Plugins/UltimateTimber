package com.songoda.ultimatetimber.api.manager;

import com.songoda.ultimatetimber.api.animation.TreeAnimation;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Service managing active tree felling animations and tracking animated blocks.
 */
public interface TreeAnimationManager {

    /**
     * Plays the appropriate tree animation for the felled tree and player.
     *
     * @param detectedTree The felled tree
     * @param player The player who felled the tree
     */
    void runAnimation(@NotNull DetectedTree detectedTree, @NotNull Player player);

    /**
     * Checks if a world block is currently part of an active tree animation.
     *
     * @param block The block to test
     * @return True if the block is animating
     */
    boolean isBlockInAnimation(@NotNull Block block);

    /**
     * Checks if a falling block entity is currently part of an active tree animation.
     *
     * @param fallingBlock The falling block entity to test
     * @return True if the entity is part of an active animation
     */
    boolean isBlockInAnimation(@NotNull FallingBlock fallingBlock);

    /**
     * Handles the impact and landing logic when an animated falling block touches the ground.
     *
     * @param treeAnimation The parent tree animation
     * @param treeBlock The falling tree block that landed
     */
    void runFallingBlockImpact(@NotNull TreeAnimation treeAnimation, @NotNull TreeBlock<FallingBlock> treeBlock);
}
