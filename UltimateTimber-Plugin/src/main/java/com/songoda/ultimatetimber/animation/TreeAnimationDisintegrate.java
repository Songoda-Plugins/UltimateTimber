package com.songoda.ultimatetimber.animation;

import com.songoda.core.SongodaPlugin;
import com.songoda.core.vortexcore.compatibility.folia.SchedulerRunnable;
import com.songoda.core.vortexcore.compatibility.folia.SchedulerUtils;
import com.songoda.ultimatetimber.api.UltimateTimberApi;
import com.songoda.ultimatetimber.api.animation.TreeAnimationType;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.utils.ParticleUtils;
import com.songoda.ultimatetimber.utils.RegionBatchProcessor;
import com.songoda.ultimatetimber.utils.SoundUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Animation where tree blocks disintegrate into particles and drops in place.
 */
public class TreeAnimationDisintegrate extends TreeAnimationBase {

    public TreeAnimationDisintegrate(@NotNull DetectedTree detectedTree,
                                     @NotNull Player player,
                                     @Nullable TimberConfig config) {
        super(TreeAnimationType.DISINTEGRATE, detectedTree, player, config);
    }

    @Override
    public void playAnimation(@NotNull Runnable whenFinished) {
        Plugin plugin = SongodaPlugin.getInstance();
        TreeDefinitionManager treeDefinitionManager = UltimateTimberApi.getTreeDefinitionManager();

        boolean useCustomSounds = this.config == null || this.config.isUseCustomSounds();
        boolean useCustomParticles = this.config == null || this.config.isUseCustomParticles();

        List<TreeBlock<Block>> orderedLogBlocks = new ArrayList<>(this.detectedTree.detectedTreeBlocks().getLogBlocks());
        orderedLogBlocks.sort(Comparator.comparingInt(treeBlock -> treeBlock.getLocation().getBlockY()));

        List<TreeBlock<Block>> shuffledLeafBlocks = new ArrayList<>(this.detectedTree.detectedTreeBlocks().getLeafBlocks());
        Collections.shuffle(shuffledLeafBlocks);

        TreeDefinition treeDefinition = this.detectedTree.treeDefinition();

        SchedulerUtils.runTaskTimer(plugin, new DisintegrateAnimationTask(
                orderedLogBlocks,
                shuffledLeafBlocks,
                treeDefinition,
                treeDefinitionManager,
                useCustomSounds,
                useCustomParticles,
                whenFinished
        ), 0L, 1L);
    }

    private boolean disintegrateBlock(@NotNull TreeBlock<Block> treeBlock,
                                      @NotNull TreeDefinition treeDefinition,
                                      @Nullable TreeDefinitionManager treeDefinitionManager,
                                      boolean useCustomSound,
                                      boolean useCustomParticles) {
        if (!this.canBreak(treeBlock) || !isExpectedTreeBlock(treeBlock, treeDefinition)) {
            return false;
        }

        if (useCustomSound) {
            SoundUtils.playLandingSound(treeBlock);
        }
        if (useCustomParticles) {
            ParticleUtils.playFallingParticles(treeBlock);
        }
        if (treeDefinitionManager != null) {
            treeDefinitionManager.dropTreeLoot(
                    treeDefinition,
                    treeBlock,
                    this.player,
                    this.hasSilkTouch,
                    false,
                    this.getLootDropLocation()
            );
        }
        this.replaceBlock(treeBlock);
        return true;
    }

    private boolean isExpectedTreeBlock(@NotNull TreeBlock<Block> treeBlock, @NotNull TreeDefinition treeDefinition) {
        return switch (treeBlock.treeBlockType()) {
            case LOG -> treeDefinition.getLogMaterials().contains(treeBlock.block().getType());
            case LEAF -> treeDefinition.getLeafMaterials().contains(treeBlock.block().getType());
        };
    }

    private final class DisintegrateAnimationTask extends SchedulerRunnable {

        private final List<TreeBlock<Block>> orderedLogBlocks;
        private final List<TreeBlock<Block>> shuffledLeafBlocks;
        private final TreeDefinition treeDefinition;
        @Nullable
        private final TreeDefinitionManager treeDefinitionManager;
        private final boolean useCustomSounds;
        private final boolean useCustomParticles;
        private final Runnable whenFinished;
        private final AtomicBoolean batchInProgress = new AtomicBoolean();
        private int nextLogIndex;
        private int nextLeafIndex;

        private DisintegrateAnimationTask(List<TreeBlock<Block>> orderedLogBlocks,
                                          List<TreeBlock<Block>> shuffledLeafBlocks,
                                          TreeDefinition treeDefinition,
                                          @Nullable TreeDefinitionManager treeDefinitionManager,
                                          boolean useCustomSounds,
                                          boolean useCustomParticles,
                                          Runnable whenFinished) {
            this.orderedLogBlocks = orderedLogBlocks;
            this.shuffledLeafBlocks = shuffledLeafBlocks;
            this.treeDefinition = treeDefinition;
            this.treeDefinitionManager = treeDefinitionManager;
            this.useCustomSounds = useCustomSounds;
            this.useCustomParticles = useCustomParticles;
            this.whenFinished = whenFinished;
        }

        @Override
        public void run() {
            if (this.batchInProgress.get()) {
                return;
            }

            if (this.nextLogIndex < this.orderedLogBlocks.size()) {
                TreeBlock<Block> logBlock = this.orderedLogBlocks.get(this.nextLogIndex++);
                this.processBatch(List.of(logBlock), block -> TreeAnimationDisintegrate.this.disintegrateBlock(
                        block,
                        this.treeDefinition,
                        this.treeDefinitionManager,
                        this.useCustomSounds,
                        this.useCustomParticles
                ));
                return;
            }

            if (this.nextLeafIndex < this.shuffledLeafBlocks.size()) {
                List<TreeBlock<Block>> leafBatch = new ArrayList<>(2);
                leafBatch.add(this.shuffledLeafBlocks.get(this.nextLeafIndex++));
                if (this.nextLeafIndex < this.shuffledLeafBlocks.size()) {
                    leafBatch.add(this.shuffledLeafBlocks.get(this.nextLeafIndex++));
                }

                AtomicBoolean firstLeafWasDisintegrated = new AtomicBoolean();
                AtomicInteger leafIndex = new AtomicInteger();
                this.processBatch(leafBatch, leafBlock -> {
                    int index = leafIndex.getAndIncrement();
                    boolean wasDisintegrated = TreeAnimationDisintegrate.this.disintegrateBlock(
                            leafBlock,
                            this.treeDefinition,
                            this.treeDefinitionManager,
                            this.useCustomSounds && (index == 0 || !firstLeafWasDisintegrated.get()),
                            this.useCustomParticles
                    );
                    if (index == 0) {
                        firstLeafWasDisintegrated.set(wasDisintegrated);
                    }
                });
                return;
            }

            this.cancel();
            this.whenFinished.run();
        }

        private void processBatch(@NotNull List<TreeBlock<Block>> treeBlocks,
                                  @NotNull Consumer<TreeBlock<Block>> action) {
            this.batchInProgress.set(true);
            RegionBatchProcessor.processByRegion(
                    SongodaPlugin.getInstance(),
                    treeBlocks,
                    TreeBlock::getLocation,
                    action,
                    () -> this.batchInProgress.set(false)
            );
        }
    }
}
