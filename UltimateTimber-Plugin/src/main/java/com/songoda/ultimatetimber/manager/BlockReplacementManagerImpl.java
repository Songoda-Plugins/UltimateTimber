package com.songoda.ultimatetimber.manager;

import com.songoda.core.vortexcore.hooks.internal.ReloadHook;
import com.songoda.core.vortexcore.vinject.annotation.RegisterReloadHook;
import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.api.manager.BlockReplacementManager;
import com.songoda.ultimatetimber.api.manager.SaplingManager;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import com.songoda.ultimatetimber.config.TimberConfig;
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.annotation.component.Component;
import net.vortexdevelopment.vinject.annotation.lifecycle.OnDestroy;
import net.vortexdevelopment.vinject.annotation.lifecycle.PostConstruct;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Service managing queued or instant tree block replacement and sapling replanting.
 */
@Component
@RegisterReloadHook
public class BlockReplacementManagerImpl implements BlockReplacementManager, ReloadHook, Runnable {

    @Inject
    private TimberConfig config;

    @Inject
    private SaplingManager saplingManager;

    private final Deque<QueuedBlockReplacement> queue = new ArrayDeque<>();
    private String mode = "NEVER";
    private int playerThreshold = 20;
    private int maxPerTick = 1000;
    private BukkitTask task;

    @PostConstruct
    public void initialize() {
        onReload();
        UltimateTimber plugin = UltimateTimber.getInstance();
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, 1L);
    }

    @OnDestroy
    public void onDestroy() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
        processAll();
    }

    @Override
    public void onReload() {
        if (this.config != null && this.config.getQueuedBlockReplacement() != null) {
            this.mode = this.config.getQueuedBlockReplacement().getMode();
            this.playerThreshold = this.config.getQueuedBlockReplacement().getThreshold();
            this.maxPerTick = this.config.getQueuedBlockReplacement().getMaxPerTick();
        }
    }

    @Override
    public void replaceBlock(@NotNull TreeBlock<Block> treeBlock, @NotNull TreeDefinition treeDefinition) {
        if (isQueuingActive()) {
            this.queue.add(new QueuedBlockReplacement(treeBlock, treeDefinition));
        } else {
            executeReplacement(new QueuedBlockReplacement(treeBlock, treeDefinition));
        }
    }

    @Override
    public void processAll() {
        while (!this.queue.isEmpty()) {
            executeReplacement(this.queue.poll());
        }
    }

    @Override
    public void run() {
        if (this.queue.isEmpty()) {
            return;
        }

        int processed = 0;
        while (!this.queue.isEmpty() && processed < this.maxPerTick) {
            QueuedBlockReplacement entry = this.queue.poll();
            if (entry != null) {
                executeReplacement(entry);
                processed++;
            }
        }
    }

    private boolean isQueuingActive() {
        if ("ALWAYS".equalsIgnoreCase(this.mode)) {
            return true;
        } else if ("DYNAMIC".equalsIgnoreCase(this.mode)) {
            return Bukkit.getOnlinePlayers().size() >= this.playerThreshold;
        }
        return false;
    }

    private void executeReplacement(QueuedBlockReplacement entry) {
        entry.treeBlock.getBlock().setType(Material.AIR);
        if (this.saplingManager != null) {
            this.saplingManager.replantSapling(entry.treeDefinition, entry.treeBlock);
        }
    }

    private record QueuedBlockReplacement(TreeBlock<Block> treeBlock, TreeDefinition treeDefinition) {
    }
}
