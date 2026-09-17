package com.songoda.ultimatetimber.manager;

import com.songoda.core.vortexcore.hooks.internal.ReloadHook;
import com.songoda.core.vortexcore.vinject.annotation.RegisterReloadHook;
import com.songoda.ultimatetimber.api.event.TreeFallEvent;
import com.songoda.ultimatetimber.api.event.TreeFellEvent;
import com.songoda.ultimatetimber.api.manager.ChoppingManager;
import com.songoda.ultimatetimber.api.manager.SaplingManager;
import com.songoda.ultimatetimber.api.manager.TreeAnimationManager;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.manager.TreeDetectionManager;
import com.songoda.ultimatetimber.api.manager.TreeFallManager;
import com.songoda.ultimatetimber.api.misc.OnlyToppleWhile;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockSet;
import com.songoda.ultimatetimber.config.TimberConfig;
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.annotation.component.Component;
import net.vortexdevelopment.vinject.annotation.lifecycle.PostConstruct;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Service managing tree felling execution, tool validation, and animation triggers.
 */
@Component
@RegisterReloadHook
public class TreeFallManagerImpl implements TreeFallManager, ReloadHook {

    @Inject
    private TimberConfig config;

    @Inject
    private TreeDefinitionManager treeDefinitionManager;

    @Inject
    private TreeDetectionManager treeDetectionManager;

    @Inject
    private TreeAnimationManager treeAnimationManager;

    @Inject
    private ChoppingManager choppingManager;

    @Inject
    private SaplingManager saplingManager;

    private int maxLogsPerChop = 150;
    private final Random random = new Random();

    @PostConstruct
    public void initialize() {
        onReload();
    }

    @Override
    public void onReload() {
        if (this.config != null) {
            this.maxLogsPerChop = this.config.getMaxLogsPerChop();
        }
    }

    @Override
    public boolean canTopple(@NotNull Player player, @NotNull Block block, @Nullable ItemStack tool) {
        if (this.saplingManager.isSaplingProtected(block)) {
            return false;
        }

        if (this.config == null) {
            return true;
        }

        if (this.config.getDisabledWorlds().contains(player.getWorld().getName())) {
            return false;
        }

        if (!this.config.isAllowCreativeMode() && player.getGameMode() == GameMode.CREATIVE) {
            return false;
        }

        if (!checkToppleWhile(player)) {
            return false;
        }

        if (this.config.isRequireChopPermission() && !player.hasPermission("ultimatetimber.chop")) {
            return false;
        }

        if (!this.choppingManager.isChopping(player)) {
            return false;
        }

        if (this.choppingManager.isInCooldown(player)) {
            return false;
        }

        if (this.treeAnimationManager.isBlockInAnimation(block)) {
            return false;
        }

        return this.treeDefinitionManager.isToolValidForAnyTreeDefinition(tool);
    }

    @Override
    public void toppleTree(@NotNull Player player, @NotNull DetectedTree detectedTree, @Nullable ItemStack tool) {
        detectedTree.getDetectedTreeBlocks().sortAndLimit(this.maxLogsPerChop);

        this.choppingManager.cooldownPlayer(player);

        if (this.config != null && this.config.isDestroyInitiatedBlock()) {
            TreeBlock<Block> initialLogBlock = detectedTree.getDetectedTreeBlocks().getInitialLogBlock();
            if (initialLogBlock != null) {
                initialLogBlock.getBlock().setType(Material.AIR);
                detectedTree.getDetectedTreeBlocks().remove(initialLogBlock);
            }
        }

        boolean hasSilkTouch = tool != null && !tool.getType().isAir()
                && tool.hasItemMeta()
                && tool.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH);

        short toolDamage = getToolDamage(detectedTree.getDetectedTreeBlocks(), hasSilkTouch);

        if (player.getGameMode() != GameMode.CREATIVE && tool != null && !tool.getType().isAir()) {
            applyToolDamage(player, tool, toolDamage);
        }

        this.treeAnimationManager.runAnimation(detectedTree, player);

        TreeBlock<Block> initialLog = detectedTree.getDetectedTreeBlocks().getInitialLogBlock();
        if (initialLog != null) {
            this.treeDefinitionManager.dropTreeLoot(detectedTree.getTreeDefinition(), initialLog, player, false, true);
        }

        TreeFellEvent treeFellEvent = new TreeFellEvent(player, detectedTree);
        Bukkit.getPluginManager().callEvent(treeFellEvent);
    }

    private void applyToolDamage(Player player, ItemStack tool, short toolDamage) {
        ItemMeta meta = tool.getItemMeta();
        if (meta instanceof Damageable damageable) {
            int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.UNBREAKING);
            int damageToApply = 0;
            for (int i = 0; i < toolDamage; i++) {
                if (unbreakingLevel <= 0 || this.random.nextInt(unbreakingLevel + 1) == 0) {
                    damageToApply++;
                }
            }

            int newDamage = damageable.getDamage() + damageToApply;
            if (newDamage >= tool.getType().getMaxDurability()) {
                player.getInventory().setItemInMainHand(null);
            } else {
                damageable.setDamage(newDamage);
                tool.setItemMeta(damageable);
            }
        }
    }

    public boolean wouldToolBreak(ItemStack tool, short toolDamage) {
        if (tool == null || tool.getType().isAir()) {
            return false;
        }

        ItemMeta meta = tool.getItemMeta();
        if (meta instanceof Damageable damageable) {
            return (damageable.getDamage() + toolDamage) >= tool.getType().getMaxDurability();
        }
        return false;
    }

    public short getToolDamage(TreeBlockSet<Block> treeBlocks, boolean hasSilkTouch) {
        if (this.config == null || !this.config.isRealisticToolDamage()) {
            return 1;
        }

        if (this.config.isApplySilkTouchToolDamage() && hasSilkTouch) {
            return (short) treeBlocks.size();
        } else {
            return (short) treeBlocks.getLogBlocks().size();
        }
    }

    private boolean checkToppleWhile(Player player) {
        if (this.config == null) {
            return true;
        }

        OnlyToppleWhile setting = OnlyToppleWhile.fromString(this.config.getOnlyToppleWhile());
        return switch (setting) {
            case SNEAKING -> player.isSneaking();
            case NOT_SNEAKING -> !player.isSneaking();
            case ALWAYS -> true;
        };
    }
}
