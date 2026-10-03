package com.songoda.ultimatetimber.hologram;

import com.songoda.core.vortexcore.compatibility.folia.SchedulerTask;
import com.songoda.core.vortexcore.compatibility.folia.SchedulerUtils;
import com.songoda.core.vortexcore.hooks.internal.ReloadHook;
import com.songoda.core.vortexcore.text.MiniMessagePlaceholder;
import com.songoda.core.vortexcore.text.hologram.Hologram;
import com.songoda.core.vortexcore.text.hologram.HologramManager;
import com.songoda.core.vortexcore.vinject.annotation.RegisterReloadHook;
import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.config.LeaderboardConfig;
import com.songoda.ultimatetimber.config.entry.LeaderboardPlacement;
import com.songoda.ultimatetimber.config.entry.LeaderboardTypeConfig;
import com.songoda.ultimatetimber.database.service.PlayerTreeStatsLeaderboardService;
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.annotation.component.Component;
import net.vortexdevelopment.vinject.annotation.lifecycle.OnDestroy;
import net.vortexdevelopment.vinject.annotation.lifecycle.PostConstruct;
import net.vortexdevelopment.vinject.di.ConfigurationContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates, restores, and refreshes configured player-stat leaderboard holograms.
 */
@Component
@RegisterReloadHook(priority = 1)
public final class LeaderboardHologramService implements ReloadHook {
    private static final int MAX_ENTRIES = 10;
    private static final long UPDATE_PERIOD_TICKS = 20L;

    @Inject
    private LeaderboardConfig config;

    @Inject
    private PlayerTreeStatsLeaderboardService statsService;

    @Inject
    private UltimateTimber plugin;

    private final Map<String, Hologram> holograms = new ConcurrentHashMap<>();
    private final Map<String, LeaderboardPlacement> placements = new ConcurrentHashMap<>();
    private SchedulerTask updateTask;

    @PostConstruct
    public void initialize() {
        restoreHolograms();
        this.updateTask = SchedulerUtils.runTaskTimerAsynchronously(
                this.plugin,
                this::updateHolograms,
                UPDATE_PERIOD_TICKS,
                UPDATE_PERIOD_TICKS
        );
    }

    @OnDestroy
    public void shutdown() {
        if (this.updateTask != null) {
            SchedulerUtils.cancelTask(this.updateTask);
            this.updateTask = null;
        }

        this.holograms.values().forEach(HologramManager::removeHologram);
        this.holograms.clear();
    }

    @Override
    public void onReload() {
        this.holograms.values().forEach(HologramManager::removeHologram);
        this.holograms.clear();
        restoreHolograms();
    }

    public @Nullable String spawn(@NotNull Player player, @NotNull String type) {
        if (!this.statsService.isEnabled()) {
            return null;
        }

        String normalizedType = type.toLowerCase(Locale.ROOT);
        if (!isSupportedType(normalizedType) || this.config.getTypes().get(normalizedType) == null) {
            return null;
        }
        if (this.placements.containsKey(normalizedType)) {
            return null;
        }

        Location location = player.getLocation().add(0.0, 0.5, 0.0);
        String id = normalizedType;
        LeaderboardPlacement placement = new LeaderboardPlacement();
        placement.setType(normalizedType);
        placement.setWorld(location.getWorld().getName());
        placement.setX(location.getX());
        placement.setY(location.getY());
        placement.setZ(location.getZ());
        this.config.getPlacements().put(id, placement);
        this.placements.put(id, placement);
        ConfigurationContainer.getInstance().saveConfig(LeaderboardConfig.class);

        createHologram(id, placement, location);
        return id;
    }

    public boolean remove(@NotNull String id) {
        String normalizedId = id.toLowerCase(Locale.ROOT);
        if (!isSupportedType(normalizedId)) {
            normalizedId = this.placements.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(id))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(normalizedId);
        }
        LeaderboardPlacement placement = this.placements.remove(normalizedId);
        if (placement == null) {
            return false;
        }

        this.config.getPlacements().remove(normalizedId);
        Hologram hologram = this.holograms.remove(normalizedId);
        if (hologram != null) {
            HologramManager.removeHologram(hologram);
        }
        ConfigurationContainer.getInstance().saveConfig(LeaderboardConfig.class);
        return true;
    }

    public @NotNull List<String> getTypes() {
        return List.of("blocks", "trees", "logs", "leaves");
    }

    public boolean isEnabled() {
        return this.statsService.isEnabled();
    }

    public @NotNull List<String> getPlacedTypes() {
        return List.copyOf(this.placements.keySet());
    }

    private void restoreHolograms() {
        this.placements.clear();
        Map<String, LeaderboardPlacement> normalizedPlacements = new LinkedHashMap<>();
        for (LeaderboardPlacement placement : this.config.getPlacements().values()) {
            if (placement.getType() == null) {
                continue;
            }
            String type = placement.getType().toLowerCase(Locale.ROOT);
            if (isSupportedType(type)) {
                normalizedPlacements.putIfAbsent(type, placement);
            }
        }
        boolean placementsChanged = !this.config.getPlacements().equals(normalizedPlacements);
        this.config.getPlacements().clear();
        this.config.getPlacements().putAll(normalizedPlacements);
        this.placements.putAll(normalizedPlacements);
        if (placementsChanged) {
            ConfigurationContainer.getInstance().saveConfig(LeaderboardConfig.class);
        }
        for (Map.Entry<String, LeaderboardPlacement> entry : this.placements.entrySet()) {
            LeaderboardPlacement placement = entry.getValue();
            World world = Bukkit.getWorld(placement.getWorld());
            if (world == null) {
                this.plugin.getLogger().warning("Could not restore leaderboard " + entry.getKey()
                        + " because world '" + placement.getWorld() + "' is not loaded.");
                continue;
            }

            Location location = new Location(world, placement.getX(), placement.getY(), placement.getZ());
            createHologram(entry.getKey(), placement, location);
        }
    }

    private void createHologram(String id, LeaderboardPlacement placement, Location location) {
        Hologram hologram = new Hologram(id, location, renderLines(placement.getType()));
        this.holograms.put(id, hologram);
        HologramManager.createHologram(hologram);
    }

    private void updateHolograms() {
        for (Map.Entry<String, Hologram> entry : this.holograms.entrySet()) {
            LeaderboardPlacement placement = this.placements.get(entry.getKey());
            if (placement == null) {
                continue;
            }

            Hologram hologram = entry.getValue();
            if (hologram.setLinesIfChanged(renderLines(placement.getType()))) {
                hologram.update();
            }
        }
    }

    private List<String> renderLines(String type) {
        LeaderboardTypeConfig typeConfig = this.config.getTypes().get(type);
        if (typeConfig == null) {
            return List.of();
        }

        List<String> lines = new ArrayList<>();
        lines.add(typeConfig.getTitle() == null ? "" : typeConfig.getTitle());
        if (!this.statsService.isEnabled()) {
            return List.copyOf(lines);
        }

        List<String> lineFormats = typeConfig.getLines() == null ? List.of() : typeConfig.getLines();
        int entries = Math.min(MAX_ENTRIES, lineFormats.size());
        for (int position = 1; position <= entries; position++) {
            String lineFormat = lineFormats.get(position - 1);
            PlayerTreeStatsLeaderboardService.LeaderboardPlayer player = this.statsService.getTopPlayer(position, type);
            long trees = player == null ? 0L : player.trees();
            long logs = player == null ? 0L : player.logs();
            long leaves = player == null ? 0L : player.leaves();
            long blocks = logs + leaves;
            long value = switch (type) {
                case "blocks" -> blocks;
                case "trees" -> trees;
                case "logs" -> logs;
                case "leaves" -> leaves;
                default -> 0L;
            };

            String line = lineFormat;
            for (MiniMessagePlaceholder placeholder : List.of(
                    new MiniMessagePlaceholder("rank", position),
                    new MiniMessagePlaceholder("position", position),
                    new MiniMessagePlaceholder("player", player == null ? "No players yet" : player.playerName()),
                    new MiniMessagePlaceholder("value", value),
                    new MiniMessagePlaceholder("blocks", blocks),
                    new MiniMessagePlaceholder("trees", trees),
                    new MiniMessagePlaceholder("logs", logs),
                    new MiniMessagePlaceholder("leaves", leaves)
            )) {
                line = placeholder.replace(line);
            }
            lines.add(line);
        }
        return List.copyOf(lines);
    }

    private static boolean isSupportedType(String type) {
        return type.equals("blocks") || type.equals("trees") || type.equals("logs") || type.equals("leaves");
    }
}
