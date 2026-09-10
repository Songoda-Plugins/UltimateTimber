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
import java.util.List;

/**
 * Configuration DTO for the custom required axe item.
 */
@Getter
@Setter
@YamlItem
public class RequiredAxeConfig {

    @Key("Type")
    private String type = "DIAMOND_AXE";

    @Key("Name")
    private String name = "<green>An Epic Axe";

    @Key("Lore")
    private List<String> lore = new ArrayList<>(List.of(
            "<gray>This axe... it's awesome.",
            "<gray>It can chop down trees real fast."
    ));

    @Key("Enchants")
    private List<String> enchants = new ArrayList<>(List.of(
            "unbreaking:3",
            "efficiency:5"
    ));

    @Key("Nbt")
    private String nbt = "ultimatetimber_axe";

    private transient Material resolvedMaterial;

    @OnLoad
    public void onLoad() {
        if (this.type != null && !this.type.trim().isEmpty()) {
            this.resolvedMaterial = Material.matchMaterial(this.type.trim());
            if (this.resolvedMaterial == null) {
                Bukkit.getLogger().warning("[UltimateTimber] Warning: Invalid material '" + this.type + "' in required axe config.");
            }
        }
    }

    public @Nullable Material getResolvedMaterial() {
        return this.resolvedMaterial;
    }
}
