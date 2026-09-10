package com.songoda.ultimatetimber.manager;

import com.songoda.core.vortexcore.hooks.internal.ReloadHook;
import com.songoda.ultimatetimber.api.manager.PlacedBlockManager;
import com.songoda.ultimatetimber.config.TimberConfig;
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.annotation.component.Component;
import com.songoda.core.vortexcore.vinject.annotation.RegisterReloadHook;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Service tracking player placed blocks to avoid toppling player constructions.
 */
@Component
@RegisterReloadHook
public class PlacedBlockManagerImpl implements PlacedBlockManager, ReloadHook {

    @Inject
    private TimberConfig config;

    private Set<Location> placedBlocks;
    private boolean ignorePlacedBlocks;
    private int maxPlacedBlockMemorySize;

    public PlacedBlockManagerImpl() {
        this.placedBlocks = Collections.synchronizedSet(Collections.newSetFromMap(new LinkedHashMap<Location, Boolean>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Location, Boolean> eldest) {
                return this.size() > (maxPlacedBlockMemorySize > 0 ? maxPlacedBlockMemorySize : 5000);
            }
        }));
    }

    @Override
    public void onReload() {
        this.ignorePlacedBlocks = this.config.isIgnorePlacedBlocks();
        this.maxPlacedBlockMemorySize = this.config.getIgnorePlacedBlocksMemorySize();
        this.placedBlocks = Collections.synchronizedSet(Collections.newSetFromMap(new LinkedHashMap<Location, Boolean>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Location, Boolean> eldest) {
                return this.size() > PlacedBlockManagerImpl.this.maxPlacedBlockMemorySize;
            }
        }));
    }

    @Override
    public boolean isBlockPlaced(@NotNull Block block) {
        return this.ignorePlacedBlocks && this.placedBlocks.contains(block.getLocation());
    }

    @Override
    public void protectBlock(@NotNull Block block, boolean isPlaced) {
        if (isPlaced) {
            this.placedBlocks.add(block.getLocation());
        } else {
            this.placedBlocks.remove(block.getLocation());
        }
    }
}
