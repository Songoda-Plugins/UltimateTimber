package com.songoda.ultimatetimber.config.entry;

import lombok.Getter;
import lombok.Setter;
import net.vortexdevelopment.vinject.annotation.yaml.Comment;
import net.vortexdevelopment.vinject.annotation.yaml.Key;
import net.vortexdevelopment.vinject.annotation.yaml.YamlItem;

/**
 * Configuration for player tree-felling statistics and leaderboard refreshes.
 */
@Getter
@Setter
@YamlItem
public final class StatisticsConfig {

    @Comment("Enable tracking player tree-felling statistics and PlaceholderAPI statistics placeholders.")
    @Key("Enabled")
    private boolean enabled = true;

    @Comment("How often the cached top 10 leaderboards refresh, in seconds.")
    @Key("Top Player Refresh")
    private int topPlayerRefresh = 60;
}
