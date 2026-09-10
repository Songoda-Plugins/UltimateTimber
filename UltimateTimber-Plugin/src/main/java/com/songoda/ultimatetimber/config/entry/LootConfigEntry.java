package com.songoda.ultimatetimber.config.entry;

import lombok.Getter;
import lombok.Setter;
import net.vortexdevelopment.vinject.annotation.lifecycle.OnLoad;
import net.vortexdevelopment.vinject.annotation.yaml.Key;
import net.vortexdevelopment.vinject.annotation.yaml.YamlItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

/**
 * Configuration DTO for an individual loot drop item or command.
 */
@Getter
@Setter
@YamlItem
public class LootConfigEntry {

    @Key("Material")
    private String material;

    @Key("Command")
    private String command;

    @Key("Chance")
    private double chance = 0.0;

    private transient Material resolvedMaterial;

    @OnLoad
    public void onLoad() {
        if (this.material != null && !this.material.trim().isEmpty()) {
            this.resolvedMaterial = Material.matchMaterial(this.material.trim());
            if (this.resolvedMaterial == null) {
                Bukkit.getLogger().warning("[UltimateTimber] Warning: Invalid material '" + this.material + "' in loot entry - skipping material drop.");
            }
        }
    }

    public @Nullable Material getResolvedMaterial() {
        return this.resolvedMaterial;
    }
}
