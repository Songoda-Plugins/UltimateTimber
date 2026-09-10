package com.songoda.ultimatetimber.listener;

import com.songoda.core.vortexcore.vinject.annotation.RegisterListener;
import com.songoda.ultimatetimber.api.event.TreeFallEvent;
import com.songoda.ultimatetimber.api.manager.SaplingManager;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.manager.TreeDetectionManager;
import com.songoda.ultimatetimber.api.manager.TreeFallManager;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.manager.TreeFallManagerImpl;
import net.vortexdevelopment.vinject.annotation.Inject;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Event listener intercepting block break events to initiate tree toppling.
 */
@RegisterListener
public class TreeFallListener implements Listener {

    @Inject
    private TimberConfig config;

    @Inject
    private TreeFallManager treeFallManager;

    @Inject
    private TreeDetectionManager treeDetectionManager;

    @Inject
    private TreeDefinitionManager treeDefinitionManager;

    @Inject
    private SaplingManager saplingManager;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (this.saplingManager.isSaplingProtected(block)) {
            event.setCancelled(true);
            return;
        }

        boolean canTopple = this.treeFallManager.canTopple(player, block, tool);
        boolean alwaysReplant = this.config != null && this.config.isAlwaysReplantSapling();

        if (!canTopple && !alwaysReplant) {
            return;
        }

        DetectedTree detectedTree = this.treeDetectionManager.detectTree(block);
        if (detectedTree == null) {
            return;
        }

        if (alwaysReplant) {
            if (detectedTree.getDetectedTreeBlocks().getInitialLogBlock() != null) {
                this.saplingManager.replantSapling(
                        detectedTree.getTreeDefinition(),
                        detectedTree.getDetectedTreeBlocks().getInitialLogBlock()
                );
            }

            if (!canTopple) {
                return;
            }
        }

        if (!this.treeDefinitionManager.isToolValidForTreeDefinition(detectedTree.getTreeDefinition(), tool)) {
            return;
        }

        if (this.config != null && this.config.isProtectTool() && this.treeFallManager instanceof TreeFallManagerImpl fallImpl) {
            boolean hasSilkTouch = tool.hasItemMeta() && tool.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH);
            short damage = fallImpl.getToolDamage(detectedTree.getDetectedTreeBlocks(), hasSilkTouch);
            if (fallImpl.wouldToolBreak(tool, damage)) {
                return;
            }
        }

        TreeFallEvent treeFallEvent = new TreeFallEvent(player, detectedTree);
        Bukkit.getPluginManager().callEvent(treeFallEvent);
        if (treeFallEvent.isCancelled()) {
            return;
        }

        event.setCancelled(true);
        this.treeFallManager.toppleTree(player, detectedTree, tool);
    }
}
