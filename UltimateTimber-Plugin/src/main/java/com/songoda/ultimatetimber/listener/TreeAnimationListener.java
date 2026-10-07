package com.songoda.ultimatetimber.listener;

import com.songoda.core.SongodaPlugin;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.compatibility.folia.SchedulerUtils;
import com.songoda.core.vinject.annotation.RegisterListener;
import com.songoda.ultimatetimber.api.animation.TreeAnimation;
import com.songoda.ultimatetimber.api.event.TreeDamageEvent;
import com.songoda.ultimatetimber.api.manager.TreeAnimationManager;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.manager.TreeAnimationManagerImpl;
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.annotation.lifecycle.PostConstruct;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityEvent;
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

    private static @Nullable Class<? extends Event> resolveEntityRemovalEventType() {
        String eventClassName = ServerVersion.isAtLeastVersion("1.21.3")
                ? "org.bukkit.event.entity.EntityRemoveEvent"
                : "com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent";
        String fallbackEventClassName = ServerVersion.isAtLeastVersion("1.21.3")
                ? "com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent"
                : "org.bukkit.event.entity.EntityRemoveEvent";
        Class<? extends Event> removalEventType = loadEventType(eventClassName);
        return removalEventType != null ? removalEventType : loadEventType(fallbackEventClassName);
    }

    private static @Nullable Class<? extends Event> loadEventType(String eventClassName) {
        try {
            return Class.forName(eventClassName).asSubclass(Event.class);
        } catch (ClassNotFoundException | ClassCastException exception) {
            return null;
        }
    }

    @PostConstruct
    public void registerFallingBlockRemovalHandler() {
        Class<? extends Event> removalEventType = resolveEntityRemovalEventType();
        if (removalEventType == null) {
            return;
        }

        Bukkit.getPluginManager().registerEvent(
                removalEventType,
                this,
                EventPriority.NORMAL,
                (listener, event) -> this.onFallingBlockRemoved(event),
                SongodaPlugin.getInstance(),
                false
        );
    }

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

        if (this.treeAnimationManager instanceof TreeAnimationManagerImpl managerImpl) {
            managerImpl.runFallingBlockImpact(fallingBlock);
        }
        event.setCancelled(true);
    }

    private void onFallingBlockRemoved(Event event) {
        if (!(event instanceof EntityEvent entityEvent) || !(entityEvent.getEntity() instanceof FallingBlock fallingBlock)) {
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
                SchedulerUtils.runEntityTask(SongodaPlugin.getInstance(), livingEntity, () -> damageEntity(fallingBlock, livingEntity, damage));
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
