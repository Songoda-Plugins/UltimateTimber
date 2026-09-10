package com.songoda.ultimatetimber.api.tree;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Defines a tree type including its valid log materials, leaf materials,
 * sapling, plantable soil, detection parameters, and drop tables.
 */
public interface TreeDefinition {

    /**
     * Gets the configuration key or identifier of this tree definition.
     *
     * @return The unique tree key
     */
    @NotNull String getKey();

    /**
     * Gets the set of valid log materials for this tree definition.
     *
     * @return A Set of log Materials
     */
    @NotNull Set<Material> getLogMaterials();

    /**
     * Gets the set of valid leaf materials for this tree definition.
     *
     * @return A Set of leaf Materials
     */
    @NotNull Set<Material> getLeafMaterials();

    /**
     * Gets the sapling material for this tree definition, or null if none.
     *
     * @return The sapling Material, or null
     */
    @Nullable Material getSaplingMaterial();

    /**
     * Gets the set of valid plantable soil materials for this tree definition.
     *
     * @return A Set of plantable soil Materials
     */
    @NotNull Set<Material> getPlantableSoilMaterials();

    /**
     * Gets the maximum horizontal distance away a log can be from the trunk.
     *
     * @return The maximum log distance
     */
    double getMaxLogDistanceFromTrunk();

    /**
     * Gets the maximum distance away a leaf can be from a log block.
     *
     * @return The maximum leaf distance
     */
    int getMaxLeafDistanceFromLog();

    /**
     * Gets whether tree detection should check for leaves diagonally.
     *
     * @return True if leaves should be searched for diagonally
     */
    boolean shouldDetectLeavesDiagonally();

    /**
     * Gets whether logs of this tree should drop their original block.
     *
     * @return True if the original log block should be dropped
     */
    boolean shouldDropOriginalLog();

    /**
     * Gets whether leaves of this tree should drop their original block.
     *
     * @return True if the original leaf block should be dropped
     */
    boolean shouldDropOriginalLeaf();

    /**
     * Gets the log loot table for this tree definition.
     *
     * @return A Set of TreeLoot
     */
    @NotNull Set<TreeLoot> getLogLoot();

    /**
     * Gets the leaf loot table for this tree definition.
     *
     * @return A Set of TreeLoot
     */
    @NotNull Set<TreeLoot> getLeafLoot();

    /**
     * Gets the loot dropped once for the entire felled tree.
     *
     * @return A Set of TreeLoot
     */
    @NotNull Set<TreeLoot> getEntireTreeLoot();

    /**
     * Gets the required tools that can be used to chop this tree.
     *
     * @return A Set of ItemStacks
     */
    @NotNull Set<ItemStack> getRequiredTools();

    /**
     * Returns whether this tree requires the custom axe to be chopped.
     *
     * @return True if the custom axe is required
     */
    boolean isRequiredAxe();
}
