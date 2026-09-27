package com.songoda.ultimatetimber.test;

import com.songoda.ultimatetimber.utils.TreeGeometry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Block backed world for detection tests. Blocks are dynamic proxies so every call the production
 * code makes is visible: the counters show exactly how many block handles, block type reads and
 * locations a single detection needs.
 */
public final class FakeBlockWorld {

    private final UUID worldId = UUID.randomUUID();
    private final World world = this.createWorld();
    private final Map<Long, Material> materials = new HashMap<>();
    private final Map<Long, Block> blocks = new HashMap<>();

    private int relativeCalls;
    private int typeCalls;
    private int locationCalls;

    /**
     * Places a material at the given coordinates.
     *
     * @param x        The block x coordinate
     * @param y        The block y coordinate
     * @param z        The block z coordinate
     * @param material The material to place
     */
    public void set(int x, int y, int z, Material material) {
        this.materials.put(TreeGeometry.packBlockKey(x, y, z), material);
    }

    /**
     * Places a material only when the position is still air, which keeps trunks from being overwritten
     * by canopies in test worlds.
     *
     * @param x        The block x coordinate
     * @param y        The block y coordinate
     * @param z        The block z coordinate
     * @param material The material to place
     * @return True if the material was placed
     */
    public boolean setIfAir(int x, int y, int z, Material material) {
        if (this.materialAt(x, y, z) != Material.AIR) {
            return false;
        }

        set(x, y, z, material);
        return true;
    }

    /**
     * Gets the material at the given coordinates.
     *
     * @param x The block x coordinate
     * @param y The block y coordinate
     * @param z The block z coordinate
     * @return The material at the position, air when unset
     */
    public Material materialAt(int x, int y, int z) {
        Material material = this.materials.get(TreeGeometry.packBlockKey(x, y, z));
        return material == null ? Material.AIR : material;
    }

    /**
     * Gets the block handle at the given coordinates.
     *
     * @param x The block x coordinate
     * @param y The block y coordinate
     * @param z The block z coordinate
     * @return The block at the position
     */
    public Block at(int x, int y, int z) {
        return this.blocks.computeIfAbsent(TreeGeometry.packBlockKey(x, y, z), key -> createBlock(x, y, z));
    }

    /**
     * Resets the call counters.
     */
    public void resetCounters() {
        this.relativeCalls = 0;
        this.typeCalls = 0;
        this.locationCalls = 0;
    }

    /**
     * Gets the number of block handles created through {@code Block#getRelative}.
     *
     * @return The relative call count
     */
    public int relativeCalls() {
        return this.relativeCalls;
    }

    /**
     * Gets the number of block type reads.
     *
     * @return The type call count
     */
    public int typeCalls() {
        return this.typeCalls;
    }

    /**
     * Gets the number of locations created through {@code Block#getLocation}.
     *
     * @return The location call count
     */
    public int locationCalls() {
        return this.locationCalls;
    }

    private Block createBlock(int x, int y, int z) {
        return (Block) Proxy.newProxyInstance(Block.class.getClassLoader(), new Class<?>[]{Block.class}, (proxy, method, args) -> switch (method.getName()) {
            case "getX" -> x;
            case "getY" -> y;
            case "getZ" -> z;
            case "getWorld" -> this.world;
            case "getType" -> {
                this.typeCalls++;
                yield this.materialAt(x, y, z);
            }
            case "getRelative" -> {
                this.relativeCalls++;
                yield resolveRelative(x, y, z, args);
            }
            case "getLocation" -> {
                this.locationCalls++;
                yield new Location(this.world, x, y, z);
            }
            case "hashCode" -> (int) TreeGeometry.packBlockKey(x, y, z);
            case "equals" -> proxy == args[0];
            case "toString" -> "Block[" + x + ", " + y + ", " + z + "]";
            default -> throw new UnsupportedOperationException(method.getName());
        });
    }

    private World createWorld() {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUID" -> this.worldId;
                    case "hashCode" -> this.worldId.hashCode();
                    case "equals" -> proxy == args[0];
                    case "toString" -> "FakeWorld[" + this.worldId + "]";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private Block resolveRelative(int x, int y, int z, Object[] args) {
        if (args.length == 3) {
            return at(x + (int) args[0], y + (int) args[1], z + (int) args[2]);
        }

        BlockFace face = (BlockFace) args[0];
        int distance = args.length == 2 ? (int) args[1] : 1;
        return at(x + face.getModX() * distance, y + face.getModY() * distance, z + face.getModZ() * distance);
    }
}
