package com.songoda.ultimatetimber.utils;

import com.songoda.core.compatibility.ServerVersion;
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

    private static final Particle BLOCK_PARTICLE = Particle.valueOf(
            ServerVersion.isAtLeastVersion("1.20.5") ? "BLOCK" : "BLOCK_CRACK");

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

        Location location = particleLocation(treeBlock);
        if (location.getWorld() != null) {
            location.getWorld().spawnParticle(BLOCK_PARTICLE, location, 10, 0.2, 0.2, 0.2, blockData);
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

        Location location = particleLocation(treeBlock);
        if (location.getWorld() != null) {
            location.getWorld().spawnParticle(BLOCK_PARTICLE, location, 15, 0.3, 0.1, 0.3, blockData);
        }
    }

    private static Location particleLocation(TreeBlock<?> treeBlock) {
        Location location = treeBlock.getLocation().clone();
        if (treeBlock.block() instanceof FallingBlock) {
            return location.add(0.0, 0.5, 0.0);
        }

        return location.add(0.5, 0.5, 0.5);
    }

    private static BlockData extractBlockData(TreeBlock<?> treeBlock) {
        if (treeBlock.block() instanceof Block block) {
            return block.getBlockData();
        } else if (treeBlock.block() instanceof FallingBlock fallingBlock) {
            return fallingBlock.getBlockData();
        }
        return null;
    }
}
