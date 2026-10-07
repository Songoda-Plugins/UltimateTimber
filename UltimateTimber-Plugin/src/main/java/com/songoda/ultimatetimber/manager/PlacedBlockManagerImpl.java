package com.songoda.ultimatetimber.manager;

import com.songoda.core.hooks.internal.ReloadHook;
import com.songoda.core.vinject.annotation.RegisterReloadHook;
import com.songoda.ultimatetimber.api.manager.PlacedBlockManager;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.utils.LongSet;
import com.songoda.ultimatetimber.utils.TreeGeometry;
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.annotation.component.Component;
import net.vortexdevelopment.vinject.annotation.lifecycle.PostConstruct;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Service tracking player placed blocks to avoid toppling player constructions.
 *
 * <p>Blocks are stored as packed coordinate keys so probing this repository allocates nothing, and
 * entries are evicted in insertion order once the configured memory size is reached. Tracked blocks
 * survive reloads, only the memory bound is reapplied.
 */
@Component
@RegisterReloadHook
public class PlacedBlockManagerImpl implements PlacedBlockManager, ReloadHook {

    private static final int DEFAULT_MEMORY_SIZE = 5000;
    private final LongSet placedBlocks = new LongSet();
    @Inject
    private TimberConfig config;
    private long[] insertionOrder = new long[DEFAULT_MEMORY_SIZE];
    private int insertionCursor;
    private int insertionCount;

    private boolean ignorePlacedBlocks;
    private int maxPlacedBlockMemorySize = DEFAULT_MEMORY_SIZE;

    @PostConstruct
    public void initialize() {
        onReload();
    }

    @Override
    public synchronized void onReload() {
        this.ignorePlacedBlocks = this.config.isIgnorePlacedBlocks();
        this.maxPlacedBlockMemorySize = Math.max(1, this.config.getIgnorePlacedBlocksMemorySize());

        if (this.insertionOrder.length != this.maxPlacedBlockMemorySize) {
            this.insertionOrder = new long[this.maxPlacedBlockMemorySize];
            this.insertionCursor = 0;
            this.insertionCount = 0;

            // Insertion order is lost when the bound changes, so drop the excess instead of keeping
            // entries that can no longer be evicted in order.
            if (this.placedBlocks.size() > this.maxPlacedBlockMemorySize) {
                this.placedBlocks.clear();
            }
        }
    }

    @Override
    public synchronized boolean isBlockPlaced(@NotNull Block block) {
        return this.ignorePlacedBlocks && this.placedBlocks.contains(TreeGeometry.packBlockKey(block));
    }

    @Override
    public synchronized void protectBlock(@NotNull Block block, boolean isPlaced) {
        long blockKey = TreeGeometry.packBlockKey(block);
        if (!isPlaced) {
            this.placedBlocks.remove(blockKey);
            return;
        }

        if (!this.placedBlocks.add(blockKey)) {
            return;
        }

        this.insertionOrder[this.insertionCursor] = blockKey;
        this.insertionCursor = (this.insertionCursor + 1) % this.insertionOrder.length;
        if (this.insertionCount < this.insertionOrder.length) {
            this.insertionCount++;
        }
        evictOverflow();
    }

    private void evictOverflow() {
        while (this.placedBlocks.size() > this.maxPlacedBlockMemorySize && this.insertionCount > 0) {
            long eldest = this.insertionOrder[this.insertionCursor];
            this.insertionCursor = (this.insertionCursor + 1) % this.insertionOrder.length;
            this.insertionCount--;
            this.placedBlocks.remove(eldest);
        }
    }
}
