package com.songoda.ultimatetimber.config.entry;

import com.songoda.core.SongodaPlugin;
import lombok.Getter;
import lombok.Setter;
import net.vortexdevelopment.vinject.annotation.lifecycle.OnLoad;
import net.vortexdevelopment.vinject.annotation.yaml.Key;
import net.vortexdevelopment.vinject.annotation.yaml.YamlItem;
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
        this.resolvedLogs = resolveConfiguredMaterials(this.logs, "log");
        this.resolvedLeaves = resolveConfiguredMaterials(this.leaves, "leaf");
        this.resolvedSapling = resolveConfiguredMaterial(this.sapling, "sapling");
        this.resolvedPlantableSoil = resolveConfiguredMaterials(this.plantableSoil, "soil");
        this.resolvedRequiredTools = resolveConfiguredMaterials(this.requiredTools, "tool");
    }

    private Set<Material> resolveConfiguredMaterials(List<String> configuredMaterials, String materialType) {
        Set<Material> resolvedMaterials = new HashSet<>();
        if (configuredMaterials == null) {
            return resolvedMaterials;
        }

        for (String configuredMaterial : configuredMaterials) {
            Material material = resolveConfiguredMaterial(configuredMaterial, materialType);
            if (material == null) {
                continue;
            }

            resolvedMaterials.add(material);
        }

        return resolvedMaterials;
    }

    private @Nullable Material resolveConfiguredMaterial(@Nullable String configuredMaterial, String materialType) {
        if (configuredMaterial == null) {
            return null;
        }

        String materialName = configuredMaterial.trim();
        if (materialName.isEmpty() || materialName.equals("{}") || materialName.equals("[]")) {
            return null;
        }

        Material material = Material.matchMaterial(materialName);
        if (material != null) {
            return material;
        }

        SongodaPlugin.getInstance().getLogger().warning("Invalid " + materialType + " material '" + configuredMaterial + "' in tree definition.");
        return null;
    }

    public @Nullable Material getResolvedSapling() {
        return this.resolvedSapling;
    }
}
