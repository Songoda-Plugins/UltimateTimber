package com.songoda.ultimatetimber.api.manager;

import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlockType;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Service responsible for scanning, detecting, and validating trees in the world.
 */
public interface TreeDetectionManager {

    /**
     * Detects a full tree starting from an initial broken block.
     *
     * @param initialBlock The starting block of the detection
     * @return The DetectedTree if valid, or null if not recognized as a tree. On Folia, this only
     * detects trees contained in the current region; use {@link #detectTreeAsync(Block, Consumer)}
     * when a tree may cross region boundaries.
     */
    @Nullable DetectedTree detectTree(@NotNull Block initialBlock);

    /**
     * Detects a tree asynchronously, allowing implementations to inspect blocks owned by multiple regions.
     * On Folia, call this while on the initial block's owning region and before that block is broken so its
     * material can be captured safely. The callback receives null when the starting block is not part of a valid tree.
     *
     * @param initialBlock The starting block of the detection
     * @param callback     Receives the detected tree, or null when no tree was found
     */
    default void detectTreeAsync(@NotNull Block initialBlock, @NotNull Consumer<DetectedTree> callback) {
        callback.accept(this.detectTree(initialBlock));
    }

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
     * @param block                   The block to test
     * @param treeBlockType           The type of tree block (log or leaf)
     * @return A narrowed Set of matching TreeDefinitions
     */
    @NotNull Set<TreeDefinition> narrowTreeDefinition(@NotNull Set<TreeDefinition> possibleTreeDefinitions,
                                                      @NotNull Block block,
                                                      @NotNull TreeBlockType treeBlockType);

    /**
     * Narrows a set of possible tree definitions down to those matching the given material and type.
     *
     * @param possibleTreeDefinitions The candidate tree definitions
     * @param material                The block material
     * @param treeBlockType           The type of tree block (log or leaf)
     * @return A narrowed Set of matching TreeDefinitions
     */
    @NotNull Set<TreeDefinition> narrowTreeDefinition(@NotNull Set<TreeDefinition> possibleTreeDefinitions,
                                                      @Nullable Material material,
                                                      @NotNull TreeBlockType treeBlockType);
}
