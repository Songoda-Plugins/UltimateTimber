package com.songoda.ultimatetimber.tree;

import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockType;
import org.bukkit.Location;
import org.bukkit.entity.FallingBlock;
import org.jetbrains.annotations.NotNull;

public class FallingTreeBlockImpl implements TreeBlock<FallingBlock> {
    private final FallingBlock fallingBlock;
    private final TreeBlockType treeBlockType;

    public FallingTreeBlockImpl(@NotNull FallingBlock fallingBlock, @NotNull TreeBlockType treeBlockType) {
        this.fallingBlock = fallingBlock;
        this.treeBlockType = treeBlockType;
    }

    @Override
    public @NotNull FallingBlock getBlock() {
        return this.fallingBlock;
    }

    @Override
    public @NotNull Location getLocation() {
        return this.fallingBlock.getLocation();
    }

    @Override
    public @NotNull TreeBlockType getTreeBlockType() {
        return this.treeBlockType;
    }
}
