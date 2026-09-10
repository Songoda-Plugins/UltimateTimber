package com.songoda.ultimatetimber.api.event;

import com.songoda.ultimatetimber.api.tree.DetectedTree;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called before a detected tree topples over.
 * Cancelling this event prevents the tree from falling and leaves all blocks untouched.
 */
public class TreeFallEvent extends TreeEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;

    public TreeFallEvent(@NotNull Player who, @NotNull DetectedTree detectedTree) {
        super(who, detectedTree);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
