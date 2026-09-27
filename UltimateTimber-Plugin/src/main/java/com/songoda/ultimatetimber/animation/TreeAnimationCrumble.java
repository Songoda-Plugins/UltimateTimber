package com.songoda.ultimatetimber.animation;

import com.songoda.core.SongodaPlugin;
import com.songoda.ultimatetimber.api.UltimateTimberApi;
import com.songoda.ultimatetimber.api.animation.TreeAnimationType;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockType;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.utils.BlockUtils;
import com.songoda.ultimatetimber.utils.ParticleUtils;
import com.songoda.ultimatetimber.utils.RegionBatchProcessor;
import com.songoda.ultimatetimber.utils.SoundUtils;
import com.songoda.core.vortexcore.compatibility.folia.SchedulerRunnable;
import com.songoda.core.vortexcore.compatibility.folia.SchedulerUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Animation where tree blocks crumble down layer by layer.
 */
public class TreeAnimationCrumble extends TreeAnimationBase {

    public TreeAnimationCrumble(@NotNull DetectedTree detectedTree,
                                @NotNull Player player,
                                @Nullable TimberConfig config) {
        super(TreeAnimationType.CRUMBLE, detectedTree, player, config);
    }

    @Override
    public void playAnimation(@NotNull Runnable whenFinished) {
        Plugin plugin = SongodaPlugin.getInstance();
        TimberConfig config = this.config;

        boolean useCustomSound = config == null || config.isUseCustomSounds();
        boolean useCustomParticles = config == null || config.isUseCustomParticles();

        int currentY = -1;
        List<List<TreeBlock<Block>>> treeBlocks = new ArrayList<>();
        List<TreeBlock<Block>> currentPartition = new ArrayList<>();
        List<TreeBlock<Block>> orderedDetectedTreeBlocks = new ArrayList<>(this.detectedTree.detectedTreeBlocks().getAllTreeBlocks());
        orderedDetectedTreeBlocks.sort(Comparator.comparingInt(x -> x.getLocation().getBlockY()));

        for (TreeBlock<Block> treeBlock : orderedDetectedTreeBlocks) {
            if (currentY != treeBlock.getLocation().getBlockY()) {
                if (!currentPartition.isEmpty()) {
                    Collections.shuffle(currentPartition);
                    treeBlocks.add(new ArrayList<>(currentPartition));
                    currentPartition.clear();
                }
                currentY = treeBlock.getLocation().getBlockY();
            }
            currentPartition.add(treeBlock);
        }

        if (!currentPartition.isEmpty()) {
            Collections.shuffle(currentPartition);
            treeBlocks.add(new ArrayList<>(currentPartition));
        }

        TreeDefinition td = this.detectedTree.treeDefinition();
        SchedulerUtils.runTaskTimer(plugin, new CrumbleAnimationTask(
                treeBlocks,
                td,
                useCustomSound,
                useCustomParticles,
                whenFinished
        ), 0L, 1L);
    }

    private final class CrumbleAnimationTask extends SchedulerRunnable {

        private final List<List<TreeBlock<Block>>> treeBlocks;
        private final TreeDefinition treeDefinition;
        private final boolean useCustomSound;
        private final boolean useCustomParticles;
        private final Runnable whenFinished;
        private final AtomicBoolean batchInProgress = new AtomicBoolean();

        private CrumbleAnimationTask(List<List<TreeBlock<Block>>> treeBlocks,
                                     TreeDefinition treeDefinition,
                                     boolean useCustomSound,
                                     boolean useCustomParticles,
                                     Runnable whenFinished) {
            this.treeBlocks = treeBlocks;
            this.treeDefinition = treeDefinition;
            this.useCustomSound = useCustomSound;
            this.useCustomParticles = useCustomParticles;
            this.whenFinished = whenFinished;
        }

        @Override
        public void run() {
            if (this.batchInProgress.get()) {
                return;
            }

            if (this.treeBlocks.isEmpty()) {
                if (!TreeAnimationCrumble.this.hasFallingBlocks()) {
                    this.cancel();
                    this.whenFinished.run();
                }
                return;
            }

            List<TreeBlock<Block>> partition = this.treeBlocks.get(0);
            List<TreeBlock<Block>> batch = new ArrayList<>(3);
            for (int i = 0; i < 3 && !partition.isEmpty(); i++) {
                batch.add(partition.remove(0));
            }

            if (partition.isEmpty()) {
                this.treeBlocks.remove(0);
            }

            this.batchInProgress.set(true);
            RegionBatchProcessor.processByRegion(
                    SongodaPlugin.getInstance(),
                    batch,
                    TreeBlock::getLocation,
                    this::animateBlock,
                    () -> this.batchInProgress.set(false)
            );
        }

        private void animateBlock(@NotNull TreeBlock<Block> treeBlock) {
            if (treeBlock.treeBlockType() == TreeBlockType.LOG) {
                if (!this.treeDefinition.getLogMaterials().contains(treeBlock.block().getType())) {
                    return;
                }
            } else if (treeBlock.treeBlockType() == TreeBlockType.LEAF) {
                if (!this.treeDefinition.getLeafMaterials().contains(treeBlock.block().getType())) {
                    return;
                }
            }

            TreeBlock<FallingBlock> fallingTreeBlock = TreeAnimationCrumble.this.convertToFallingBlock(treeBlock);
            if (fallingTreeBlock == null) {
                return;
            }

            BlockUtils.toggleGravityFallingBlock(fallingTreeBlock.block(), true);
            fallingTreeBlock.block().setVelocity(Vector.getRandom().setY(0).subtract(new Vector(0.5, 0, 0.5)).multiply(0.15));
            TreeAnimationCrumble.this.trackFallingBlock(fallingTreeBlock);

            if (this.useCustomSound) {
                SoundUtils.playLandingSound(treeBlock);
            }
            if (this.useCustomParticles) {
                ParticleUtils.playFallingParticles(fallingTreeBlock);
            }
        }
    }
}
