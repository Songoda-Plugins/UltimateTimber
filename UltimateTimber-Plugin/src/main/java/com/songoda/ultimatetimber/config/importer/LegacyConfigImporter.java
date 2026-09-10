package com.songoda.ultimatetimber.config.importer;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Importer to detect legacy UltimateTimber configuration and migrate it to the modern bean format.
 */
public final class LegacyConfigImporter {

    private final File dataFolder;
    private final Logger logger;

    public LegacyConfigImporter(File dataFolder, Logger logger) {
        this.dataFolder = dataFolder;
        this.logger = logger;
    }

    /**
     * Checks whether the given config file is in legacy format.
     *
     * @param configFile The config.yml file to test
     * @return True if legacy format is detected
     */
    public boolean isLegacy(File configFile) {
        if (!configFile.exists()) {
            return false;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        return config.contains("server-type")
                || config.contains("max-logs-per-chop")
                || config.contains("leaves-required-for-tree")
                || config.contains("destroy-leaves")
                || config.contains("realistic-tool-damage");
    }

    /**
     * Imports and migrates the legacy config file if detected.
     *
     * @param configFile The config.yml file to migrate
     * @return True if migration occurred, false otherwise
     */
    public boolean importIfLegacy(File configFile) {
        if (!isLegacy(configFile)) {
            return false;
        }

        this.logger.info("[UltimateTimber] Detected legacy configuration format. Starting migration...");

        File backupFile = new File(this.dataFolder, "config-legacy.yml");
        try {
            Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            this.logger.info("[UltimateTimber] Backed up legacy configuration to " + backupFile.getName());
        } catch (IOException e) {
            this.logger.log(Level.SEVERE, "[UltimateTimber] Failed to back up legacy configuration file!", e);
            return false;
        }

        YamlConfiguration legacy = YamlConfiguration.loadConfiguration(backupFile);
        YamlConfiguration modern = new YamlConfiguration();

        // Top level settings
        modern.set("Locale", legacy.getString("locale", "en_US"));
        modern.set("Disabled Worlds", legacy.getStringList("disabled-worlds"));
        modern.set("Max Logs Per Chop", legacy.getInt("max-logs-per-chop", 150));
        modern.set("Leaves Required For Tree", legacy.getInt("leaves-required-for-tree", 5));
        modern.set("Destroy Leaves", legacy.getBoolean("destroy-leaves", true));
        modern.set("Realistic Tool Damage", legacy.getBoolean("realistic-tool-damage", true));
        modern.set("Protect Tool", legacy.getBoolean("protect-tool", false));
        modern.set("Apply Silk Touch", legacy.getBoolean("apply-silk-touch", true));
        modern.set("Apply Silk Touch Tool Damage", legacy.getBoolean("apply-silk-touch-tool-damage", true));
        modern.set("Break Entire Tree Base", legacy.getBoolean("break-entire-tree-base", false));
        modern.set("Destroy Initiated Block", legacy.getBoolean("destroy-initiated-block", false));
        modern.set("Only Detect Logs Upwards", legacy.getBoolean("only-detect-logs-upwards", true));
        modern.set("Only Topple While", legacy.getString("only-topple-while", "ALWAYS"));
        modern.set("Allow Creative Mode", legacy.getBoolean("allow-creative-mode", true));
        modern.set("Require Chop Permission", legacy.getBoolean("require-chop-permission", false));
        modern.set("Player Tree Topple Cooldown", legacy.getBoolean("player-tree-topple-cooldown", false));
        modern.set("Player Tree Topple Cooldown Length", legacy.getInt("player-tree-topple-cooldown-length", 5));
        modern.set("Ignore Required Tools", legacy.getBoolean("ignore-required-tools", false));
        modern.set("Replant Saplings", legacy.getBoolean("replant-saplings", true));
        modern.set("Always Replant Sapling", legacy.getBoolean("always-replant-sapling", false));
        modern.set("Replant Saplings Cooldown", legacy.getInt("replant-saplings-cooldown", 3));
        modern.set("Falling Blocks Replant Saplings", legacy.getBoolean("falling-blocks-replant-saplings", true));
        modern.set("Falling Blocks Replant Saplings Chance", legacy.getDouble("falling-blocks-replant-saplings-chance", 1.0));
        modern.set("Falling Blocks Deal Damage", legacy.getBoolean("falling-blocks-deal-damage", true));
        modern.set("Falling Block Damage", legacy.getInt("falling-block-damage", 1));
        modern.set("Add Items To Inventory", legacy.getBoolean("add-items-to-inventory", false));
        modern.set("Use Custom Sounds", legacy.getBoolean("use-custom-sounds", true));
        modern.set("Use Custom Particles", legacy.getBoolean("use-custom-particles", true));
        modern.set("Bonus Loot Multiplier", legacy.getDouble("bonus-loot-multiplier", 2.0));
        modern.set("Ignore Placed Blocks", legacy.getBoolean("ignore-placed-blocks", true));
        modern.set("Ignore Placed Blocks Memory Size", legacy.getInt("ignore-placed-blocks-memory-size", 5000));
        modern.set("Tree Animation Type", legacy.getString("tree-animation-type", "FANCY"));
        modern.set("Scatter Tree Blocks On Ground", legacy.getBoolean("scatter-tree-blocks-on-ground", false));
        modern.set("Fragile Blocks", legacy.getStringList("fragile-blocks"));

        // Queued block replacement
        modern.set("Queued Block Replacement.Mode", legacy.getString("queued-block-replacement", "NEVER"));
        modern.set("Queued Block Replacement.Threshold", legacy.getInt("queued-block-replacement-threshold", 20));
        modern.set("Queued Block Replacement.Max Per Tick", legacy.getInt("queued-block-replacement-max-per-tick", 1000));

        // Hooks
        modern.set("Hooks.Apply Experience", legacy.getBoolean("hooks-apply-experience", true));
        modern.set("Hooks.Apply Extra Drops", legacy.getBoolean("hooks-apply-extra-drops", true));
        modern.set("Hooks.Require Ability Active", legacy.getBoolean("hooks-require-ability-active", false));

        // Global loot
        modern.set("Global Loot.Plantable Soil", legacy.getStringList("global-plantable-soil"));
        modern.set("Global Loot.Required Tools", legacy.getStringList("global-required-tools"));
        modern.set("Global Loot.Required Axe", legacy.getBoolean("global-required-axe", false));
        migrateLootSection(legacy.getConfigurationSection("global-log-loot"), modern, "Global Loot.Log Loot");
        migrateLootSection(legacy.getConfigurationSection("global-leaf-loot"), modern, "Global Loot.Leaf Loot");
        migrateLootSection(legacy.getConfigurationSection("global-entire-tree-loot"), modern, "Global Loot.Entire Tree Loot");

        // Required axe
        if (legacy.contains("required-axe")) {
            modern.set("Required Axe.Type", legacy.getString("required-axe.type", "DIAMOND_AXE"));
            modern.set("Required Axe.Name", legacy.getString("required-axe.name", "<green>An Epic Axe"));
            modern.set("Required Axe.Lore", legacy.getStringList("required-axe.lore"));
            modern.set("Required Axe.Enchants", legacy.getStringList("required-axe.enchants"));
            modern.set("Required Axe.Nbt", legacy.getString("required-axe.nbt", "ultimatetimber_axe"));
        }

        // Trees
        ConfigurationSection treesSection = legacy.getConfigurationSection("trees");
        if (treesSection != null) {
            for (String key : treesSection.getKeys(false)) {
                ConfigurationSection tree = treesSection.getConfigurationSection(key);
                if (tree == null) {
                    continue;
                }

                String prefix = "Trees." + key + ".";
                modern.set(prefix + "Logs", tree.getStringList("logs"));
                modern.set(prefix + "Leaves", tree.getStringList("leaves"));
                modern.set(prefix + "Sapling", tree.getString("sapling"));
                modern.set(prefix + "Plantable Soil", tree.getStringList("plantable-soil"));
                modern.set(prefix + "Max Log Distance From Trunk", tree.getDouble("max-log-distance-from-trunk", 6.0));
                modern.set(prefix + "Max Leaf Distance From Log", tree.getInt("max-leaf-distance-from-log", 6));
                modern.set(prefix + "Search For Leaves Diagonally", tree.getBoolean("search-for-leaves-diagonally", false));
                modern.set(prefix + "Drop Original Log", tree.getBoolean("drop-original-log", true));
                modern.set(prefix + "Drop Original Leaf", tree.getBoolean("drop-original-leaf", false));
                modern.set(prefix + "Required Tools", tree.getStringList("required-tools"));
                modern.set(prefix + "Required Axe", tree.getBoolean("required-axe", false));

                migrateLootSection(tree.getConfigurationSection("log-loot"), modern, prefix + "Log Loot");
                migrateLootSection(tree.getConfigurationSection("leaf-loot"), modern, prefix + "Leaf Loot");
                migrateLootSection(tree.getConfigurationSection("entire-tree-loot"), modern, prefix + "Entire Tree Loot");
            }
        }

        try {
            modern.save(configFile);
            this.logger.info("[UltimateTimber] Successfully migrated legacy configuration to modern YAML structure.");
            return true;
        } catch (IOException e) {
            this.logger.log(Level.SEVERE, "[UltimateTimber] Failed to save modern configuration file after migration!", e);
            return false;
        }
    }

    private void migrateLootSection(ConfigurationSection section, YamlConfiguration modern, String targetPath) {
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection lootItem = section.getConfigurationSection(key);
            if (lootItem == null) {
                continue;
            }

            String itemPrefix = targetPath + "." + key + ".";
            if (lootItem.contains("material")) {
                modern.set(itemPrefix + "Material", lootItem.getString("material"));
            }
            if (lootItem.contains("command")) {
                modern.set(itemPrefix + "Command", lootItem.getString("command"));
            }
            modern.set(itemPrefix + "Chance", lootItem.getDouble("chance", 0.0));
        }
    }
}
