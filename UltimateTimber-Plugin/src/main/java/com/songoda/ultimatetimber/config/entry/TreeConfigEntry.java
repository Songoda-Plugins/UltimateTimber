package com.songoda.ultimatetimber.config.entry;

import lombok.Getter;
import lombok.Setter;
import net.vortexdevelopment.vinject.annotation.lifecycle.OnLoad;
import net.vortexdevelopment.vinject.annotation.yaml.Key;
import net.vortexdevelopment.vinject.annotation.yaml.YamlItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Configuration DTO for an individual tree species definition.
 */
@Getter
@Setter
@YamlItem
public class TreeConfigEntry {

    @Key("Logs")
    private List<String> logs = new ArrayList<>();

    @Key("Leaves")
    private List<String> leaves = new ArrayList<>();

    @Key("Sapling")
    private String sapling;

    @Key("Plantable Soil")
    private List<String> plantableSoil = new ArrayList<>();

    @Key("Max Log Distance From Trunk")
    private double maxLogDistanceFromTrunk = 6.0;

    @Key("Max Leaf Distance From Log")
    private int maxLeafDistanceFromLog = 6;

    @Key("Search For Leaves Diagonally")
    private boolean searchForLeavesDiagonally = false;

    @Key("Drop Original Log")
    private boolean dropOriginalLog = true;

    @Key("Drop Original Leaf")
    private boolean dropOriginalLeaf = false;

    @Key("Log Loot")
    private Map<String, LootConfigEntry> logLoot = new LinkedHashMap<>();

    @Key("Leaf Loot")
    private Map<String, LootConfigEntry> leafLoot = new LinkedHashMap<>();

    @Key("Entire Tree Loot")
    private Map<String, LootConfigEntry> entireTreeLoot = new LinkedHashMap<>();

    @Key("Required Tools")
    private List<String> requiredTools = new ArrayList<>();

    @Key("Required Axe")
    private boolean requiredAxe = false;

    private transient Set<Material> resolvedLogs = new HashSet<>();
    private transient Set<Material> resolvedLeaves = new HashSet<>();
    private transient Material resolvedSapling;
    private transient Set<Material> resolvedPlantableSoil = new HashSet<>();
    private transient Set<Material> resolvedRequiredTools = new HashSet<>();

    @OnLoad
    public void onLoad() {
        this.resolvedLogs = new HashSet<>();
        if (this.logs != null) {
            for (String item : this.logs) {
                if (item != null && !item.trim().isEmpty()) {
                    Material mat = Material.matchMaterial(item.trim());
                    if (mat != null) {
                        this.resolvedLogs.add(mat);
                    } else {
                        Bukkit.getLogger().warning("[UltimateTimber] Warning: Invalid log material '" + item + "' in tree definition.");
                    }
                }
            }
        }

        this.resolvedLeaves = new HashSet<>();
        if (this.leaves != null) {
            for (String item : this.leaves) {
                if (item != null && !item.trim().isEmpty()) {
                    Material mat = Material.matchMaterial(item.trim());
                    if (mat != null) {
                        this.resolvedLeaves.add(mat);
                    } else {
                        Bukkit.getLogger().warning("[UltimateTimber] Warning: Invalid leaf material '" + item + "' in tree definition.");
                    }
                }
            }
        }

        this.resolvedSapling = null;
        if (this.sapling != null && !this.sapling.trim().isEmpty()) {
            this.resolvedSapling = Material.matchMaterial(this.sapling.trim());
            if (this.resolvedSapling == null) {
                Bukkit.getLogger().warning("[UltimateTimber] Warning: Invalid sapling material '" + this.sapling + "' in tree definition.");
            }
        }

        this.resolvedPlantableSoil = new HashSet<>();
        if (this.plantableSoil != null) {
            for (String item : this.plantableSoil) {
                if (item != null && !item.trim().isEmpty()) {
                    Material mat = Material.matchMaterial(item.trim());
                    if (mat != null) {
                        this.resolvedPlantableSoil.add(mat);
                    } else {
                        Bukkit.getLogger().warning("[UltimateTimber] Warning: Invalid soil material '" + item + "' in tree definition.");
                    }
                }
            }
        }

        this.resolvedRequiredTools = new HashSet<>();
        if (this.requiredTools != null) {
            for (String item : this.requiredTools) {
                if (item != null && !item.trim().isEmpty()) {
                    Material mat = Material.matchMaterial(item.trim());
                    if (mat != null) {
                        this.resolvedRequiredTools.add(mat);
                    } else {
                        Bukkit.getLogger().warning("[UltimateTimber] Warning: Invalid tool material '" + item + "' in tree definition.");
                    }
                }
            }
        }
    }

    public @Nullable Material getResolvedSapling() {
        return this.resolvedSapling;
    }
}
