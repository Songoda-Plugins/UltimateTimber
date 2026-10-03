package com.songoda.ultimatetimber.config;

import com.songoda.ultimatetimber.config.entry.LeaderboardPlacement;
import com.songoda.ultimatetimber.config.entry.LeaderboardTypeConfig;
import lombok.Getter;
import lombok.Setter;
import net.vortexdevelopment.vinject.annotation.yaml.Comment;
import net.vortexdevelopment.vinject.annotation.yaml.Key;
import net.vortexdevelopment.vinject.annotation.yaml.YamlConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Leaderboard text templates and saved hologram locations.
 */
@Getter
@Setter
@YamlConfiguration(file = "leaderboard.yml")
public final class LeaderboardConfig {

    @Comment("Formatting and row limits for each player statistic leaderboard.")
    @Key("Types")
    private Map<String, LeaderboardTypeConfig> types = createDefaultTypes();

    @Comment("Saved hologram placements created by /ut leaderboard spawn.")
    @Key("Placed")
    private Map<String, LeaderboardPlacement> placements = new LinkedHashMap<>();

    public static Map<String, LeaderboardTypeConfig> createDefaultTypes() {
        Map<String, LeaderboardTypeConfig> defaults = new LinkedHashMap<>();
        defaults.put("blocks", new LeaderboardTypeConfig("<gold><bold>Top 10 Broken Blocks"));
        defaults.put("trees", new LeaderboardTypeConfig("<gold><bold>Top 10 Trees Felled"));
        defaults.put("logs", new LeaderboardTypeConfig("<gold><bold>Top 10 Logs Chopped"));
        defaults.put("leaves", new LeaderboardTypeConfig("<gold><bold>Top 10 Leaves Chopped"));
        return defaults;
    }
}
