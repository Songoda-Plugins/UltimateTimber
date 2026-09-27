package com.songoda.ultimatetimber.tree;

import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockType;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class TreeBlockImpl implements TreeBlock<Block> {

    private final Block block;
    private final TreeBlockType treeBlockType;
    private final Location location;
    private final UUID worldId;
    private final int blockX;
    private final int blockY;
    private final int blockZ;

    public TreeBlockImpl(@NotNull Block block, @NotNull TreeBlockType treeBlockType) {
        this.block = block;
        this.treeBlockType = treeBlockType;
        this.location = null;
        this.worldId = block.getWorld().getUID();
        this.blockX = block.getX();
        this.blockY = block.getY();
        this.blockZ = block.getZ();
    }

    public TreeBlockImpl(@NotNull Block block, @NotNull TreeBlockType treeBlockType, @NotNull Location location) {
        this.block = block;
        this.treeBlockType = treeBlockType;
        this.location = location.clone();
        this.worldId = location.getWorld().getUID();
        this.blockX = location.getBlockX();
        this.blockY = location.getBlockY();
        this.blockZ = location.getBlockZ();
    }

    @Override
    public @NotNull Block block() {
        return this.block;
    }

    @Override
    public @NotNull Location getLocation() {
        return this.location == null ? this.block.getLocation() : this.location.clone();
    }

    @Override
    public @NotNull TreeBlockType treeBlockType() {
        return this.treeBlockType;
    }

    @Override
    public int hashCode() {
        int result = this.worldId.hashCode();
        result = 31 * result + this.blockX;
        result = 31 * result + this.blockY;
        result = 31 * result + this.blockZ;
        return 31 * result + this.treeBlockType.ordinal();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TreeBlockImpl other)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        return this.worldId.equals(other.worldId)
                && this.blockX == other.blockX
                && this.blockY == other.blockY
                && this.blockZ == other.blockZ
                && this.treeBlockType == other.treeBlockType;
    }
}
