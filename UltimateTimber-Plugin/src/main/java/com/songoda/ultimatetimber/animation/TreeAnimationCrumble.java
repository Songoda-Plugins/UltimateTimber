package com.songoda.ultimatetimber.animation;

import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.api.animation.TreeAnimationType;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockSet;
import com.songoda.ultimatetimber.api.tree.TreeBlockType;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.utils.BlockUtils;
import com.songoda.ultimatetimber.utils.ParticleUtils;
import com.songoda.ultimatetimber.utils.SoundUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Animation where tree blocks crumble down layer by layer.
 */
public class TreeAnimationCrumble extends TreeAnimationBase {

    public TreeAnimationCrumble(@NotNull DetectedTree detectedTree, @NotNull Player player) {
        super(TreeAnimationType.CRUMBLE, detectedTree, player);
    }

    @Override
    public void playAnimation(@NotNull Runnable whenFinished) {
        UltimateTimber plugin = UltimateTimber.getInstance();
        TimberConfig config = plugin.getTimberConfig();

        boolean useCustomSound = config == null || config.isUseCustomSounds();
        boolean useCustomParticles = config == null || config.isUseCustomParticles();

        int currentY = -1;
        List<List<TreeBlock<Block>>> treeBlocks = new ArrayList<>();
        List<TreeBlock<Block>> currentPartition = new ArrayList<>();
        List<TreeBlock<Block>> orderedDetectedTreeBlocks = new ArrayList<>(this.detectedTree.getDetectedTreeBlocks().getAllTreeBlocks());
        orderedDetectedTreeBlocks.sort(Comparator.comparingInt(x -> x.getLocation().getBlockY()));

        for (TreeBlock<Block> treeBlock : orderedDetectedTreeBlocks) {
            if (currentY != treeBlock.getLocation().getBlockY()) {
                Collections.shuffle(currentPartition);
                treeBlocks.add(new ArrayList<>(currentPartition));
                currentPartition.clear();
                currentY = treeBlock.getLocation().getBlockY();
            }
            currentPartition.add(treeBlock);
        }

        Collections.shuffle(currentPartition);
        treeBlocks.add(new ArrayList<>(currentPartition));

        TreeDefinition td = this.detectedTree.getTreeDefinition();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!treeBlocks.isEmpty()) {
                    List<TreeBlock<Block>> partition = treeBlocks.get(0);
                    for (int i = 0; i < 3 && !partition.isEmpty(); i++) {
                        TreeBlock<Block> treeBlock = partition.remove(0);
                        if (treeBlock.getTreeBlockType() == TreeBlockType.LOG) {
                            if (!td.getLogMaterials().contains(treeBlock.getBlock().getType())) {
                                continue;
                            }
                        } else if (treeBlock.getTreeBlockType() == TreeBlockType.LEAF) {
                            if (!td.getLeafMaterials().contains(treeBlock.getBlock().getType())) {
                                continue;
                            }
                        }

                        TreeBlock<org.bukkit.entity.FallingBlock> fallingTreeBlock = TreeAnimationCrumble.this.convertToFallingBlock(treeBlock);
                        if (fallingTreeBlock == null) {
                            continue;
                        }

                        BlockUtils.toggleGravityFallingBlock(fallingTreeBlock.getBlock(), true);
                        fallingTreeBlock.getBlock().setVelocity(Vector.getRandom().setY(0).subtract(new Vector(0.5, 0, 0.5)).multiply(0.15));

                        if (TreeAnimationCrumble.this.fallingTreeBlocks == null) {
                            TreeAnimationCrumble.this.fallingTreeBlocks = new TreeBlockSet<>(fallingTreeBlock);
                        } else {
                            TreeAnimationCrumble.this.fallingTreeBlocks.add(fallingTreeBlock);
                        }

                        if (useCustomSound) {
                            SoundUtils.playLandingSound(treeBlock);
                        }
                        if (useCustomParticles) {
                            ParticleUtils.playFallingParticles(treeBlock);
                        }
                    }

                    if (partition.isEmpty()) {
                        treeBlocks.remove(0);
                    }
                }

                if (treeBlocks.isEmpty() && TreeAnimationCrumble.this.fallingTreeBlocks.getAllTreeBlocks().isEmpty()) {
                    whenFinished.run();
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
