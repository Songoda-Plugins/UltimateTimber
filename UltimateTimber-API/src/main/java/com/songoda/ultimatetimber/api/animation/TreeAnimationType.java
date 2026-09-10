package com.songoda.ultimatetimber.api.animation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The supported tree toppling visual animation styles.
 */
public enum TreeAnimationType {
    /**
     * Physics-based falling tree trunk and branches animation.
     */
    FANCY,

    /**
     * Tree blocks crumble downwards piece-by-piece.
     */
    CRUMBLE,

    /**
     * Tree blocks disintegrate and break sequentially without physics.
     */
    DISINTEGRATE,

    /**
     * Instantaneous felling without visual falling blocks.
     */
    NONE;

    /**
     * Parses a TreeAnimationType from a string name, case-insensitively.
     *
     * @param name The type name to parse
     * @return The matching TreeAnimationType, or {@link #FANCY} if invalid or null
     */
    public static @NotNull TreeAnimationType fromString(@Nullable String name) {
        if (name == null) {
            return FANCY;
        }
        for (TreeAnimationType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return FANCY;
    }
}
