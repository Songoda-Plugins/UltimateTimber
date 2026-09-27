package com.songoda.ultimatetimber.config.entry;

import com.songoda.core.SongodaPlugin;
import lombok.Getter;
import lombok.Setter;
import net.vortexdevelopment.vinject.annotation.lifecycle.OnLoad;
import net.vortexdevelopment.vinject.annotation.yaml.Key;
import net.vortexdevelopment.vinject.annotation.yaml.YamlItem;
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
        this.resolvedPlantableSoil = resolveMaterials(this.plantableSoil, "global plantable soil");
        this.resolvedRequiredTools = resolveMaterials(this.requiredTools, "global required tools");
    }

    private Set<Material> resolveMaterials(List<String> configuredMaterials, String settingName) {
        Set<Material> resolvedMaterials = new HashSet<>();
        if (configuredMaterials == null) {
            return resolvedMaterials;
        }

        for (String configuredMaterial : configuredMaterials) {
            if (configuredMaterial == null) {
                continue;
            }

            String materialName = configuredMaterial.trim();
            if (materialName.isEmpty() || materialName.equals("{}") || materialName.equals("[]")) {
                continue;
            }

            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                SongodaPlugin.getInstance().getLogger().warning("[UltimateTimber] Warning: Invalid material '" + configuredMaterial + "' in " + settingName + ".");
                continue;
            }

            resolvedMaterials.add(material);
        }

        return resolvedMaterials;
    }
}
