package com.songoda.ultimatetimber.integration;

import com.songoda.core.vortexcore.compatibility.folia.SchedulerUtils;
import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.config.TimberConfig;
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.annotation.component.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Optional CoreProtect integration for batched tree block-removal logging.
 */
@Component
public final class CoreProtectIntegration {

    private final Queue<PendingRemoval> pendingRemovals = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean drainScheduled = new AtomicBoolean();
    @Inject
    private TimberConfig config;
    @Inject
    private UltimateTimber plugin;
    private boolean coreProtectResolved;
    private Object coreProtectApi;
    private Method logRemovalMethod;

    public void logRemoval(@NotNull String actor, @NotNull BlockState blockState) {
        if (this.config == null || !this.config.isCoreProtectLogging()) {
            return;
        }

        this.pendingRemovals.add(new PendingRemoval(actor, blockState));
        this.scheduleDrain();
    }

    private void scheduleDrain() {
        if (!this.drainScheduled.compareAndSet(false, true)) {
            return;
        }

        SchedulerUtils.runTaskLaterAsynchronously(this.plugin, this::drain, 1L);
    }

    private void drain() {
        try {
            if (!this.resolveCoreProtect()) {
                this.pendingRemovals.clear();
                return;
            }

            PendingRemoval pendingRemoval;
            while ((pendingRemoval = this.pendingRemovals.poll()) != null) {
                try {
                    this.logRemovalMethod.invoke(this.coreProtectApi, pendingRemoval.actor(), pendingRemoval.blockState());
                } catch (IllegalAccessException | InvocationTargetException exception) {
                    this.plugin.getLogger().warning("Failed to log a tree block removal to CoreProtect: " + exception.getMessage());
                }
            }
        } finally {
            this.drainScheduled.set(false);
            if (!this.pendingRemovals.isEmpty()) {
                this.scheduleDrain();
            }
        }
    }

    private synchronized boolean resolveCoreProtect() {
        if (this.coreProtectResolved) {
            return this.logRemovalMethod != null;
        }

        this.coreProtectResolved = true;
        Plugin coreProtect = Bukkit.getPluginManager().getPlugin("CoreProtect");
        if (coreProtect == null || !coreProtect.isEnabled()) {
            return false;
        }

        try {
            Object api = coreProtect.getClass().getMethod("getAPI").invoke(coreProtect);
            int apiVersion = (int) api.getClass().getMethod("APIVersion").invoke(api);
            if (apiVersion < 10) {
                this.plugin.getLogger().warning("CoreProtect logging is enabled, but CoreProtect API version 10 or newer is required.");
                return false;
            }

            this.coreProtectApi = api;
            this.logRemovalMethod = api.getClass().getMethod("logRemoval", String.class, BlockState.class);
            return true;
        } catch (ReflectiveOperationException exception) {
            this.plugin.getLogger().warning("CoreProtect logging is enabled, but its API could not be initialized: " + exception.getMessage());
            return false;
        }
    }

    private record PendingRemoval(String actor, BlockState blockState) {
    }
}
