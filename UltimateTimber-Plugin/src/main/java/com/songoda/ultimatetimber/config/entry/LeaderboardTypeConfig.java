package com.songoda.ultimatetimber.config.entry;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.vortexdevelopment.vinject.annotation.yaml.Comment;
import net.vortexdevelopment.vinject.annotation.yaml.Key;
import net.vortexdevelopment.vinject.annotation.yaml.YamlItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Display options for one leaderboard type.
 */
@Getter
@Setter
@NoArgsConstructor
@YamlItem
public final class LeaderboardTypeConfig {

    @Comment("MiniMessage title displayed above the leaderboard rows.")
    @Key("Title")
    private String title = "<gold><bold>Top 10 Players";

    @Comment("One MiniMessage format per displayed rank, up to 10 rows. List length controls row count. Supports <rank>, <position>, <player>, <value>, <blocks>, <trees>, <logs>, and <leaves>.")
    @Key("Lines")
    private List<String> lines = createDefaultLines();

    public LeaderboardTypeConfig(String title) {
        this.title = title;
    }

    private static List<String> createDefaultLines() {
        List<String> lines = new ArrayList<>(10);
        for (int position = 1; position <= 10; position++) {
            lines.add("<gray><position>. <white><player> <dark_gray>- <gold><value>");
        }
        return lines;
    }
}
