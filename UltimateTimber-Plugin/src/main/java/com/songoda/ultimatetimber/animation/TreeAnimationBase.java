package com.songoda.ultimatetimber.animation;

import com.songoda.core.SongodaPlugin;
import com.songoda.core.vortexcore.compatibility.EnchantmentResolver;
import com.songoda.ultimatetimber.api.UltimateTimberApi;
import com.songoda.ultimatetimber.api.animation.TreeAnimation;
import com.songoda.ultimatetimber.api.animation.TreeAnimationType;
import com.songoda.ultimatetimber.api.manager.BlockReplacementManager;
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockSet;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.tree.FallingTreeBlockImpl;
import com.songoda.ultimatetimber.utils.BlockUtils;
import com.songoda.core.vortexcore.compatibility.folia.SchedulerRunnable;
import com.songoda.core.vortexcore.compatibility.folia.SchedulerUtils;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntConsumer;

/**
 * Base implementation for tree felling animations.
 */
@Getter
public abstract class TreeAnimationBase implements TreeAnimation {

    protected final TreeAnimationType treeAnimationType;
    protected final DetectedTree detectedTree;
    protected final Player player;
    protected final boolean hasSilkTouch;
    protected final TimberConfig config;
    protected TreeBlockSet<FallingBlock> fallingTreeBlocks;
    @Getter(AccessLevel.NONE)
    private final Set<FallingBlock> trackedFallingBlocks = ConcurrentHashMap.newKeySet();
    @Getter(AccessLevel.NONE)
    private final Map<FallingBlock, SchedulerRunnable> fallingBlockTasks = new ConcurrentHashMap<>();

    protected TreeAnimationBase(@NotNull TreeAnimationType treeAnimationType,
                                @NotNull DetectedTree detectedTree,
                                @NotNull Player player,
                                @Nullable TimberConfig config) {
        this.treeAnimationType = treeAnimationType;
        this.detectedTree = detectedTree;
        this.player = player;
        this.config = config;

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        this.hasSilkTouch = !itemInHand.getType().isAir()
                && itemInHand.hasItemMeta()
                && itemInHand.getItemMeta().hasEnchant(EnchantmentResolver.resolve(NamespacedKey.minecraft("silk_touch")));

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
        if (!this.trackedFallingBlocks.remove(fallingBlock)) {
            return;
        }

        SchedulerRunnable task = this.fallingBlockTasks.remove(fallingBlock);
        if (task != null) {
            task.cancel();
        }

        synchronized (this.fallingTreeBlocks) {
            for (TreeBlock<FallingBlock> fallingTreeBlock : this.fallingTreeBlocks.getAllTreeBlocks()) {
                if (fallingTreeBlock.block().equals(fallingBlock)) {
                    this.fallingTreeBlocks.remove(fallingTreeBlock);
                    return;
                }
            }
        }
    }

    protected void trackFallingBlock(@NotNull TreeBlock<FallingBlock> fallingTreeBlock) {
        this.trackFallingBlock(fallingTreeBlock, 1L, 0L, ignored -> { });
    }

    protected void trackFallingBlock(@NotNull TreeBlock<FallingBlock> fallingTreeBlock,
                                     long delayTicks,
                                     long maxAgeTicks,
                                     @NotNull IntConsumer onTick) {
        FallingBlock fallingBlock = fallingTreeBlock.block();
        synchronized (this.fallingTreeBlocks) {
            this.fallingTreeBlocks.add(fallingTreeBlock);
        }

        if (!this.trackedFallingBlocks.add(fallingBlock)) {
            return;
        }

        SchedulerRunnable task = new SchedulerRunnable() {
            private int age;

            @Override
            public void run() {
                if (!fallingBlock.isValid() || fallingBlock.isDead()) {
                    TreeAnimationBase.this.removeFallingBlock(fallingBlock);
                    this.cancel();
                    return;
                }

                if (fallingBlock.isOnGround()) {
                    this.finishFallingBlock();
                    return;
                }

                this.age++;
                onTick.accept(this.age);
                if (maxAgeTicks > 0 && this.age > maxAgeTicks) {
                    this.finishFallingBlock();
                }
            }

            private void finishFallingBlock() {
                if (UltimateTimberApi.getTreeAnimationManager() != null) {
                    UltimateTimberApi.getTreeAnimationManager().runFallingBlockImpact(TreeAnimationBase.this, fallingTreeBlock);
                    fallingBlock.remove();
                } else {
                    TreeAnimationBase.this.removeFallingBlock(fallingBlock);
                    fallingBlock.remove();
                }
            }
        };

        this.fallingBlockTasks.put(fallingBlock, task);
        SchedulerUtils.runEntityTaskTimer(SongodaPlugin.getInstance(), fallingBlock, task, delayTicks, 1L);
    }

    protected boolean hasFallingBlocks() {
        synchronized (this.fallingTreeBlocks) {
            return !this.fallingTreeBlocks.isEmpty();
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
        Block block = treeBlock.block();
        if (block.getType().isAir()) {
            replaceBlock(treeBlock);
            return null;
        }

        FallingBlock fallingBlock = BlockUtils.spawnFallingBlock(location, block.getBlockData());
        BlockUtils.configureFallingBlock(fallingBlock);

        TreeBlock<FallingBlock> fallingTreeBlock = new FallingTreeBlockImpl(fallingBlock, treeBlock.treeBlockType());
        replaceBlock(treeBlock);
        return fallingTreeBlock;
    }

    /**
     * Replaces the world tree block and schedules sapling replanting.
     *
     * @param treeBlock The tree block to replace
     */
    public void replaceBlock(@NotNull TreeBlock<Block> treeBlock) {
        BlockReplacementManager replacementManager = UltimateTimberApi.getBlockReplacementManager();
        if (replacementManager != null) {
            replacementManager.replaceBlock(treeBlock, this.detectedTree.treeDefinition());
        }
    }
}
