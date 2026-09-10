package com.songoda.ultimatetimber.api.animation;

import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlockSet;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an active tree animation sequence for a felled tree.
 */
public interface TreeAnimation {

    /**
     * Gets the visual animation type being played.
     *
     * @return The TreeAnimationType
     */
    @NotNull TreeAnimationType getTreeAnimationType();

    /**
     * Gets the detected tree being animated.
     *
     * @return The DetectedTree instance
     */
    @NotNull DetectedTree getDetectedTree();

    /**
     * Gets the player who felled the tree.
     *
     * @return The Player instance
     */
    @NotNull Player getPlayer();

    /**
     * Gets whether silk touch tool rules should be applied to the animation drops.
     *
     * @return True if silk touch is active
     */
    boolean hasSilkTouch();

    /**
     * Gets the collection of active falling entity blocks spawned for this animation.
     *
     * @return A TreeBlockSet of FallingBlocks
     */
    @NotNull TreeBlockSet<FallingBlock> getFallingTreeBlocks();

    /**
     * Removes a falling block entity from the active tracking set.
     *
     * @param fallingBlock The FallingBlock entity to remove
     */
    void removeFallingBlock(@NotNull FallingBlock fallingBlock);

    /**
     * Starts playback of the animation sequence and invokes the callback upon completion.
     *
     * @param whenFinished The callback to run when animation finishes
     */
    void playAnimation(@NotNull Runnable whenFinished);
}
