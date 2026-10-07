package com.songoda.ultimatetimber.manager;

import com.songoda.core.SongodaPlugin;
import com.songoda.core.compatibility.folia.SchedulerUtils;
import com.songoda.core.hooks.internal.ReloadHook;
import com.songoda.core.vinject.annotation.RegisterReloadHook;
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
import net.vortexdevelopment.vinject.annotation.lifecycle.OnDestroy;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing active tree falling animations and tracking animated blocks.
 */
@Component
@RegisterReloadHook
public class TreeAnimationManagerImpl implements TreeAnimationManager, ReloadHook {

    private final Set<TreeAnimation> activeAnimations = ConcurrentHashMap.newKeySet();
    @Inject
    private TimberConfig config;
    @Inject
    private TreeDefinitionManager treeDefinitionManager;
    @Inject
    private SaplingManager saplingManager;

    @OnDestroy
    public void onDestroy() {
        this.clearActiveAnimations();
    }

    @Override
    public void onReload() {
        this.clearActiveAnimations();
    }

    private void clearActiveAnimations() {
        for (TreeAnimation treeAnimation : this.activeAnimations) {
            Set<TreeBlock<FallingBlock>> fallingBlocks;
            synchronized (treeAnimation.getFallingTreeBlocks()) {
                fallingBlocks = new HashSet<>(treeAnimation.getFallingTreeBlocks().getAllTreeBlocks());
            }

            for (TreeBlock<FallingBlock> fallingTreeBlock : fallingBlocks) {
                treeAnimation.removeFallingBlock(fallingTreeBlock.block());
            }
        }

        this.activeAnimations.clear();
    }

    @Override
    public void runAnimation(@NotNull DetectedTree detectedTree, @NotNull Player player) {
        this.runAnimation(detectedTree, player, () -> {
        });
    }

    @Override
    public void runAnimation(@NotNull DetectedTree detectedTree,
                             @NotNull Player player,
                             @NotNull Runnable whenFinished) {
        TreeAnimationType animationType = this.resolveAnimationType(player);

        TreeAnimation animation = switch (animationType) {
            case FANCY -> new TreeAnimationFancy(detectedTree, player, this.config);
            case DISINTEGRATE -> new TreeAnimationDisintegrate(detectedTree, player, this.config);
            case CRUMBLE -> new TreeAnimationCrumble(detectedTree, player, this.config);
            case NONE -> new TreeAnimationNone(detectedTree, player, this.config);
        };

        registerTreeAnimation(animation, whenFinished);
    }

    private TreeAnimationType resolveAnimationType(Player player) {
        String configuredType = this.config != null ? this.config.getTreeAnimationType() : "FANCY";
        TreeAnimationType configuredAnimation = TreeAnimationType.fromString(configuredType);
        TreeAnimationType permittedAnimation = null;

        for (TreeAnimationType type : TreeAnimationType.values()) {
            String permission = "ultimatetimber.animation." + type.name().toLowerCase();
            if (!player.hasPermission(permission)) {
                continue;
            }

            if (permittedAnimation != null) {
                return configuredAnimation;
            }

            permittedAnimation = type;
        }

        return permittedAnimation != null ? permittedAnimation : configuredAnimation;
    }

    private void registerTreeAnimation(TreeAnimation treeAnimation, Runnable whenFinished) {
        this.activeAnimations.add(treeAnimation);
        treeAnimation.playAnimation(() -> {
            this.activeAnimations.remove(treeAnimation);
            whenFinished.run();
        });
    }

    @Override
    public boolean isBlockInAnimation(@NotNull Block block) {
        for (TreeAnimation treeAnimation : this.activeAnimations) {
            for (TreeBlock<Block> treeBlock : treeAnimation.getDetectedTree().detectedTreeBlocks().getAllTreeBlocks()) {
                if (treeBlock.block().equals(block)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isBlockInAnimation(@NotNull FallingBlock fallingBlock) {
        for (TreeAnimation treeAnimation : this.activeAnimations) {
            synchronized (treeAnimation.getFallingTreeBlocks()) {
                for (TreeBlock<FallingBlock> treeBlock : treeAnimation.getFallingTreeBlocks().getAllTreeBlocks()) {
                    if (treeBlock.block().equals(fallingBlock)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public TreeAnimation getAnimationForBlock(@NotNull FallingBlock fallingBlock) {
        for (TreeAnimation treeAnimation : this.activeAnimations) {
            synchronized (treeAnimation.getFallingTreeBlocks()) {
                for (TreeBlock<FallingBlock> treeBlock : treeAnimation.getFallingTreeBlocks().getAllTreeBlocks()) {
                    if (treeBlock.block().equals(fallingBlock)) {
                        return treeAnimation;
                    }
                }
            }
        }
        return null;
    }

    public boolean runFallingBlockImpact(@NotNull FallingBlock fallingBlock) {
        TreeAnimation treeAnimation = this.getAnimationForBlock(fallingBlock);
        if (treeAnimation == null) {
            return false;
        }

        TreeBlock<FallingBlock> treeBlock = null;
        synchronized (treeAnimation.getFallingTreeBlocks()) {
            for (TreeBlock<FallingBlock> candidate : treeAnimation.getFallingTreeBlocks().getAllTreeBlocks()) {
                if (candidate.block().equals(fallingBlock)) {
                    treeBlock = candidate;
                    break;
                }
            }
        }

        if (treeBlock == null) {
            return false;
        }

        this.runFallingBlockImpact(treeAnimation, treeBlock);
        return true;
    }

    @Override
    public void runFallingBlockImpact(@NotNull TreeAnimation treeAnimation, @NotNull TreeBlock<FallingBlock> treeBlock) {
        boolean useCustomSound = this.config == null || this.config.isUseCustomSounds();
        boolean useCustomParticles = this.config == null || this.config.isUseCustomParticles();
        TreeDefinition treeDefinition = treeAnimation.getDetectedTree().treeDefinition();

        if (useCustomParticles) {
            ParticleUtils.playLandingParticles(treeBlock);
        }
        if (useCustomSound) {
            SoundUtils.playLandingSound(treeBlock);
        }

        Location impactLocation = treeBlock.getLocation().clone().subtract(0.0, 1.0, 0.0);
        if (this.config != null && !this.config.getResolvedFragileBlocks().isEmpty()) {
            SchedulerUtils.runLocationTask(SongodaPlugin.getInstance(), impactLocation, () -> this.breakFragileBlock(impactLocation));
        }

        if (this.treeDefinitionManager != null) {
            Location lootDropLocation = null;
            if (this.config != null && !this.config.isRealisticDrops()) {
                TreeBlock<?> initialLog = treeAnimation.getDetectedTree().detectedTreeBlocks().getInitialLogBlock();
                if (initialLog != null) {
                    lootDropLocation = initialLog.getLocation();
                }
            }

            this.treeDefinitionManager.dropTreeLoot(
                    treeDefinition,
                    treeBlock,
                    treeAnimation.getPlayer(),
                    treeAnimation.hasSilkTouch(),
                    false,
                    lootDropLocation
            );
        }
        if (this.saplingManager != null) {
            this.saplingManager.replantSaplingWithChance(treeDefinition, treeBlock);
        }
        treeAnimation.removeFallingBlock(treeBlock.block());
    }

    private void breakFragileBlock(@NotNull Location location) {
        if (this.config == null) {
            return;
        }

        Block block = location.getBlock();
        if (!this.config.getResolvedFragileBlocks().contains(block.getType())) {
            return;
        }

        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(block.getType()));
        block.breakNaturally();
    }
}
