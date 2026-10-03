package com.songoda.ultimatetimber.integration;

import com.songoda.core.vortexcore.vinject.annotation.PlaceholderApiExpansion;
import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.database.service.PlayerTreeStatsLeaderboardService;
import com.songoda.ultimatetimber.listener.PlayerTreeStatsListener;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.vortexdevelopment.vinject.annotation.Inject;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI expansion for player tree-felling statistics.
 */
@PlaceholderApiExpansion
public final class UltimateTimberPlaceholderExpansion extends PlaceholderExpansion {
    @Inject
    private UltimateTimber plugin;

    @Inject
    private PlayerTreeStatsListener statsListener;

    @Inject
    private PlayerTreeStatsLeaderboardService leaderboardService;

    @Override
    public @NotNull String getIdentifier() {
        return "ultimatetimber";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Songoda";
    }

    @Override
    public @NotNull String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(@Nullable OfflinePlayer player, @NotNull String identifier) {
        if (identifier.startsWith("top_")) {
            return resolveTopPlayer(identifier);
        }

        if (player == null) {
            return null;
        }

        if (!this.leaderboardService.isEnabled()) {
            return null;
        }

        PlayerTreeStatsListener.PlayerTreeStatsSnapshot stats = this.statsListener.getStats(player.getUniqueId());
        return switch (identifier) {
            case "chopped_trees" -> Long.toString(stats.triggers());
            case "chopped_logs" -> Long.toString(stats.logsChopped());
            case "chopped_leaves" -> Long.toString(stats.leavesChopped());
            case "chopped_blocks" -> Long.toString(stats.totalBlocksChopped());
            default -> null;
        };
    }

    private @Nullable String resolveTopPlayer(String identifier) {
        if (!this.leaderboardService.isEnabled()) {
            return null;
        }

        String[] parts = identifier.split("_");
        if (parts.length != 3 && parts.length != 4) {
            return null;
        }

        int position;
        try {
            position = Integer.parseInt(parts[1]);
        } catch (NumberFormatException exception) {
            return null;
        }

        String statistic = parts[2];
        PlayerTreeStatsLeaderboardService.LeaderboardPlayer topPlayer = this.leaderboardService.getTopPlayer(position, statistic);
        if (topPlayer == null) {
            return position >= 1 && position <= 10 && isSupportedStatistic(statistic)
                    && (parts.length == 3 || parts[3].equals("player"))
                    ? (parts.length == 4 ? "" : "0")
                    : null;
        }

        if (parts.length == 4) {
            return parts[3].equals("player") ? topPlayer.playerName() : null;
        }

        return switch (statistic) {
            case "trees" -> Long.toString(topPlayer.trees());
            case "logs" -> Long.toString(topPlayer.logs());
            case "leaves" -> Long.toString(topPlayer.leaves());
            case "blocks" -> Long.toString(topPlayer.blocks());
            default -> null;
        };
    }

    private static boolean isSupportedStatistic(String statistic) {
        return statistic.equals("trees")
                || statistic.equals("logs")
                || statistic.equals("leaves")
                || statistic.equals("blocks");
    }
}
