package com.songoda.ultimatetimber.listener;

import com.songoda.core.SongodaPlugin;
import com.songoda.core.vortexcore.compatibility.EnchantmentResolver;
import com.songoda.core.vortexcore.vinject.annotation.RegisterListener;
import com.songoda.core.vortexcore.compatibility.folia.SchedulerUtils;
import com.songoda.ultimatetimber.api.UltimateTimberApi;
import com.songoda.ultimatetimber.api.event.TreeFallEvent;
import com.songoda.ultimatetimber.api.manager.SaplingManager;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.manager.TreeDetectionManager;
import com.songoda.ultimatetimber.api.manager.TreeFallManager;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockSet;
import com.songoda.ultimatetimber.tree.DetectedTreeImpl;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.manager.TreeFallManagerImpl;
import net.vortexdevelopment.vinject.annotation.Inject;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
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

        if (cancelProtectedSaplingBreak(event, block)) {
            return;
        }

        if (SchedulerUtils.isFolia()) {
            handleFoliaBlockBreak(player, block);
            return;
        }

        DetectedTree detectedTree = prepareTreeForToppling(player, block, tool);
        if (detectedTree == null || !isToolValidForToppling(detectedTree, tool)) {
            return;
        }

        if (!isTreeFallAllowed(player, detectedTree)) {
            return;
        }

        event.setCancelled(true);
        this.treeFallManager.toppleTree(player, detectedTree, tool);
    }

    private void handleFoliaBlockBreak(Player player, Block block) {
        ItemStack tool = player.getInventory().getItemInMainHand();
        boolean canTopple = this.treeFallManager.canTopple(player, block, tool);
        boolean alwaysReplant = this.config != null && this.config.isAlwaysReplantSapling();
        if (!canTopple && !alwaysReplant) {
            return;
        }

        this.treeDetectionManager.detectTreeAsync(block, detectedTree -> {
            if (detectedTree == null) {
                return;
            }

            SchedulerUtils.runEntityTask(SongodaPlugin.getInstance(), player,
                    () -> this.finishFoliaTreeBreak(player, canTopple, alwaysReplant, detectedTree));
        });
    }

    private void finishFoliaTreeBreak(Player player,
                                     boolean canToppleAtBreak,
                                     boolean alwaysReplant,
                                     DetectedTree detectedTree) {
        if (alwaysReplant) {
            replantInitialLog(detectedTree);
        }
        if (!canToppleAtBreak) {
            return;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();
        DetectedTree remainingTree = withoutInitialLog(detectedTree);
        if (remainingTree == null || !isToolValidForToppling(remainingTree, tool)) {
            return;
        }

        if (!isTreeFallAllowed(player, remainingTree)) {
            return;
        }

        if (!alwaysReplant) {
            replantInitialLog(detectedTree);
        }

        this.treeFallManager.toppleTree(player, remainingTree, tool);
    }

    private DetectedTree withoutInitialLog(DetectedTree detectedTree) {
        TreeBlock<Block> initialLog = detectedTree.detectedTreeBlocks().getInitialLogBlock();
        if (initialLog == null) {
            return detectedTree;
        }

        // Preserve the chopped log as the origin for animation direction and full-tree loot.
        TreeBlockSet<Block> remainingBlocks = new TreeBlockSet<>(initialLog);
        remainingBlocks.remove(initialLog);
        for (TreeBlock<Block> treeBlock : detectedTree.detectedTreeBlocks().getAllTreeBlocks()) {
            if (treeBlock != initialLog) {
                remainingBlocks.add(treeBlock);
            }
        }
        return new DetectedTreeImpl(detectedTree.treeDefinition(), remainingBlocks);
    }

    private boolean cancelProtectedSaplingBreak(BlockBreakEvent event, Block block) {
        if (!this.saplingManager.isSaplingProtected(block)) {
            return false;
        }

        event.setCancelled(true);
        return true;
    }

    private DetectedTree prepareTreeForToppling(Player player, Block block, ItemStack tool) {
        boolean canTopple = this.treeFallManager.canTopple(player, block, tool);
        boolean alwaysReplant = this.config != null && this.config.isAlwaysReplantSapling();
        if (!canTopple && !alwaysReplant) {
            return null;
        }

        DetectedTree detectedTree = this.treeDetectionManager.detectTree(block);
        if (detectedTree == null) {
            return null;
        }

        if (alwaysReplant) {
            replantInitialLog(detectedTree);
        }

        return canTopple ? detectedTree : null;
    }

    private void replantInitialLog(DetectedTree detectedTree) {
        TreeBlock<?> initialLogBlock = detectedTree.detectedTreeBlocks().getInitialLogBlock();
        if (initialLogBlock == null) {
            return;
        }

        this.saplingManager.replantSapling(detectedTree.treeDefinition(), initialLogBlock);
    }

    private boolean isToolValidForToppling(DetectedTree detectedTree, ItemStack tool) {
        if (!this.treeDefinitionManager.isToolValidForTreeDefinition(detectedTree.treeDefinition(), tool)) {
            return false;
        }

        if (this.config == null
                || !this.config.isProtectTool()
                || !(this.treeFallManager instanceof TreeFallManagerImpl fallImpl)) {
            return true;
        }

        boolean hasSilkTouch = tool.hasItemMeta() && tool.getItemMeta().hasEnchant(EnchantmentResolver.resolve(NamespacedKey.minecraft("silk_touch")));
        short damage = fallImpl.getToolDamage(detectedTree.detectedTreeBlocks(), hasSilkTouch);
        return !fallImpl.wouldToolBreak(tool, damage);
    }

    private boolean isTreeFallAllowed(Player player, DetectedTree detectedTree) {
        TreeFallEvent treeFallEvent = new TreeFallEvent(player, detectedTree);
        Bukkit.getPluginManager().callEvent(treeFallEvent);
        return !treeFallEvent.isCancelled();
    }
}
