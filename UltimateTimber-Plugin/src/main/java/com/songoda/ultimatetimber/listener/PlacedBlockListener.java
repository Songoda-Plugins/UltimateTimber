package com.songoda.ultimatetimber.listener;

import com.songoda.core.vortexcore.vinject.annotation.RegisterListener;
import com.songoda.ultimatetimber.api.event.TreeFellEvent;
import com.songoda.ultimatetimber.api.manager.PlacedBlockManager;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import net.vortexdevelopment.vinject.annotation.Inject;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.world.StructureGrowEvent;

/**
 * Event listener tracking player block placement, breaking, decay, and tree felling.
 */
@RegisterListener
public class PlacedBlockListener implements Listener {

    @Inject
    private PlacedBlockManager placedBlockManager;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placed = event.getBlockPlaced();
        if (placed.getType().name().contains("STRIPPED") && !event.getBlockReplacedState().getType().isAir()) {
            return;
        }

        this.placedBlockManager.protectBlock(placed, true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        this.placedBlockManager.protectBlock(event.getBlock(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeafDecay(LeavesDecayEvent event) {
        this.placedBlockManager.protectBlock(event.getBlock(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        for (BlockState blockState : event.getBlocks()) {
            this.placedBlockManager.protectBlock(blockState.getBlock(), false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTreeFell(TreeFellEvent event) {
        for (TreeBlock<?> treeBlock : event.getDetectedTree().getDetectedTreeBlocks().getAllTreeBlocks()) {
            if (treeBlock.getBlock() instanceof Block block) {
                this.placedBlockManager.protectBlock(block, false);
            }
        }
    }
}
