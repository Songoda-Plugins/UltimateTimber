package com.songoda.ultimatetimber.config.entry;

import lombok.Getter;
import lombok.Setter;
import net.vortexdevelopment.vinject.annotation.lifecycle.OnLoad;
import net.vortexdevelopment.vinject.annotation.yaml.Key;
import net.vortexdevelopment.vinject.annotation.yaml.YamlItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Configuration DTO for global tree loot, tools, and plantable soil.
 */
@Getter
@Setter
@YamlItem
public class GlobalLootConfig {

    @Key("Plantable Soil")
    private List<String> plantableSoil = new ArrayList<>(List.of(
            "DIRT",
            "GRASS_BLOCK",
            "COARSE_DIRT",
            "PODZOL",
            "ROOTED_DIRT"
    ));

    @Key("Log Loot")
    private Map<String, LootConfigEntry> logLoot = new LinkedHashMap<>();

    @Key("Leaf Loot")
    private Map<String, LootConfigEntry> leafLoot = new LinkedHashMap<>();

    @Key("Entire Tree Loot")
    private Map<String, LootConfigEntry> entireTreeLoot = new LinkedHashMap<>();

    @Key("Required Tools")
    private List<String> requiredTools = new ArrayList<>(List.of(
            "WOODEN_AXE",
            "STONE_AXE",
            "IRON_AXE",
            "GOLDEN_AXE",
            "DIAMOND_AXE",
            "NETHERITE_AXE"
    ));

    @Key("Required Axe")
    private boolean requiredAxe = false;

    private transient Set<Material> resolvedPlantableSoil = new HashSet<>();
    private transient Set<Material> resolvedRequiredTools = new HashSet<>();

    @OnLoad
    public void onLoad() {
        this.resolvedPlantableSoil = new HashSet<>();
        if (this.plantableSoil != null) {
            for (String soil : this.plantableSoil) {
                if (soil != null && !soil.trim().isEmpty()) {
                    Material mat = Material.matchMaterial(soil.trim());
                    if (mat != null) {
                        this.resolvedPlantableSoil.add(mat);
                    } else {
                        Bukkit.getLogger().warning("[UltimateTimber] Warning: Invalid material '" + soil + "' in global plantable soil.");
                    }
                }
            }
        }

        this.resolvedRequiredTools = new HashSet<>();
        if (this.requiredTools != null) {
            for (String tool : this.requiredTools) {
                if (tool != null && !tool.trim().isEmpty()) {
                    Material mat = Material.matchMaterial(tool.trim());
                    if (mat != null) {
                        this.resolvedRequiredTools.add(mat);
                    } else {
                        Bukkit.getLogger().warning("[UltimateTimber] Warning: Invalid material '" + tool + "' in global required tools.");
                    }
                }
            }
        }
    }
}
