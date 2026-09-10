package com.songoda.ultimatetimber.animation;

import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.api.animation.TreeAnimationType;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.utils.ParticleUtils;
import com.songoda.ultimatetimber.utils.SoundUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Instantaneous tree felling animation with no physics falling blocks.
 */
public class TreeAnimationNone extends TreeAnimationBase {

    public TreeAnimationNone(@NotNull DetectedTree detectedTree, @NotNull Player player) {
        super(TreeAnimationType.NONE, detectedTree, player);
    }

    @Override
    public void playAnimation(@NotNull Runnable whenFinished) {
        UltimateTimber plugin = UltimateTimber.getInstance();
        TreeDefinitionManager treeDefinitionManager = plugin.getTreeDefinitionManager();
        TimberConfig config = plugin.getTimberConfig();

        boolean useCustomSound = config == null || config.isUseCustomSounds();
        boolean useCustomParticles = config == null || config.isUseCustomParticles();

        if (useCustomSound && this.detectedTree.getDetectedTreeBlocks().getInitialLogBlock() != null) {
            SoundUtils.playFallingSound(this.detectedTree.getDetectedTreeBlocks().getInitialLogBlock());
        }

        if (useCustomParticles) {
            for (TreeBlock<Block> treeBlock : this.detectedTree.getDetectedTreeBlocks().getAllTreeBlocks()) {
                ParticleUtils.playFallingParticles(treeBlock);
            }
        }

        for (TreeBlock<Block> treeBlock : this.detectedTree.getDetectedTreeBlocks().getAllTreeBlocks()) {
            if (treeDefinitionManager != null) {
                treeDefinitionManager.dropTreeLoot(this.detectedTree.getTreeDefinition(), treeBlock, this.player, this.hasSilkTouch, false);
            }
            replaceBlock(treeBlock);
        }

        whenFinished.run();
    }
}
