package com.songoda.ultimatetimber.api.event;

import com.songoda.ultimatetimber.api.tree.DetectedTree;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after a tree has completely fallen and its blocks have been removed from the world.
 */
public class TreeFellEvent extends TreeEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public TreeFellEvent(@NotNull Player who, @NotNull DetectedTree detectedTree) {
        super(who, detectedTree);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
