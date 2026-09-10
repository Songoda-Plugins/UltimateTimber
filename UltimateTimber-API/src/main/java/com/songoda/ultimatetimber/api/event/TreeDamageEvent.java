package com.songoda.ultimatetimber.api.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a player takes damage from a falling timber tree block.
 */
public class TreeDamageEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled = false;
    private FallingBlock blockAttacker = null;
    private Player playerAttacker = null;

    /**
     * Constructs a TreeDamageEvent caused by a falling block.
     *
     * @param attacker The falling block causing damage
     * @param victim The player receiving damage
     */
    public TreeDamageEvent(@NotNull FallingBlock attacker, @NotNull Player victim) {
        super(victim);
        this.blockAttacker = attacker;
    }

    /**
     * Constructs a TreeDamageEvent caused by an attacking player.
     *
     * @param attacker The player causing damage
     * @param victim The player receiving damage
     */
    public TreeDamageEvent(@NotNull Player attacker, @NotNull Player victim) {
        super(victim);
        this.playerAttacker = attacker;
    }

    /**
     * Gets the attacking entity (either a {@link FallingBlock} or {@link Player}).
     *
     * @return The attacking entity, or null if unassigned
     */
    public @Nullable Entity getAttacker() {
        if (this.playerAttacker != null) {
            return this.playerAttacker;
        }
        if (this.blockAttacker != null) {
            return this.blockAttacker;
        }

        return null;
    }

    /**
     * Gets the player damaged by this event.
     *
     * @return The victim player
     */
    public @NotNull Player getVictim() {
        return this.getPlayer();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelState) {
        this.cancelled = cancelState;
    }
}
