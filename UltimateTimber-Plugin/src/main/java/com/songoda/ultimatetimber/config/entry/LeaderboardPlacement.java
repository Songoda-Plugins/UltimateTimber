package com.songoda.ultimatetimber.config.entry;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.vortexdevelopment.vinject.annotation.yaml.Key;
import net.vortexdevelopment.vinject.annotation.yaml.YamlItem;

/**
 * Saved location and type for a spawned leaderboard hologram.
 */
@Getter
@Setter
@NoArgsConstructor
@YamlItem
public final class LeaderboardPlacement {

    @Key("Type")
    private String type;

    @Key("World")
    private String world;

    @Key("X")
    private double x;

    @Key("Y")
    private double y;

    @Key("Z")
    private double z;
}
