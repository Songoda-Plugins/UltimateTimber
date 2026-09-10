package com.songoda.ultimatetimber.api.manager;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Service managing player chopping toggle state and topple cooldowns.
 */
public interface ChoppingManager {

    /**
     * Toggles a player's individual chopping enabled state.
     *
     * @param player The player to toggle
     * @return True if chopping is now enabled, false if disabled
     */
    boolean togglePlayer(@NotNull Player player);

    /**
     * Checks if a player currently has chopping enabled.
     *
     * @param player The player to test
     * @return True if chopping is enabled
     */
    boolean isChopping(@NotNull Player player);

    /**
     * Puts a player onto tree topple cooldown.
     *
     * @param player The player to cooldown
     */
    void cooldownPlayer(@NotNull Player player);

    /**
     * Checks if a player is currently in cooldown and cannot fell trees.
     *
     * @param player The player to test
     * @return True if the player is in cooldown
     */
    boolean isInCooldown(@NotNull Player player);
}
