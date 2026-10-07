package com.songoda.ultimatetimber.database.service;

import com.songoda.core.compatibility.folia.SchedulerTask;
import com.songoda.core.compatibility.folia.SchedulerUtils;
import com.songoda.core.hooks.internal.ReloadHook;
import com.songoda.core.vinject.annotation.RegisterReloadHook;
import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.database.entity.PlayerTreeStats;
import com.songoda.ultimatetimber.database.repository.PlayerTreeStatsRepository;
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.annotation.component.Component;
import net.vortexdevelopment.vinject.annotation.lifecycle.OnDestroy;
import net.vortexdevelopment.vinject.annotation.lifecycle.PostConstruct;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 * Periodically loads SQL-sorted top player lists and serves them from memory.
 */
@Component
@RegisterReloadHook
public final class PlayerTreeStatsLeaderboardService implements ReloadHook {
    private static final int LEADERBOARD_SIZE = 10;
    private static final String TOTAL_BLOCKS_QUERY = "SELECT * FROM player_tree_stats ORDER BY (logs_chopped + leaves_chopped) DESC LIMIT 10";

    @Inject
    private TimberConfig config;

    @Inject
    private PlayerTreeStatsRepository repository;

    @Inject
    private UltimateTimber plugin;

    private final AtomicReference<LeaderboardCache> leaderboard = new AtomicReference<>(LeaderboardCache.EMPTY);
    private final AtomicBoolean refreshInProgress = new AtomicBoolean();
    private final AtomicBoolean refreshPending = new AtomicBoolean();
    private final AtomicLong refreshGeneration = new AtomicLong();
    private SchedulerTask refreshTask;

    @PostConstruct
    public void initialize() {
        onReload();
    }

    @OnDestroy
    public void shutdown() {
        this.refreshGeneration.incrementAndGet();
        cancelRefreshTask();
        this.leaderboard.set(LeaderboardCache.EMPTY);
    }

    @Override
    public void onReload() {
        long generation = this.refreshGeneration.incrementAndGet();
        cancelRefreshTask();

        if (!this.config.getStatistics().isEnabled()) {
            this.leaderboard.set(LeaderboardCache.EMPTY);
            return;
        }

        int refreshSeconds = Math.max(1, this.config.getStatistics().getTopPlayerRefresh());
        long periodTicks = (long) refreshSeconds * 20L;
        SchedulerUtils.runTaskAsynchronously(this.plugin, () -> refresh(generation));
        this.refreshTask = SchedulerUtils.runTaskTimerAsynchronously(
                this.plugin,
                () -> refresh(generation),
                periodTicks,
                periodTicks
        );
    }

    public boolean isEnabled() {
        return this.config.getStatistics().isEnabled();
    }

    public void requestRefresh() {
        if (isEnabled()) {
            long generation = this.refreshGeneration.get();
            if (this.refreshInProgress.get()) {
                this.refreshPending.set(true);
            } else {
                SchedulerUtils.runTaskAsynchronously(this.plugin, () -> refresh(generation));
            }
        }
    }

    public @Nullable LeaderboardPlayer getTopPlayer(int position, String statistic) {
        if (!isEnabled() || position < 1 || position > LEADERBOARD_SIZE) {
            return null;
        }

        List<LeaderboardPlayer> players = switch (statistic) {
            case "trees" -> this.leaderboard.get().trees();
            case "logs" -> this.leaderboard.get().logs();
            case "leaves" -> this.leaderboard.get().leaves();
            case "blocks" -> this.leaderboard.get().blocks();
            default -> null;
        };
        if (players == null || players.size() < position) {
            return null;
        }

        return players.get(position - 1);
    }

    private void refresh(long generation) {
        if (generation != this.refreshGeneration.get() || !isEnabled() || !this.refreshInProgress.compareAndSet(false, true)) {
            return;
        }

        try {
            List<LeaderboardPlayer> trees = snapshot(this.repository.findTop10ByOrderByTriggersDesc(), Statistic.TREES);
            List<LeaderboardPlayer> logs = snapshot(this.repository.findTop10ByOrderByLogsChoppedDesc(), Statistic.LOGS);
            List<LeaderboardPlayer> leaves = snapshot(this.repository.findTop10ByOrderByLeavesChoppedDesc(), Statistic.LEAVES);
            PlayerTreeStats[] blockRows = this.repository.query(TOTAL_BLOCKS_QUERY, PlayerTreeStats[].class);
            List<LeaderboardPlayer> blocks = snapshot(blockRows == null ? List.of() : List.of(blockRows), Statistic.BLOCKS);

            if (generation == this.refreshGeneration.get() && isEnabled()) {
                this.leaderboard.set(new LeaderboardCache(trees, logs, leaves, blocks));
            }
        } catch (RuntimeException exception) {
            if (generation == this.refreshGeneration.get()) {
                this.plugin.getLogger().log(Level.WARNING, "Could not refresh the cached tree statistics leaderboards.", exception);
            }
        } finally {
            this.refreshInProgress.set(false);
            if (this.refreshPending.getAndSet(false) && generation == this.refreshGeneration.get()) {
                refresh(generation);
            }
        }
    }

    private static List<LeaderboardPlayer> snapshot(Iterable<PlayerTreeStats> entries, Statistic statistic) {
        List<LeaderboardPlayer> players = new ArrayList<>(LEADERBOARD_SIZE);
        for (PlayerTreeStats stats : entries) {
            synchronized (stats) {
                long trees = value(stats.getTriggers());
                long logs = value(stats.getLogsChopped());
                long leaves = value(stats.getLeavesChopped());
                long total = switch (statistic) {
                    case TREES -> trees;
                    case LOGS -> logs;
                    case LEAVES -> leaves;
                    case BLOCKS -> logs + leaves;
                };
                if (total == 0L) {
                    continue;
                }

                String playerName = stats.getPlayerName();
                players.add(new LeaderboardPlayer(
                        playerName == null || playerName.isBlank() ? "Unknown" : playerName,
                        trees,
                        logs,
                        leaves
                ));
            }
        }
        return List.copyOf(players);
    }

    private static long value(Long value) {
        return value == null ? 0L : value;
    }

    private void cancelRefreshTask() {
        if (this.refreshTask != null) {
            SchedulerUtils.cancelTask(this.refreshTask);
            this.refreshTask = null;
        }
    }

    public record LeaderboardPlayer(String playerName, long trees, long logs, long leaves) {
        public long blocks() {
            return this.logs + this.leaves;
        }
    }

    private enum Statistic {
        TREES,
        LOGS,
        LEAVES,
        BLOCKS
    }

    private record LeaderboardCache(
            List<LeaderboardPlayer> trees,
            List<LeaderboardPlayer> logs,
            List<LeaderboardPlayer> leaves,
            List<LeaderboardPlayer> blocks
    ) {
        private static final LeaderboardCache EMPTY = new LeaderboardCache(List.of(), List.of(), List.of(), List.of());
    }
}
