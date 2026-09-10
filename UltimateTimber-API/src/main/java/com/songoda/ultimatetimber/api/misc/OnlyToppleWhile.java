package com.songoda.ultimatetimber.api.misc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Player sneak requirement conditions for activating timber tree felling.
 */
public enum OnlyToppleWhile {
    /**
     * Tree felling activates only when the player is sneaking.
     */
    SNEAKING,

    /**
     * Tree felling activates only when the player is NOT sneaking.
     */
    NOT_SNEAKING,

    /**
     * Tree felling activates regardless of player sneak posture.
     */
    ALWAYS;

    /**
     * Parses an OnlyToppleWhile from a string name, case-insensitively.
     *
     * @param string The string name to parse
     * @return The matching OnlyToppleWhile, or {@link #ALWAYS} if invalid or null
     */
    public static @NotNull OnlyToppleWhile fromString(@Nullable String string) {
        if (string == null) {
            return ALWAYS;
        }
        for (OnlyToppleWhile value : values()) {
            if (value.name().equalsIgnoreCase(string)) {
                return value;
            }
        }
        return ALWAYS;
    }
}
