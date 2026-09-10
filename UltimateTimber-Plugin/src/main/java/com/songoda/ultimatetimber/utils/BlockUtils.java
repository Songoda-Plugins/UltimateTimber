package com.songoda.ultimatetimber.utils;

import com.songoda.ultimatetimber.api.tree.TreeBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for block manipulation and falling block entity handling.
 */
public final class BlockUtils {

    private BlockUtils() {
    }

    /**
     * Obtains standard item drops for the given tree block.
     *
     * @param treeBlock The tree block to get drops for
     * @return A collection of dropped item stacks
     */
    public static Collection<ItemStack> getBlockDrops(TreeBlock<?> treeBlock) {
        Set<ItemStack> drops = new HashSet<>();
        if (treeBlock.getBlock() instanceof Block block) {
            if (!block.getType().isAir()) {
                drops.add(new ItemStack(block.getType()));
            }
        } else if (treeBlock.getBlock() instanceof FallingBlock fallingBlock) {
            Material material = fallingBlock.getBlockData().getMaterial();
            if (!material.isAir()) {
                drops.add(new ItemStack(material));
            }
        }
        return drops;
    }

    /**
     * Toggles gravity on a falling block.
     *
     * @param fallingBlock The falling block
     * @param applyGravity Whether gravity should apply
     */
    public static void toggleGravityFallingBlock(FallingBlock fallingBlock, boolean applyGravity) {
        fallingBlock.setGravity(applyGravity);
    }

    /**
     * Spawns a falling block using block data at the specified location.
     *
     * @param location The spawn location
     * @param blockData The block data
     * @return The spawned falling block entity
     */
    public static FallingBlock spawnFallingBlock(Location location, BlockData blockData) {
        return location.getWorld().spawnFallingBlock(location, blockData);
    }

    /**
     * Configures a falling block for tree animations.
     *
     * @param fallingBlock The falling block to configure
     */
    public static void configureFallingBlock(FallingBlock fallingBlock) {
        toggleGravityFallingBlock(fallingBlock, false);
        fallingBlock.setDropItem(false);
        fallingBlock.setHurtEntities(false);
    }
}
