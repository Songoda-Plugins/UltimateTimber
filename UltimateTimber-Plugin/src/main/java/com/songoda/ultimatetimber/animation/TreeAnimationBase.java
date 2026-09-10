package com.songoda.ultimatetimber.animation;

import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.api.animation.TreeAnimation;
import com.songoda.ultimatetimber.api.animation.TreeAnimationType;
import com.songoda.ultimatetimber.api.manager.BlockReplacementManager;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockSet;
import com.songoda.ultimatetimber.tree.FallingTreeBlockImpl;
import com.songoda.ultimatetimber.utils.BlockUtils;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base implementation for tree felling animations.
 */
@Getter
public abstract class TreeAnimationBase implements TreeAnimation {

    protected final TreeAnimationType treeAnimationType;
    protected final DetectedTree detectedTree;
    protected final Player player;
    protected final boolean hasSilkTouch;
    protected TreeBlockSet<FallingBlock> fallingTreeBlocks;

    protected TreeAnimationBase(@NotNull TreeAnimationType treeAnimationType,
                                @NotNull DetectedTree detectedTree,
                                @NotNull Player player) {
        this.treeAnimationType = treeAnimationType;
        this.detectedTree = detectedTree;
        this.player = player;

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        this.hasSilkTouch = !itemInHand.getType().isAir()
                && itemInHand.hasItemMeta()
                && itemInHand.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH);

        this.fallingTreeBlocks = new TreeBlockSet<>();
    }

    @Override
    public boolean hasSilkTouch() {
        return this.hasSilkTouch;
    }

    @Override
    public @NotNull TreeBlockSet<FallingBlock> getFallingTreeBlocks() {
        return this.fallingTreeBlocks;
    }

    @Override
    public void removeFallingBlock(@NotNull FallingBlock fallingBlock) {
        for (TreeBlock<FallingBlock> fallingTreeBlock : this.fallingTreeBlocks.getAllTreeBlocks()) {
            if (fallingTreeBlock.getBlock().equals(fallingBlock)) {
                this.fallingTreeBlocks.remove(fallingTreeBlock);
                return;
            }
        }
    }

    /**
     * Converts a static world tree block into a physics-driven falling block entity.
     *
     * @param treeBlock The world tree block
     * @return The resulting FallingTreeBlock, or null if air
     */
    protected @Nullable TreeBlock<FallingBlock> convertToFallingBlock(@NotNull TreeBlock<Block> treeBlock) {
        Location location = treeBlock.getLocation().clone().add(0.5, 0.0, 0.5);
        Block block = treeBlock.getBlock();
        if (block.getType().isAir()) {
            replaceBlock(treeBlock);
            return null;
        }

        FallingBlock fallingBlock = BlockUtils.spawnFallingBlock(location, block.getBlockData());
        BlockUtils.configureFallingBlock(fallingBlock);

        TreeBlock<FallingBlock> fallingTreeBlock = new FallingTreeBlockImpl(fallingBlock, treeBlock.getTreeBlockType());
        replaceBlock(treeBlock);
        return fallingTreeBlock;
    }

    /**
     * Replaces the world tree block and schedules sapling replanting.
     *
     * @param treeBlock The tree block to replace
     */
    public void replaceBlock(@NotNull TreeBlock<Block> treeBlock) {
        UltimateTimber plugin = UltimateTimber.getInstance();
        BlockReplacementManager replacementManager = plugin.getBlockReplacementManager();
        if (replacementManager != null) {
            replacementManager.replaceBlock(treeBlock, this.detectedTree.getTreeDefinition());
        }
    }
}
