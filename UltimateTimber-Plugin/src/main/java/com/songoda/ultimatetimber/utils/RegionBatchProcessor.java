package com.songoda.ultimatetimber.utils;

import com.songoda.core.vortexcore.compatibility.folia.SchedulerUtils;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Runs a collection of location-bound operations in batches on the regions that own them.
 *
 * <p>Values owned by the current region are processed inline; other values are scheduled on their
 * owning regions in batches. Region ownership is checked before touching each value so region
 * merges and splits remain safe.
 */
public final class RegionBatchProcessor {

    private RegionBatchProcessor() {
    }

    public static <T> void processByRegion(@NotNull Plugin plugin,
                                           @NotNull Collection<T> values,
                                           @NotNull Function<T, Location> locationProvider,
                                           @NotNull Consumer<T> action,
                                           @NotNull Runnable whenComplete) {
        List<RegionValue<T>> regionValues = new ArrayList<>(values.size());
        for (T value : values) {
            regionValues.add(new RegionValue<>(value, locationProvider.apply(value).clone()));
        }

        new RegionBatch<>(plugin, regionValues, action, whenComplete).scheduleNextRegion();
    }

    private static final class RegionBatch<T> implements Runnable {

        private final Plugin plugin;
        private final List<RegionValue<T>> remaining;
        private final Consumer<T> action;
        private final Runnable whenComplete;
        private boolean completed;

        private RegionBatch(@NotNull Plugin plugin,
                            @NotNull List<RegionValue<T>> values,
                            @NotNull Consumer<T> action,
                            @NotNull Runnable whenComplete) {
            this.plugin = plugin;
            this.remaining = values;
            this.action = action;
            this.whenComplete = whenComplete;
        }

        @Override
        public synchronized void run() {
            try {
                Iterator<RegionValue<T>> iterator = this.remaining.iterator();
                while (iterator.hasNext()) {
                    RegionValue<T> regionValue = iterator.next();
                    if (!SchedulerUtils.isOwnedByCurrentRegion(regionValue.location())) {
                        continue;
                    }

                    iterator.remove();
                    this.action.accept(regionValue.value());
                }
            } finally {
                this.scheduleNextRegion();
            }
        }

        private synchronized void scheduleNextRegion() {
            if (this.completed) {
                return;
            }

            if (this.remaining.isEmpty()) {
                this.completed = true;
                this.whenComplete.run();
                return;
            }

            Location nextLocation = this.remaining.get(0).location();
            if (SchedulerUtils.isFolia() && SchedulerUtils.isOwnedByCurrentRegion(nextLocation)) {
                this.run();
                return;
            }

            SchedulerUtils.runLocationTask(this.plugin, nextLocation, this);
        }
    }

    private record RegionValue<T>(T value, Location location) {
    }
}
