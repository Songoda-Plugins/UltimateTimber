package com.songoda.ultimatetimber.utils;

import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockType;
import org.bukkit.Location;
import org.bukkit.Sound;

/**
 * Utility methods for playing tree audio effects.
 */
public final class SoundUtils {

    private SoundUtils() {
    }

    /**
     * Plays the creaking / toppling initiation sound.
     *
     * @param block The block position where sound plays
     */
    public static void playFallingSound(TreeBlock<?> block) {
        Location location = block.getLocation();
        if (location.getWorld() != null) {
            location.getWorld().playSound(location, Sound.BLOCK_CHEST_OPEN, 2.0f, 0.1f);
        }
    }

    /**
     * Plays the landing / impact sound when an animated block lands.
     *
     * @param block The block position where sound plays
     */
    public static void playLandingSound(TreeBlock<?> block) {
        Location location = block.getLocation();
        if (location.getWorld() != null) {
            if (block.getTreeBlockType() == TreeBlockType.LOG) {
                location.getWorld().playSound(location, Sound.BLOCK_WOOD_FALL, 2.0f, 0.1f);
            } else {
                location.getWorld().playSound(location, Sound.BLOCK_GRASS_BREAK, 0.5f, 0.75f);
            }
        }
    }
}
