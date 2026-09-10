package com.songoda.ultimatetimber.api.manager;

import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockType;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Repository and management service for loaded tree definitions, required tools, and drop tables.
 */
public interface TreeDefinitionManager {

    /**
     * Gets all loaded and active tree definitions.
     *
     * @return An unmodifiable Set of TreeDefinitions
     */
    @NotNull Set<TreeDefinition> getTreeDefinitions();

    /**
     * Gets a tree definition by its unique key.
     *
     * @param key The tree key
     * @return The TreeDefinition, or null if not found
     */
    @Nullable TreeDefinition getTreeDefinition(@NotNull String key);

    /**
     * Gets all tree definitions whose log materials match the given block.
     *
     * @param block The block to match
     * @return A Set of matching TreeDefinitions
     */
    @NotNull Set<TreeDefinition> getTreeDefinitionsForLog(@NotNull Block block);

    /**
     * Narrows a set of tree definitions matching a specific block and block type.
     *
     * @param possibleTreeDefinitions Candidate tree definitions
     * @param block The block to test
     * @param treeBlockType The block type
     * @return A narrowed Set of TreeDefinitions
     */
    @NotNull Set<TreeDefinition> narrowTreeDefinition(@NotNull Set<TreeDefinition> possibleTreeDefinitions,
                                                     @NotNull Block block,
                                                     @NotNull TreeBlockType treeBlockType);

    /**
     * Narrows a set of tree definitions matching a specific material and block type.
     *
     * @param possibleTreeDefinitions Candidate tree definitions
     * @param material The material to match
     * @param treeBlockType The block type
     * @return A narrowed Set of TreeDefinitions
     */
    @NotNull Set<TreeDefinition> narrowTreeDefinition(@NotNull Set<TreeDefinition> possibleTreeDefinitions,
                                                     @Nullable Material material,
                                                     @NotNull TreeBlockType treeBlockType);

    /**
     * Checks if a held tool is permitted to fell any configured tree type.
     *
     * @param tool The tool item
     * @return True if allowed
     */
    boolean isToolValidForAnyTreeDefinition(@Nullable ItemStack tool);

    /**
     * Checks if a held tool is permitted to fell a specific tree type.
     *
     * @param treeDefinition The tree definition
     * @param tool The tool item
     * @return True if allowed
     */
    boolean isToolValidForTreeDefinition(@NotNull TreeDefinition treeDefinition, @Nullable ItemStack tool);

    /**
     * Drops configured loot or executes reward commands for a tree block.
     *
     * @param treeDefinition The tree definition
     * @param treeBlock The tree block being dropped
     * @param player The player felling the tree
     * @param hasSilkTouch Whether silk touch applies
     * @param isForEntireTree Whether this is the once-per-tree drop
     */
    void dropTreeLoot(@NotNull TreeDefinition treeDefinition,
                      @NotNull TreeBlock<?> treeBlock,
                      @NotNull Player player,
                      boolean hasSilkTouch,
                      boolean isForEntireTree);

    /**
     * Gets all valid plantable soil materials for a tree definition (including global soil types).
     *
     * @param treeDefinition The tree definition
     * @return A Set of valid soil Materials
     */
    @NotNull Set<Material> getPlantableSoilMaterials(@NotNull TreeDefinition treeDefinition);

    /**
     * Gets the configured required axe item stack, if one is configured.
     *
     * @return The required axe ItemStack, or null if none
     */
    @Nullable ItemStack getRequiredAxe();

    /**
     * Gets whether a custom axe is globally required for all tree types.
     *
     * @return True if globally required
     */
    boolean isGlobalAxeRequired();
}
