package com.songoda.ultimatetimber.manager;

import com.songoda.core.vortexcore.compatibility.folia.SchedulerTask;
import com.songoda.core.vortexcore.compatibility.folia.SchedulerUtils;
import com.songoda.core.vortexcore.hooks.internal.ReloadHook;
import com.songoda.core.vortexcore.vinject.annotation.RegisterReloadHook;
import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.api.manager.BlockReplacementManager;
import com.songoda.ultimatetimber.api.manager.SaplingManager;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.integration.CoreProtectIntegration;
import com.songoda.ultimatetimber.utils.RegionBatchProcessor;
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.annotation.component.Component;
import net.vortexdevelopment.vinject.annotation.lifecycle.OnDestroy;
import net.vortexdevelopment.vinject.annotation.lifecycle.PostConstruct;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service managing queued or instant tree block replacement and sapling replanting.
 */
@Component
@RegisterReloadHook
public class BlockReplacementManagerImpl implements BlockReplacementManager, ReloadHook, Runnable {

    private final Queue<QueuedBlockReplacement> queue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean batchInProgress = new AtomicBoolean();
    @Inject
    private TimberConfig config;
    @Inject
    private SaplingManager saplingManager;
    @Inject
    private CoreProtectIntegration coreProtectIntegration;
    @Inject
    private UltimateTimber plugin;
    private volatile ReplacementMode mode = ReplacementMode.NEVER;
    private volatile int playerThreshold = 20;
    private volatile int maxPerTick = 1000;
    private volatile int onlinePlayerCount;
    private SchedulerTask task;

    @PostConstruct
    public void initialize() {
        onReload();
        this.task = SchedulerUtils.runTaskTimer(plugin, this, 0L, 1L);
    }

    @OnDestroy
    public void onDestroy() {
        if (this.task != null) {
            SchedulerUtils.cancelTask(this.task);
            this.task = null;
        }
        processAll();
    }

    @Override
    public void onReload() {
        this.mode = ReplacementMode.from(this.config.getQueuedBlockReplacement().getMode());
        this.playerThreshold = this.config.getQueuedBlockReplacement().getThreshold();
        this.maxPerTick = this.config.getQueuedBlockReplacement().getMaxPerTick();
    }

    @Override
    public void replaceBlock(@NotNull TreeBlock<Block> treeBlock, @NotNull TreeDefinition treeDefinition) {
        this.replaceBlock(treeBlock, treeDefinition, "UltimateTimber");
    }

    @Override
    public void replaceBlock(@NotNull TreeBlock<Block> treeBlock,
                             @NotNull TreeDefinition treeDefinition,
                             @NotNull String actor) {
        if (isQueuingActive()) {
            this.queue.add(new QueuedBlockReplacement(treeBlock, treeDefinition, actor));
        } else {
            executeReplacement(new QueuedBlockReplacement(treeBlock, treeDefinition, actor));
        }
    }

    @Override
    public void processAll() {
        if (!this.batchInProgress.compareAndSet(false, true)) {
            return;
        }

        List<QueuedBlockReplacement> replacements = new ArrayList<>();
        QueuedBlockReplacement replacement;
        while ((replacement = this.queue.poll()) != null) {
            replacements.add(replacement);
        }

        this.processReplacements(replacements);
    }

    @Override
    public void run() {
        this.onlinePlayerCount = Bukkit.getOnlinePlayers().size();
        if (!this.batchInProgress.compareAndSet(false, true)) {
            return;
        }

        List<QueuedBlockReplacement> replacements = new ArrayList<>(Math.max(0, this.maxPerTick));
        for (int processed = 0; processed < this.maxPerTick; processed++) {
            QueuedBlockReplacement replacement = this.queue.poll();
            if (replacement == null) {
                break;
            }
            replacements.add(replacement);
        }

        this.processReplacements(replacements);
    }

    private void processReplacements(@NotNull List<QueuedBlockReplacement> replacements) {
        if (replacements.isEmpty()) {
            this.batchInProgress.set(false);
            return;
        }

        RegionBatchProcessor.processByRegion(
                plugin,
                replacements,
                replacement -> replacement.treeBlock().getLocation(),
                this::executeReplacement,
                () -> this.batchInProgress.set(false)
        );
    }

    private boolean isQueuingActive() {
        return switch (this.mode) {
            case NEVER -> false;
            case ALWAYS -> true;
            case DYNAMIC -> this.onlinePlayerCount >= this.playerThreshold;
        };
    }

    private void executeReplacement(QueuedBlockReplacement entry) {
        Location location = entry.treeBlock().getLocation();
        if (!SchedulerUtils.isOwnedByCurrentRegion(location)) {
            SchedulerUtils.runLocationTask(plugin, location, () -> this.executeReplacement(entry));
            return;
        }

        Block block = entry.treeBlock.block();
        if (this.coreProtectIntegration != null) {
            this.coreProtectIntegration.logRemoval(entry.actor(), block.getState());
        }
        block.setType(Material.AIR);
        if (this.saplingManager != null) {
            this.saplingManager.replantSapling(entry.treeDefinition, entry.treeBlock);
        }
    }

    private enum ReplacementMode {
        NEVER,
        ALWAYS,
        DYNAMIC;

        private static ReplacementMode from(String value) {
            if (value == null) {
                return NEVER;
            }

            try {
                return valueOf(value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return NEVER;
            }
        }
    }

    private record QueuedBlockReplacement(TreeBlock<Block> treeBlock, TreeDefinition treeDefinition, String actor) {
    }
}
