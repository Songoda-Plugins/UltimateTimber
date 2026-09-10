package com.songoda.ultimatetimber.utils;

import com.songoda.ultimatetimber.api.tree.TreeBlock;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;

/**
 * Utility methods for playing tree particle effects.
 */
public final class ParticleUtils {

    private ParticleUtils() {
    }

    /**
     * Plays falling particles around a moving tree block.
     *
     * @param treeBlock The tree block
     */
    public static void playFallingParticles(TreeBlock<?> treeBlock) {
        BlockData blockData = extractBlockData(treeBlock);
        if (blockData == null) {
            return;
        }

        Location location = treeBlock.getLocation().clone().add(0.5, 0.5, 0.5);
        if (location.getWorld() != null) {
            location.getWorld().spawnParticle(Particle.BLOCK, location, 10, 0.2, 0.2, 0.2, blockData);
        }
    }

    /**
     * Plays landing impact particles when a tree block hits the ground.
     *
     * @param treeBlock The tree block
     */
    public static void playLandingParticles(TreeBlock<?> treeBlock) {
        BlockData blockData = extractBlockData(treeBlock);
        if (blockData == null) {
            return;
        }

        Location location = treeBlock.getLocation().clone().add(0.5, 0.5, 0.5);
        if (location.getWorld() != null) {
            location.getWorld().spawnParticle(Particle.BLOCK, location, 15, 0.3, 0.1, 0.3, blockData);
        }
    }

    private static BlockData extractBlockData(TreeBlock<?> treeBlock) {
        if (treeBlock.getBlock() instanceof Block block) {
            return block.getBlockData();
        } else if (treeBlock.getBlock() instanceof FallingBlock fallingBlock) {
            return fallingBlock.getBlockData();
        }
        return null;
    }
}
