package com.songoda.ultimatetimber.api.manager;

import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlockType;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Service responsible for scanning, detecting, and validating trees in the world.
 */
public interface TreeDetectionManager {

    /**
     * Detects a full tree starting from an initial broken block.
     *
     * @param initialBlock The starting block of the detection
     * @return The DetectedTree if valid, or null if not recognized as a tree
     */
    @Nullable DetectedTree detectTree(@NotNull Block initialBlock);

    /**
     * Gets all configured tree definitions that match the given log block.
     *
     * @param block The block to check
     * @return A Set of matching TreeDefinitions
     */
    @NotNull Set<TreeDefinition> getTreeDefinitionsForLog(@NotNull Block block);

    /**
     * Narrows a set of possible tree definitions down to those matching the given block and type.
     *
     * @param possibleTreeDefinitions The candidate tree definitions
     * @param block The block to test
     * @param treeBlockType The type of tree block (log or leaf)
     * @return A narrowed Set of matching TreeDefinitions
     */
    @NotNull Set<TreeDefinition> narrowTreeDefinition(@NotNull Set<TreeDefinition> possibleTreeDefinitions,
                                                     @NotNull Block block,
                                                     @NotNull TreeBlockType treeBlockType);

    /**
     * Narrows a set of possible tree definitions down to those matching the given material and type.
     *
     * @param possibleTreeDefinitions The candidate tree definitions
     * @param material The block material
     * @param treeBlockType The type of tree block (log or leaf)
     * @return A narrowed Set of matching TreeDefinitions
     */
    @NotNull Set<TreeDefinition> narrowTreeDefinition(@NotNull Set<TreeDefinition> possibleTreeDefinitions,
                                                     @Nullable Material material,
                                                     @NotNull TreeBlockType treeBlockType);
}
