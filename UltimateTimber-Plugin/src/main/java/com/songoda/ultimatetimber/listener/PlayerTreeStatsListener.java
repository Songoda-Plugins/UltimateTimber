package com.songoda.ultimatetimber.listener;

import com.songoda.core.vinject.annotation.RegisterListener;
import com.songoda.core.vinject.database.cache.DefaultCacheKeys;
import com.songoda.ultimatetimber.api.event.TreeFellEvent;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.database.entity.PlayerTreeStats;
import com.songoda.ultimatetimber.database.repository.PlayerTreeStatsRepository;
import com.songoda.ultimatetimber.database.service.PlayerTreeStatsLeaderboardService;
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.database.cache.CacheCoordinator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Loads online player stats into SongodaCore's pinned cache and records completed chops.
 */
@RegisterListener
public final class PlayerTreeStatsListener implements Listener {
    private static final String ONLINE_PIN_REASON = "ultimatetimber-online-player";

    private final ConcurrentMap<UUID, PlayerTreeStats> onlineStats = new ConcurrentHashMap<>();

    @Inject
    private CacheCoordinator cacheCoordinator;

    @Inject
    private PlayerTreeStatsRepository repository;

    @Inject
    private PlayerTreeStatsLeaderboardService leaderboardService;

    @Inject
    private TimberConfig config;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!this.config.getStatistics().isEnabled()) {
            return;
        }

        UUID playerUuid = event.getPlayer().getUniqueId();
        this.cacheCoordinator.load(DefaultCacheKeys.PLAYER_UUID, playerUuid);

        PlayerTreeStats stats = this.repository.findById(playerUuid);
        if (stats != null) {
            synchronized (stats) {
                stats.setPlayerName(event.getPlayer().getName());
                this.repository.save(stats);
            }
            this.repository.pin(stats, ONLINE_PIN_REASON);
            this.onlineStats.put(playerUuid, stats);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUuid = event.getPlayer().getUniqueId();
        PlayerTreeStats stats = this.onlineStats.remove(playerUuid);
        if (stats != null) {
            this.repository.unpin(stats, ONLINE_PIN_REASON);
        }

        this.cacheCoordinator.unload(DefaultCacheKeys.PLAYER_UUID, playerUuid);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTreeFell(TreeFellEvent event) {
        if (!this.config.getStatistics().isEnabled()) {
            return;
        }

        UUID playerUuid = event.getPlayer().getUniqueId();
        PlayerTreeStats stats = this.onlineStats.get(playerUuid);
        if (stats == null) {
            stats = this.repository.findById(playerUuid);
            if (stats == null) {
                stats = this.repository.save(new PlayerTreeStats(playerUuid));
            }
            this.repository.pin(stats, ONLINE_PIN_REASON);
            this.onlineStats.put(playerUuid, stats);
        }

        long logsChopped = event.getDetectedTree().detectedTreeBlocks().getLogBlocks().size();
        TreeBlock<?> initialLog = event.getDetectedTree().detectedTreeBlocks().getInitialLogBlock();
        if (initialLog != null && !event.getDetectedTree().detectedTreeBlocks().contains(initialLog)) {
            logsChopped++;
        }
        long leavesChopped = event.getDetectedTree().detectedTreeBlocks().getLeafBlocks().size();

        synchronized (stats) {
            stats.setPlayerName(event.getPlayer().getName());
            stats.setTriggers(stats.getTriggers() + 1L);
            stats.setLogsChopped(stats.getLogsChopped() + logsChopped);
            stats.setLeavesChopped(stats.getLeavesChopped() + leavesChopped);
            this.repository.save(stats);
        }
        this.leaderboardService.requestRefresh();
    }

    public @NotNull PlayerTreeStatsSnapshot getStats(@NotNull UUID playerUuid) {
        PlayerTreeStats stats = this.onlineStats.get(playerUuid);
        if (stats == null) {
            stats = this.repository.findById(playerUuid);
        }
        if (stats == null) {
            return PlayerTreeStatsSnapshot.EMPTY;
        }

        synchronized (stats) {
            return new PlayerTreeStatsSnapshot(stats.getTriggers(), stats.getLogsChopped(), stats.getLeavesChopped());
        }
    }

    public record PlayerTreeStatsSnapshot(long triggers, long logsChopped, long leavesChopped) {
        private static final PlayerTreeStatsSnapshot EMPTY = new PlayerTreeStatsSnapshot(0L, 0L, 0L);

        public long totalBlocksChopped() {
            return this.logsChopped + this.leavesChopped;
        }
    }
}
