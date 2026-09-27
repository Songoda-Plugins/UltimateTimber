package com.songoda.ultimatetimber.animation;

import com.songoda.ultimatetimber.api.UltimateTimberApi;
import com.songoda.ultimatetimber.api.animation.TreeAnimationType;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
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
import java.util.List;

/**
 * Instantaneous tree felling animation with no physics falling blocks.
 */
public class TreeAnimationNone extends TreeAnimationBase {

    public TreeAnimationNone(@NotNull DetectedTree detectedTree,
                             @NotNull Player player,
                             @Nullable TimberConfig config) {
        super(TreeAnimationType.NONE, detectedTree, player, config);
    }

    @Override
    public void playAnimation(@NotNull Runnable whenFinished) {
        Plugin plugin = UltimateTimberApi.getPlugin();
        TreeDefinitionManager treeDefinitionManager = UltimateTimberApi.getTreeDefinitionManager();
        TimberConfig config = this.config;

        boolean useCustomSound = config == null || config.isUseCustomSounds();
        boolean useCustomParticles = config == null || config.isUseCustomParticles();

        TreeBlock<Block> initialLog = this.detectedTree.detectedTreeBlocks().getInitialLogBlock();
        List<TreeBlock<Block>> treeBlocks = new ArrayList<>(this.detectedTree.detectedTreeBlocks().getAllTreeBlocks());
        RegionBatchProcessor.processByRegion(
                plugin,
                treeBlocks,
                TreeBlock::getLocation,
                treeBlock -> this.processBlock(
                        treeBlock,
                        initialLog,
                        treeDefinitionManager,
                        useCustomSound,
                        useCustomParticles
                ),
                whenFinished
        );
    }

    private void processBlock(@NotNull TreeBlock<Block> treeBlock,
                              @Nullable TreeBlock<Block> initialLog,
                              @Nullable TreeDefinitionManager treeDefinitionManager,
                              boolean useCustomSound,
                              boolean useCustomParticles) {
        if (useCustomSound && treeBlock == initialLog) {
            SoundUtils.playFallingSound(treeBlock);
        }
        if (useCustomParticles) {
            ParticleUtils.playFallingParticles(treeBlock);
        }
        if (treeDefinitionManager != null) {
            treeDefinitionManager.dropTreeLoot(this.detectedTree.treeDefinition(), treeBlock, this.player, this.hasSilkTouch, false);
        }
        this.replaceBlock(treeBlock);
    }
}
