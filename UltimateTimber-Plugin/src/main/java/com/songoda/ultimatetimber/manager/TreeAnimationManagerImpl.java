package com.songoda.ultimatetimber.manager;

import com.songoda.core.vortexcore.hooks.internal.ReloadHook;
import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.animation.TreeAnimationCrumble;
import com.songoda.ultimatetimber.animation.TreeAnimationDisintegrate;
import com.songoda.ultimatetimber.animation.TreeAnimationFancy;
import com.songoda.ultimatetimber.animation.TreeAnimationNone;
import com.songoda.ultimatetimber.api.animation.TreeAnimation;
import com.songoda.ultimatetimber.api.animation.TreeAnimationType;
import com.songoda.ultimatetimber.api.manager.SaplingManager;
import com.songoda.ultimatetimber.api.manager.TreeAnimationManager;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.utils.ParticleUtils;
import com.songoda.ultimatetimber.utils.SoundUtils;
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.annotation.component.Component;
import com.songoda.core.vortexcore.vinject.annotation.RegisterReloadHook;
import net.vortexdevelopment.vinject.annotation.lifecycle.OnDestroy;
import net.vortexdevelopment.vinject.annotation.lifecycle.OnLoad;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing active tree falling animations and tracking animated blocks.
 */
@Component
@RegisterReloadHook
public class TreeAnimationManagerImpl implements TreeAnimationManager, ReloadHook, Runnable {

    @Inject
    private TimberConfig config;

    @Inject
    private TreeDefinitionManager treeDefinitionManager;

    @Inject
    private SaplingManager saplingManager;

    private final Set<TreeAnimation> activeAnimations = ConcurrentHashMap.newKeySet();
    private BukkitTask task;

    @OnLoad
    public void onLoad() {
        UltimateTimber plugin = UltimateTimber.getInstance();
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, 1L);
    }

    @OnDestroy
    public void onDestroy() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
        this.activeAnimations.clear();
    }

    @Override
    public void onReload() {
        this.activeAnimations.clear();
    }

    @Override
    public void run() {
        for (TreeAnimation treeAnimation : this.activeAnimations) {
            Set<TreeBlock<FallingBlock>> groundedBlocks = new HashSet<>();
            for (TreeBlock<FallingBlock> fallingTreeBlock : treeAnimation.getFallingTreeBlocks().getAllTreeBlocks()) {
                FallingBlock fallingBlock = fallingTreeBlock.getBlock();
                if (fallingBlock.isDead() || fallingBlock.isOnGround()) {
                    groundedBlocks.add(fallingTreeBlock);
                }
            }

            for (TreeBlock<FallingBlock> fallingBlock : groundedBlocks) {
                runFallingBlockImpact(treeAnimation, fallingBlock);
                fallingBlock.getBlock().remove();
                treeAnimation.getFallingTreeBlocks().remove(fallingBlock);
            }
        }
    }

    @Override
    public void runAnimation(@NotNull DetectedTree detectedTree, @NotNull Player player) {
        String configuredType = this.config != null ? this.config.getTreeAnimationType() : "FANCY";
        TreeAnimationType animationType = TreeAnimationType.fromString(configuredType);

        for (TreeAnimationType type : TreeAnimationType.values()) {
            String permission = "ultimatetimber.animation." + type.name().toLowerCase();
            if (player.hasPermission(permission)) {
                animationType = type;
                break;
            }
        }

        TreeAnimation animation = switch (animationType) {
            case FANCY -> new TreeAnimationFancy(detectedTree, player);
            case DISINTEGRATE -> new TreeAnimationDisintegrate(detectedTree, player);
            case CRUMBLE -> new TreeAnimationCrumble(detectedTree, player);
            case NONE -> new TreeAnimationNone(detectedTree, player);
        };

        registerTreeAnimation(animation);
    }

    private void registerTreeAnimation(TreeAnimation treeAnimation) {
        this.activeAnimations.add(treeAnimation);
        treeAnimation.playAnimation(() -> this.activeAnimations.remove(treeAnimation));
    }

    @Override
    public boolean isBlockInAnimation(@NotNull Block block) {
        for (TreeAnimation treeAnimation : this.activeAnimations) {
            for (TreeBlock<Block> treeBlock : treeAnimation.getDetectedTree().getDetectedTreeBlocks().getAllTreeBlocks()) {
                if (treeBlock.getBlock().equals(block)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isBlockInAnimation(@NotNull FallingBlock fallingBlock) {
        for (TreeAnimation treeAnimation : this.activeAnimations) {
            for (TreeBlock<FallingBlock> treeBlock : treeAnimation.getFallingTreeBlocks().getAllTreeBlocks()) {
                if (treeBlock.getBlock().equals(fallingBlock)) {
                    return true;
                }
            }
        }
        return false;
    }

    public TreeAnimation getAnimationForBlock(@NotNull FallingBlock fallingBlock) {
        for (TreeAnimation treeAnimation : this.activeAnimations) {
            for (TreeBlock<FallingBlock> treeBlock : treeAnimation.getFallingTreeBlocks().getAllTreeBlocks()) {
                if (treeBlock.getBlock().equals(fallingBlock)) {
                    return treeAnimation;
                }
            }
        }
        return null;
    }

    @Override
    public void runFallingBlockImpact(@NotNull TreeAnimation treeAnimation, @NotNull TreeBlock<FallingBlock> treeBlock) {
        boolean useCustomSound = this.config == null || this.config.isUseCustomSounds();
        boolean useCustomParticles = this.config == null || this.config.isUseCustomParticles();
        TreeDefinition treeDefinition = treeAnimation.getDetectedTree().getTreeDefinition();

        if (useCustomParticles) {
            ParticleUtils.playLandingParticles(treeBlock);
        }
        if (useCustomSound) {
            SoundUtils.playLandingSound(treeBlock);
        }

        Location impactLocation = treeBlock.getLocation().clone().subtract(0.0, 1.0, 0.0);
        Block blockBelow = impactLocation.getBlock();
        if (this.config != null && this.config.getResolvedFragileBlocks().contains(blockBelow.getType())) {
            if (blockBelow.getWorld() != null) {
                blockBelow.getWorld().dropItemNaturally(blockBelow.getLocation(), new ItemStack(blockBelow.getType()));
            }
            blockBelow.breakNaturally();
        }

        if (this.treeDefinitionManager != null) {
            this.treeDefinitionManager.dropTreeLoot(treeDefinition, treeBlock, treeAnimation.getPlayer(), treeAnimation.hasSilkTouch(), false);
        }
        if (this.saplingManager != null) {
            this.saplingManager.replantSaplingWithChance(treeDefinition, treeBlock);
        }
        treeAnimation.getFallingTreeBlocks().remove(treeBlock);
    }
}
