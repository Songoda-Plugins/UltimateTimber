package com.songoda.ultimatetimber.manager;

import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.api.manager.SaplingManager;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockType;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import com.songoda.ultimatetimber.config.TimberConfig;
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.annotation.component.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Service handling sapling replanting and temporary protection after tree felling.
 */
@Component
public class SaplingManagerImpl implements SaplingManager {

    @Inject
    private TimberConfig config;

    @Inject
    private TreeDefinitionManager treeDefinitionManager;

    private final Random random = new Random();
    private final Set<Location> protectedSaplings = new HashSet<>();

    @Override
    public void replantSapling(@NotNull TreeDefinition treeDefinition, @NotNull TreeBlock<?> treeBlock) {
        if (this.config != null && !this.config.isReplantSaplings()) {
            return;
        }

        Block block = treeBlock.getLocation().getBlock();
        if (!block.getType().isAir() || treeBlock.getTreeBlockType() == TreeBlockType.LEAF) {
            return;
        }

        UltimateTimber plugin = UltimateTimber.getInstance();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> internalReplant(treeDefinition, treeBlock), 1L);
    }

    @Override
    public void replantSaplingWithChance(@NotNull TreeDefinition treeDefinition, @NotNull TreeBlock<?> treeBlock) {
        if (this.config == null || !this.config.isFallingBlocksReplantSaplings() || !treeBlock.getLocation().getBlock().getType().isAir()) {
            return;
        }

        double chance = this.config.getFallingBlocksReplantSaplingsChance();
        if (this.random.nextDouble() > chance / 100.0) {
            return;
        }

        UltimateTimber plugin = UltimateTimber.getInstance();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> internalReplant(treeDefinition, treeBlock), 1L);
    }

    @Override
    public boolean isSaplingProtected(@NotNull Block block) {
        return this.protectedSaplings.contains(block.getLocation());
    }

    private void internalReplant(@NotNull TreeDefinition treeDefinition, @NotNull TreeBlock<?> treeBlock) {
        Block block = treeBlock.getLocation().getBlock();
        Block blockBelow = block.getRelative(BlockFace.DOWN);

        Set<Material> plantableSoil = this.treeDefinitionManager.getPlantableSoilMaterials(treeDefinition);
        if (!plantableSoil.contains(blockBelow.getType())) {
            return;
        }

        Material saplingMaterial = treeDefinition.getSaplingMaterial();
        if (saplingMaterial == null || saplingMaterial.isAir()) {
            return;
        }

        block.setType(saplingMaterial);

        int cooldown = this.config != null ? this.config.getReplantSaplingsCooldown() : 3;
        if (cooldown > 0) {
            Location loc = block.getLocation();
            this.protectedSaplings.add(loc);
            UltimateTimber plugin = UltimateTimber.getInstance();
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> this.protectedSaplings.remove(loc), cooldown * 20L);
        }
    }
}
