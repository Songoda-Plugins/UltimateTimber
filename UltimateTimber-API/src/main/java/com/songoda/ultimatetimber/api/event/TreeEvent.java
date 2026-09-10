package com.songoda.ultimatetimber.api.event;

import com.songoda.ultimatetimber.api.tree.DetectedTree;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Base event for all timber-related player tree actions.
 */
public abstract class TreeEvent extends PlayerEvent {
    protected final DetectedTree detectedTree;

    public TreeEvent(@NotNull Player who, @NotNull DetectedTree detectedTree) {
        super(who);
        this.detectedTree = detectedTree;
    }

    /**
     * Gets the detected tree associated with this event.
     *
     * @return The DetectedTree instance
     */
    public @NotNull DetectedTree getDetectedTree() {
        return this.detectedTree;
    }
}
