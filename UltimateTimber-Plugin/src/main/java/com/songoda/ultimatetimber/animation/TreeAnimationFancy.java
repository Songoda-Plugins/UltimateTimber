package com.songoda.ultimatetimber.animation;

import com.songoda.core.vortexcore.compatibility.folia.SchedulerRunnable;
import com.songoda.core.vortexcore.compatibility.folia.SchedulerUtils;
import com.songoda.ultimatetimber.api.UltimateTimberApi;
import com.songoda.ultimatetimber.api.animation.TreeAnimationType;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockSet;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.utils.BlockUtils;
import com.songoda.ultimatetimber.utils.ParticleUtils;
import com.songoda.ultimatetimber.utils.RegionBatchProcessor;
import com.songoda.ultimatetimber.utils.SoundUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Animated tree fall where the tree topples over away from the player with physics.
 */
public class TreeAnimationFancy extends TreeAnimationBase {

    private static final int MAX_ANIMATION_TICKS = 4 * 20;

    public TreeAnimationFancy(@NotNull DetectedTree detectedTree,
                              @NotNull Player player,
                              @Nullable TimberConfig config) {
        super(TreeAnimationType.FANCY, detectedTree, player, config);
    }

    @Override
    public void playAnimation(@NotNull Runnable whenFinished) {
        Plugin plugin = UltimateTimberApi.getPlugin();
        TimberConfig config = this.config;

        boolean useCustomSound = config == null || config.isUseCustomSounds();
        boolean useCustomParticles = config == null || config.isUseCustomParticles();

        TreeBlock<Block> initialTreeBlock = this.detectedTree.detectedTreeBlocks().getInitialLogBlock();
        Location playerLocation = this.player.getLocation();
        Vector velocityVector = this.createVelocityVector(initialTreeBlock, playerLocation);
        double playerY = playerLocation.getY();
        TreeBlock<FallingBlock> initialFallingBlock = this.prepareInitialBlock(
                initialTreeBlock,
                velocityVector,
                playerY,
                useCustomSound,
                useCustomParticles
        );
        this.fallingTreeBlocks = new TreeBlockSet<>(initialFallingBlock);

        List<TreeBlock<Block>> remainingBlocks = new ArrayList<>(this.detectedTree.detectedTreeBlocks().getAllTreeBlocks());
        remainingBlocks.removeIf(treeBlock -> treeBlock == initialTreeBlock);
        RegionBatchProcessor.processByRegion(
                plugin,
                remainingBlocks,
                TreeBlock::getLocation,
                treeBlock -> this.prepareDetectedBlock(treeBlock, velocityVector, playerY, useCustomParticles),
                () -> this.startAnimation(plugin, whenFinished)
        );
    }

    private @Nullable TreeBlock<FallingBlock> prepareInitialBlock(@Nullable TreeBlock<Block> initialTreeBlock,
                                                                  @NotNull Vector velocityVector,
                                                                  double playerY,
                                                                  boolean useCustomSound,
                                                                  boolean useCustomParticles) {
        if (initialTreeBlock == null) {
            return null;
        }

        TreeBlock<FallingBlock> initialFallingBlock = initialTreeBlock.block().getType().isAir()
                ? null
                : this.convertToFallingBlock(initialTreeBlock);
        if (useCustomSound) {
            SoundUtils.playFallingSound(initialTreeBlock);
        }

        if (initialFallingBlock != null) {
            this.setFallingBlockVelocity(initialFallingBlock.block(), initialTreeBlock, velocityVector, playerY);
            if (useCustomParticles) {
                ParticleUtils.playFallingParticles(initialFallingBlock);
            }
        }

        return initialFallingBlock;
    }

    private void prepareDetectedBlock(@NotNull TreeBlock<Block> treeBlock,
                                      @NotNull Vector velocityVector,
                                      double playerY,
                                      boolean useCustomParticles) {
        TreeBlock<FallingBlock> fallingTreeBlock = this.convertToFallingBlock(treeBlock);
        if (fallingTreeBlock == null) {
            return;
        }

        this.setFallingBlockVelocity(fallingTreeBlock.block(), treeBlock, velocityVector, playerY);
        synchronized (this.fallingTreeBlocks) {
            this.fallingTreeBlocks.add(fallingTreeBlock);
        }

        if (useCustomParticles) {
            ParticleUtils.playFallingParticles(fallingTreeBlock);
        }
    }

    private void startAnimation(@NotNull Plugin plugin, @NotNull Runnable whenFinished) {
        Set<TreeBlock<FallingBlock>> fallingBlocks;
        synchronized (this.fallingTreeBlocks) {
            fallingBlocks = new HashSet<>(this.fallingTreeBlocks.getAllTreeBlocks());
        }

        for (TreeBlock<FallingBlock> fallingTreeBlock : fallingBlocks) {
            this.trackFallingBlock(
                    fallingTreeBlock,
                    1L,
                    MAX_ANIMATION_TICKS,
                    age -> this.applyFallingBlockMotion(fallingTreeBlock.block(), age)
            );
        }

        SchedulerUtils.runTaskTimer(plugin, new FancyAnimationTask(whenFinished), 0L, 1L);
    }

    private void applyFallingBlockMotion(@NotNull FallingBlock fallingBlock, int age) {
        if (age == 1) {
            BlockUtils.toggleGravityFallingBlock(fallingBlock, true);
            fallingBlock.setVelocity(fallingBlock.getVelocity().multiply(1.5));
        }

        fallingBlock.setVelocity(fallingBlock.getVelocity().clone().subtract(new Vector(0, 0.05, 0)));
    }

    private void setFallingBlockVelocity(@NotNull FallingBlock fallingBlock,
                                         @NotNull TreeBlock<Block> treeBlock,
                                         @NotNull Vector velocityVector,
                                         double playerY) {
        double multiplier = (treeBlock.getLocation().getY() - playerY) * 0.05;
        fallingBlock.setVelocity(velocityVector.clone().multiply(multiplier).multiply(0.3));
    }

    private @NotNull Vector createVelocityVector(@Nullable TreeBlock<Block> initialTreeBlock, @NotNull Location playerLocation) {
        if (initialTreeBlock == null) {
            return new Vector(0, 0, 0);
        }

        return initialTreeBlock.getLocation().clone().subtract(playerLocation.clone()).toVector().normalize().setY(0);
    }

    private final class FancyAnimationTask extends SchedulerRunnable {

        private final Runnable whenFinished;
        private int timer;

        private FancyAnimationTask(@NotNull Runnable whenFinished) {
            this.whenFinished = whenFinished;
        }

        @Override
        public void run() {
            if (!TreeAnimationFancy.this.hasFallingBlocks()) {
                this.finish();
                return;
            }

            this.timer++;
            if (this.timer > MAX_ANIMATION_TICKS) {
                this.finish();
            }
        }

        private void finish() {
            this.cancel();
            this.whenFinished.run();
        }
    }
}
