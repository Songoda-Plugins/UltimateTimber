package com.songoda.ultimatetimber.utils;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Utility methods for block manipulation and falling block entity handling.
 */
public final class BlockUtils {

    private static final Method MODERN_SPAWN_METHOD = findMethod(
            World.class,
            "spawn",
            Location.class,
            Class.class,
            Consumer.class
    );
    private static final Method MODERN_SET_BLOCK_DATA_METHOD = findMethod(FallingBlock.class, "setBlockData", BlockData.class);
    private static final Method LEGACY_SPAWN_METHOD = findMethod(World.class, "spawnFallingBlock", Location.class, BlockData.class);

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
        if (treeBlock.block() instanceof Block block) {
            if (!block.getType().isAir()) {
                drops.add(new ItemStack(block.getType()));
            }
        } else if (treeBlock.block() instanceof FallingBlock fallingBlock) {
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
     * @param location  The spawn location
     * @param blockData The block data
     * @return The spawned falling block entity
     */
    public static FallingBlock spawnFallingBlock(Location location, BlockData blockData) {
        if (ServerVersion.isAtLeastVersion("1.20.2")) {
            return spawnWithConsumer(location.getWorld(), location, blockData);
        }

        if (LEGACY_SPAWN_METHOD == null) {
            throw new IllegalStateException("This server does not expose the legacy falling block spawn API.");
        }

        try {
            return (FallingBlock) LEGACY_SPAWN_METHOD.invoke(location.getWorld(), location, blockData);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not spawn a falling block on this server version", exception);
        }
    }

    private static FallingBlock spawnWithConsumer(World world, Location location, BlockData blockData) {
        if (MODERN_SPAWN_METHOD == null || MODERN_SET_BLOCK_DATA_METHOD == null) {
            throw new IllegalStateException("This server does not expose the modern falling block spawn API.");
        }

        try {
            return (FallingBlock) MODERN_SPAWN_METHOD.invoke(
                    world,
                    location,
                    FallingBlock.class,
                    (Consumer<FallingBlock>) fallingBlock -> setBlockData(fallingBlock, blockData)
            );
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not spawn a falling block on this server version", exception);
        }
    }

    private static void setBlockData(FallingBlock fallingBlock, BlockData blockData) {
        try {
            MODERN_SET_BLOCK_DATA_METHOD.invoke(fallingBlock, blockData);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not set falling block data on this server version", exception);
        }
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        try {
            return type.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException exception) {
            return null;
        }
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
