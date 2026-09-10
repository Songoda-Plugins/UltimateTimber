package com.songoda.ultimatetimber.tree;

import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import com.songoda.ultimatetimber.api.tree.TreeLoot;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * Concrete implementation of {@link TreeDefinition}.
 */
public class TreeDefinitionImpl implements TreeDefinition {
    private final String key;
    private final Set<Material> logMaterials;
    private final Set<Material> leafMaterials;
    private final Material saplingMaterial;
    private final Set<Material> plantableSoilMaterials;
    @Getter private final double maxLogDistanceFromTrunk;
    @Getter private final int maxLeafDistanceFromLog;
    private final boolean detectLeavesDiagonally;
    private final boolean dropOriginalLog;
    private final boolean dropOriginalLeaf;
    private final Set<TreeLoot> logLoot;
    private final Set<TreeLoot> leafLoot;
    private final Set<TreeLoot> entireTreeLoot;
    private final Set<ItemStack> requiredTools;
    @Getter private final boolean requiredAxe;

    public TreeDefinitionImpl(@NotNull String key,
                              @NotNull Set<Material> logMaterials,
                              @NotNull Set<Material> leafMaterials,
                              @Nullable Material saplingMaterial,
                              @NotNull Set<Material> plantableSoilMaterials,
                              double maxLogDistanceFromTrunk,
                              int maxLeafDistanceFromLog,
                              boolean detectLeavesDiagonally,
                              boolean dropOriginalLog,
                              boolean dropOriginalLeaf,
                              @NotNull Set<TreeLoot> logLoot,
                              @NotNull Set<TreeLoot> leafLoot,
                              @NotNull Set<TreeLoot> entireTreeLoot,
                              @NotNull Set<ItemStack> requiredTools,
                              boolean requiredAxe) {
        this.key = key;
        this.logMaterials = Collections.unmodifiableSet(logMaterials);
        this.leafMaterials = Collections.unmodifiableSet(leafMaterials);
        this.saplingMaterial = saplingMaterial;
        this.plantableSoilMaterials = Collections.unmodifiableSet(plantableSoilMaterials);
        this.maxLogDistanceFromTrunk = maxLogDistanceFromTrunk;
        this.maxLeafDistanceFromLog = maxLeafDistanceFromLog;
        this.detectLeavesDiagonally = detectLeavesDiagonally;
        this.dropOriginalLog = dropOriginalLog;
        this.dropOriginalLeaf = dropOriginalLeaf;
        this.logLoot = Collections.unmodifiableSet(logLoot);
        this.leafLoot = Collections.unmodifiableSet(leafLoot);
        this.entireTreeLoot = Collections.unmodifiableSet(entireTreeLoot);
        this.requiredTools = Collections.unmodifiableSet(requiredTools);
        this.requiredAxe = requiredAxe;
    }

    @Override
    public @NotNull String getKey() {
        return this.key;
    }

    @Override
    public @NotNull Set<Material> getLogMaterials() {
        return this.logMaterials;
    }

    @Override
    public @NotNull Set<Material> getLeafMaterials() {
        return this.leafMaterials;
    }

    @Override
    public @Nullable Material getSaplingMaterial() {
        return this.saplingMaterial;
    }

    @Override
    public @NotNull Set<Material> getPlantableSoilMaterials() {
        return this.plantableSoilMaterials;
    }

    @Override
    public boolean shouldDetectLeavesDiagonally() {
        return this.detectLeavesDiagonally;
    }

    @Override
    public boolean shouldDropOriginalLog() {
        return this.dropOriginalLog;
    }

    @Override
    public boolean shouldDropOriginalLeaf() {
        return this.dropOriginalLeaf;
    }

    @Override
    public @NotNull Set<TreeLoot> getLogLoot() {
        return this.logLoot;
    }

    @Override
    public @NotNull Set<TreeLoot> getLeafLoot() {
        return this.leafLoot;
    }

    @Override
    public @NotNull Set<TreeLoot> getEntireTreeLoot() {
        return this.entireTreeLoot;
    }

    @Override
    public @NotNull Set<ItemStack> getRequiredTools() {
        return this.requiredTools;
    }
}
