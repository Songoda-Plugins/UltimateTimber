package com.songoda.ultimatetimber.listener;

import com.songoda.core.vortexcore.vinject.annotation.RegisterListener;
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
        if (event.getEntityType() != EntityType.FALLING_BLOCK) {
            return;
        }

        FallingBlock fallingBlock = (FallingBlock) event.getEntity();
        if (!this.treeAnimationManager.isBlockInAnimation(fallingBlock)) {
            return;
        }

        if (this.config != null && this.config.isFallingBlocksDealDamage()) {
            int damage = this.config.getFallingBlockDamage();
            for (Entity entity : fallingBlock.getNearbyEntities(0.5, 0.5, 0.5)) {
                if (entity instanceof LivingEntity livingEntity) {
                    if (entity instanceof Player player) {
                        TreeDamageEvent damageEvent = new TreeDamageEvent(fallingBlock, player);
                        Bukkit.getPluginManager().callEvent(damageEvent);
                        if (!damageEvent.isCancelled()) {
                            livingEntity.damage(damage, fallingBlock);
                        }
                    } else {
                        livingEntity.damage(damage, fallingBlock);
                    }
                }
            }
        }

        if (this.config != null && this.config.isScatterTreeBlocksOnGround()) {
            if (this.treeAnimationManager instanceof TreeAnimationManagerImpl managerImpl) {
                TreeAnimation treeAnimation = managerImpl.getAnimationForBlock(fallingBlock);
                if (treeAnimation != null) {
                    treeAnimation.removeFallingBlock(fallingBlock);
                    return;
                }
            }
        }

        event.setCancelled(true);
    }
}
