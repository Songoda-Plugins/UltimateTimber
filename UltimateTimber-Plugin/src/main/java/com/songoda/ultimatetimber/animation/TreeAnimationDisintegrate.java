package com.songoda.ultimatetimber.animation;

import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.api.animation.TreeAnimationType;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockType;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.utils.ParticleUtils;
import com.songoda.ultimatetimber.utils.SoundUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Animation where tree blocks disintegrate into particles and drops in place.
 */
public class TreeAnimationDisintegrate extends TreeAnimationBase {

    public TreeAnimationDisintegrate(@NotNull DetectedTree detectedTree, @NotNull Player player) {
        super(TreeAnimationType.DISINTEGRATE, detectedTree, player);
    }

    @Override
    public void playAnimation(@NotNull Runnable whenFinished) {
        UltimateTimber plugin = UltimateTimber.getInstance();
        TreeDefinitionManager treeDefinitionManager = plugin.getTreeDefinitionManager();
        TimberConfig config = plugin.getTimberConfig();

        boolean useCustomSound = config == null || config.isUseCustomSounds();
        boolean useCustomParticles = config == null || config.isUseCustomParticles();

        List<TreeBlock<Block>> orderedLogBlocks = new ArrayList<>(this.detectedTree.getDetectedTreeBlocks().getLogBlocks());
        orderedLogBlocks.sort(Comparator.comparingInt(x -> x.getLocation().getBlockY()));

        List<TreeBlock<Block>> leafBlocks = new ArrayList<>(this.detectedTree.getDetectedTreeBlocks().getLeafBlocks());
        Collections.shuffle(leafBlocks);

        Player p = this.player;
        TreeDefinition td = this.detectedTree.getTreeDefinition();
        boolean hst = this.hasSilkTouch;

        new BukkitRunnable() {
            @Override
            public void run() {
                List<TreeBlock<Block>> toDestroy = new ArrayList<>();

                if (!orderedLogBlocks.isEmpty()) {
                    TreeBlock<Block> treeBlock = orderedLogBlocks.remove(0);
                    toDestroy.add(treeBlock);
                } else if (!leafBlocks.isEmpty()) {
                    TreeBlock<Block> treeBlock = leafBlocks.remove(0);
                    toDestroy.add(treeBlock);

                    if (!leafBlocks.isEmpty()) {
                        treeBlock = leafBlocks.remove(0);
                        toDestroy.add(treeBlock);
                    }
                }

                for (TreeBlock<FallingBlock> fallingTreeBlock : TreeAnimationDisintegrate.this.fallingTreeBlocks.getAllTreeBlocks()) {
                    FallingBlock fallingBlock = fallingTreeBlock.getBlock();
                    fallingBlock.setVelocity(fallingBlock.getVelocity().clone().subtract(new Vector(0, 0.05, 0)));
                }

                if (!toDestroy.isEmpty()) {
                    TreeBlock<Block> first = toDestroy.get(0);
                    if (useCustomSound) {
                        SoundUtils.playLandingSound(first);
                    }

                    for (TreeBlock<Block> treeBlock : toDestroy) {
                        if (treeBlock.getTreeBlockType() == TreeBlockType.LOG) {
                            if (!td.getLogMaterials().contains(treeBlock.getBlock().getType())) {
                                continue;
                            }
                        } else if (treeBlock.getTreeBlockType() == TreeBlockType.LEAF) {
                            if (!td.getLeafMaterials().contains(treeBlock.getBlock().getType())) {
                                continue;
                            }
                        }

                        if (useCustomParticles) {
                            ParticleUtils.playFallingParticles(treeBlock);
                        }
                        if (treeDefinitionManager != null) {
                            treeDefinitionManager.dropTreeLoot(td, treeBlock, p, hst, false);
                        }
                        TreeAnimationDisintegrate.this.replaceBlock(treeBlock);
                    }
                } else {
                    this.cancel();
                    whenFinished.run();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
