package com.songoda.ultimatetimber.listener;

import com.songoda.core.vortexcore.vinject.annotation.RegisterListener;
import com.songoda.core.vortexcore.compatibility.folia.SchedulerUtils;
import com.songoda.ultimatetimber.api.UltimateTimberApi;
import com.songoda.ultimatetimber.api.animation.TreeAnimation;
import com.songoda.ultimatetimber.api.event.TreeDamageEvent;
import com.songoda.ultimatetimber.api.manager.TreeAnimationManager;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.manager.TreeAnimationManagerImpl;
import net.vortexdevelopment.vinject.annotation.Inject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Listener handling falling block impact, entity damage, and cancellation of regular block forming.
 */
@RegisterListener
public class TreeAnimationListener implements Listener {

    @Inject
    private TreeAnimationManager treeAnimationManager;

    @Inject
    private TimberConfig config;

    @EventHandler(priority = EventPriority.HIGH)
    public void onFallingBlockLand(EntityChangeBlockEvent event) {
        FallingBlock fallingBlock = findAnimatedFallingBlock(event);
        if (fallingBlock == null) {
            return;
        }

        damageNearbyEntities(fallingBlock);
        if (removeFallingBlockFromAnimation(fallingBlock)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onFallingBlockRemoved(EntityRemoveEvent event) {
        if (!(event.getEntity() instanceof FallingBlock fallingBlock)) {
            return;
        }

        if (!(this.treeAnimationManager instanceof TreeAnimationManagerImpl managerImpl)) {
            return;
        }

        TreeAnimation treeAnimation = managerImpl.getAnimationForBlock(fallingBlock);
        if (treeAnimation != null) {
            treeAnimation.removeFallingBlock(fallingBlock);
        }
    }

    private @Nullable FallingBlock findAnimatedFallingBlock(EntityChangeBlockEvent event) {
        if (event.getEntityType() != EntityType.FALLING_BLOCK) {
            return null;
        }

        FallingBlock fallingBlock = (FallingBlock) event.getEntity();
        return this.treeAnimationManager.isBlockInAnimation(fallingBlock) ? fallingBlock : null;
    }

    private void damageNearbyEntities(FallingBlock fallingBlock) {
        if (this.config == null || !this.config.isFallingBlocksDealDamage()) {
            return;
        }

        int damage = this.config.getFallingBlockDamage();
        for (Entity entity : fallingBlock.getNearbyEntities(0.5, 0.5, 0.5)) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }

            if (!SchedulerUtils.isOwnedByCurrentRegion(livingEntity)) {
                SchedulerUtils.runEntityTask(UltimateTimberApi.getPlugin(), livingEntity, () -> damageEntity(fallingBlock, livingEntity, damage));
                continue;
            }

            this.damageEntity(fallingBlock, livingEntity, damage);
        }
    }

    private void damageEntity(FallingBlock fallingBlock, LivingEntity livingEntity, int damage) {
        if (livingEntity instanceof Player player && !isPlayerDamageAllowed(fallingBlock, player)) {
            return;
        }

        livingEntity.damage(damage, fallingBlock);
    }

    private boolean isPlayerDamageAllowed(FallingBlock fallingBlock, Player player) {
        TreeDamageEvent damageEvent = new TreeDamageEvent(fallingBlock, player);
        Bukkit.getPluginManager().callEvent(damageEvent);
        return !damageEvent.isCancelled();
    }

    private boolean removeFallingBlockFromAnimation(FallingBlock fallingBlock) {
        if (this.config == null || !this.config.isScatterTreeBlocksOnGround()) {
            return false;
        }

        if (!(this.treeAnimationManager instanceof TreeAnimationManagerImpl managerImpl)) {
            return false;
        }

        TreeAnimation treeAnimation = managerImpl.getAnimationForBlock(fallingBlock);
        if (treeAnimation == null) {
            return false;
        }

        treeAnimation.removeFallingBlock(fallingBlock);
        return true;
    }
}
