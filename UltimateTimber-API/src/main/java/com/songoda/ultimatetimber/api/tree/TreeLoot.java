package com.songoda.ultimatetimber.api.tree;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents configured bonus loot drops and/or console commands executed upon tree felling.
 */
public record TreeLoot(TreeBlockType treeBlockType, ItemStack item, String command, double chance) {
    public TreeLoot(@NotNull TreeBlockType treeBlockType, @Nullable ItemStack item, @Nullable String command, double chance) {
        this.treeBlockType = treeBlockType;
        this.item = item;
        this.command = command;
        this.chance = chance;
    }

    /**
     * Gets the tree block type this loot is for
     *
     * @return The tree block type this loot is for
     */
    @Override
    public @NotNull TreeBlockType treeBlockType() {
        return this.treeBlockType;
    }

    /**
     * Checks if this TreeLoot has an item to drop
     *
     * @return True if an item exists, otherwise false
     */
    public boolean hasItem() {
        return this.item != null;
    }

    /**
     * Gets the item that this tree loot can drop
     *
     * @return An ItemStack this tree loot can drop
     */
    @Override
    public @Nullable ItemStack item() {
        return this.item;
    }

    /**
     * Checks if this TreeLoot has a command to run
     *
     * @return True if a command exists, otherwise false
     */
    public boolean hasCommand() {
        return this.command != null;
    }

    /**
     * Gets the command that this tree loot can run
     *
     * @return The command that this tree loot can run
     */
    @Override
    public @Nullable String command() {
        return this.command;
    }

    /**
     * Gets the percent chance this tree loot will drop
     *
     * @return The percent chance this tree loot can drop
     */
    @Override
    public double chance() {
        return this.chance;
    }

    @Override
    public @NotNull String toString() {
        return "TreeLoot{" +
                "treeBlockType=" + this.treeBlockType +
                ", item=" + this.item +
                ", command='" + this.command + '\'' +
                ", chance=" + this.chance +
                '}';
    }
}
