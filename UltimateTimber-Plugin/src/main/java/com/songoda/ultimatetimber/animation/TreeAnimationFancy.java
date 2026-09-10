package com.songoda.ultimatetimber.animation;

import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.api.animation.TreeAnimationType;
import com.songoda.ultimatetimber.api.manager.TreeAnimationManager;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockSet;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.utils.BlockUtils;
import com.songoda.ultimatetimber.utils.ParticleUtils;
import com.songoda.ultimatetimber.utils.SoundUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Animated tree fall where the tree topples over away from the player with physics.
 */
public class TreeAnimationFancy extends TreeAnimationBase {

    public TreeAnimationFancy(@NotNull DetectedTree detectedTree, @NotNull Player player) {
        super(TreeAnimationType.FANCY, detectedTree, player);
    }

    @Override
    public void playAnimation(@NotNull Runnable whenFinished) {
        UltimateTimber plugin = UltimateTimber.getInstance();
        TimberConfig config = plugin.getTimberConfig();

        boolean useCustomSound = config == null || config.isUseCustomSounds();
        boolean useCustomParticles = config == null || config.isUseCustomParticles();

        TreeBlock<Block> initialTreeBlock = this.detectedTree.getDetectedTreeBlocks().getInitialLogBlock();
        TreeBlock<FallingBlock> initialFallingBlock = this.convertToFallingBlock(initialTreeBlock);

        if (useCustomSound && initialTreeBlock != null) {
            SoundUtils.playFallingSound(initialTreeBlock);
        }

        Vector velocityVector = initialTreeBlock != null
                ? initialTreeBlock.getLocation().clone().subtract(this.player.getLocation().clone()).toVector().normalize().setY(0)
                : new Vector(0, 0, 0);

        this.fallingTreeBlocks = new TreeBlockSet<>(initialFallingBlock);
        for (TreeBlock<Block> treeBlock : this.detectedTree.getDetectedTreeBlocks().getAllTreeBlocks()) {
            TreeBlock<FallingBlock> fallingTreeBlock = this.convertToFallingBlock(treeBlock);
            if (fallingTreeBlock == null) {
                continue;
            }

            FallingBlock fallingBlock = fallingTreeBlock.getBlock();
            this.fallingTreeBlocks.add(fallingTreeBlock);

            if (useCustomParticles) {
                ParticleUtils.playFallingParticles(treeBlock);
            }

            double multiplier = (treeBlock.getLocation().getY() - this.player.getLocation().getY()) * 0.05;
            fallingBlock.setVelocity(velocityVector.clone().multiply(multiplier).multiply(0.3));
        }

        new BukkitRunnable() {
            int timer = 0;

            @Override
            public void run() {
                if (this.timer == 0) {
                    for (TreeBlock<FallingBlock> fallingTreeBlock : TreeAnimationFancy.this.fallingTreeBlocks.getAllTreeBlocks()) {
                        FallingBlock fallingBlock = fallingTreeBlock.getBlock();
                        BlockUtils.toggleGravityFallingBlock(fallingBlock, true);
                        fallingBlock.setVelocity(fallingBlock.getVelocity().multiply(1.5));
                    }
                }

                if (TreeAnimationFancy.this.fallingTreeBlocks.getAllTreeBlocks().isEmpty()) {
                    whenFinished.run();
                    this.cancel();
                    return;
                }

                for (TreeBlock<FallingBlock> fallingTreeBlock : TreeAnimationFancy.this.fallingTreeBlocks.getAllTreeBlocks()) {
                    FallingBlock fallingBlock = fallingTreeBlock.getBlock();
                    fallingBlock.setVelocity(fallingBlock.getVelocity().clone().subtract(new Vector(0, 0.05, 0)));
                }

                this.timer++;

                if (this.timer > 4 * 20) {
                    TreeAnimationManager treeAnimationManager = plugin.getTreeAnimationManager();
                    if (treeAnimationManager != null) {
                        for (TreeBlock<FallingBlock> fallingTreeBlock : TreeAnimationFancy.this.fallingTreeBlocks.getAllTreeBlocks()) {
                            treeAnimationManager.runFallingBlockImpact(TreeAnimationFancy.this, fallingTreeBlock);
                        }
                    }
                    whenFinished.run();
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 1L);
    }
}
