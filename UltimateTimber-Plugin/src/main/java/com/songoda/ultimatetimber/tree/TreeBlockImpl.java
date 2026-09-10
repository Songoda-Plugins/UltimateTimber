package com.songoda.ultimatetimber.tree;

import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockType;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TreeBlockImpl implements TreeBlock<Block> {
    private final Block block;
    private final TreeBlockType treeBlockType;

    public TreeBlockImpl(Block block, TreeBlockType treeBlockType) {
        this.block = block;
        this.treeBlockType = treeBlockType;
    }

    @Override
    public @NotNull Block getBlock() {
        return this.block;
    }

    @Override
    public @NotNull Location getLocation() {
        return this.block.getLocation();
    }

    @Override
    public @NotNull TreeBlockType getTreeBlockType() {
        return this.treeBlockType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.block, this.treeBlockType);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TreeBlockImpl)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        TreeBlockImpl oTreeBlock = (TreeBlockImpl) obj;
        return oTreeBlock.block.equals(this.block) && oTreeBlock.treeBlockType == this.treeBlockType;
    }
}
