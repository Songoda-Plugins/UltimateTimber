package com.songoda.ultimatetimber.config.importer;

import com.songoda.core.SongodaPlugin;
import net.vortexdevelopment.vinject.config.yaml.YamlConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Importer to detect legacy UltimateTimber configuration and migrate it to the modern bean format.
 */
public final class LegacyConfigImporter {

    /**
     * Legacy Songoda configuration files were commonly saved as Windows-1252.
     * Reading those bytes as UTF-8 replaces every non-ASCII character with a
     * replacement character, which permanently corrupts localized text during
     * migration, so such files are re-decoded instead.
     */
    private static final Charset LEGACY_CHARSET = Charset.forName("windows-1252");

    private final File dataFolder;
    private final Logger logger = resolveLogger();

    public LegacyConfigImporter(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    /**
     * Inspects the configuration file, strips any UTF-8 Byte Order Mark (BOM),
     * and ensures the file is valid UTF-8. If non-UTF-8 bytes (e.g. Windows-1252)
     * are detected, it re-encodes the file cleanly to prevent MalformedInputException.
     *
     * @param configFile The configuration file to sanitize
     */
    public static void sanitizeEncoding(File configFile) {
        Logger logger = resolveLogger();
        if (configFile == null || !configFile.isFile()) {
            return;
        }

        try {
            byte[] bytes = Files.readAllBytes(configFile.toPath());
            if (bytes.length == 0) {
                return;
            }

            boolean hasBom = bytes.length >= 3
                    && (bytes[0] & 0xFF) == 0xEF
                    && (bytes[1] & 0xFF) == 0xBB
                    && (bytes[2] & 0xFF) == 0xBF;

            byte[] cleanBytes = hasBom
                    ? Arrays.copyOfRange(bytes, 3, bytes.length)
                    : bytes;

            java.nio.charset.CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);

            try {
                decoder.decode(ByteBuffer.wrap(cleanBytes));
            } catch (CharacterCodingException e) {
                String decoded = new String(cleanBytes, LEGACY_CHARSET);
                Files.writeString(configFile.toPath(), decoded, StandardCharsets.UTF_8);
                logger.info("[UltimateTimber] Converted config.yml from legacy encoding to clean UTF-8.");
                return;
            }

            if (hasBom) {
                Files.write(configFile.toPath(), cleanBytes);
                logger.info("[UltimateTimber] Stripped UTF-8 BOM from config.yml.");
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "[UltimateTimber] Could not verify/sanitize configuration file encoding.", e);
        }
    }

    private static Logger resolveLogger() {
        SongodaPlugin plugin = SongodaPlugin.getInstance();
        return plugin == null ? Logger.getLogger(LegacyConfigImporter.class.getName()) : plugin.getLogger();
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

        YamlConfiguration config;
        try {
            config = readConfiguration(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            this.logger.log(Level.WARNING, "[UltimateTimber] Unable to read the existing configuration file.", e);
            return false;
        }

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

        File backupFile = resolveBackupFile();
        try {
            Files.copy(configFile.toPath(), backupFile.toPath());
            this.logger.info("[UltimateTimber] Backed up legacy configuration to " + backupFile.getName());
        } catch (IOException e) {
            this.logger.log(Level.SEVERE, "[UltimateTimber] Failed to back up legacy configuration file!", e);
            return false;
        }

        YamlConfiguration legacy;
        try {
            legacy = readConfiguration(backupFile);
        } catch (IOException | InvalidConfigurationException e) {
            this.logger.log(Level.SEVERE, "[UltimateTimber] Failed to read the backed up legacy configuration file!", e);
            return false;
        }

        YamlConfiguration modern;
        try (java.io.InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("config.yml")) {
            if (resourceStream != null) {
                modern = YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(resourceStream, StandardCharsets.UTF_8));
            } else {
                modern = new YamlConfiguration();
            }
        } catch (Exception e) {
            modern = new YamlConfiguration();
        }

        // Top level settings
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
            modern.set("Required Axe.Material", legacy.getString("required-axe.material", legacy.getString("required-axe.type", "DIAMOND_AXE")));
            modern.set("Required Axe.Name", legacy.getString("required-axe.name", "<green>An Epic Axe"));
            modern.set("Required Axe.Lore", legacy.getStringList("required-axe.lore"));
            modern.set("Required Axe.Enchants", legacy.getStringList("required-axe.enchants"));
            String legacyNbt = legacy.getString("required-axe.nbt", "ultimatetimber_axe");
            String sanitized = sanitizeLegacyAxeKey(legacyNbt);
            if (sanitized.isEmpty()) {
                sanitized = "axe";
            }
            modern.set("Required Axe.PDC", java.util.List.of("ultimatetimber:" + sanitized + ":BYTE:1"));
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

        String migrated = renderModern(modern);
        try {
            YamlConfig.load(migrated);
        } catch (RuntimeException e) {
            this.logger.log(Level.SEVERE, "[UltimateTimber] Migrated configuration failed validation and was not written. "
                    + "The legacy configuration file was left untouched.", e);
            return false;
        }

        return writeAtomically(configFile, migrated);
    }

    /**
     * Renders the modern configuration in a shape the VInject YAML reader accepts.
     * Its reader is line based, so a value must never be folded onto a
     * continuation line, which is what the default 80 character dump width does
     * to long values such as loot commands.
     *
     * @param modern The modern configuration to render
     * @return The rendered YAML document
     */
    private String renderModern(YamlConfiguration modern) {
        modern.options().width(Integer.MAX_VALUE);
        return modern.saveToString();
    }

    /**
     * Writes the migrated document through a temporary file so a failed write can
     * never leave a half written configuration in place.
     *
     * @param configFile The live configuration file to replace
     * @param migrated   The validated YAML document
     * @return True if the file was replaced
     */
    private boolean writeAtomically(File configFile, String migrated) {
        Path target = configFile.toPath();
        Path temporary = new File(this.dataFolder, configFile.getName() + ".migrating").toPath();

        try {
            Files.writeString(temporary, migrated, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveFailure) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            this.logger.info("[UltimateTimber] Successfully migrated legacy configuration to modern YAML structure.");
            return true;
        } catch (IOException e) {
            this.logger.log(Level.SEVERE, "[UltimateTimber] Failed to save modern configuration file after migration!", e);
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
                // The temporary file is left behind for inspection only.
            }
            return false;
        }
    }

    /**
     * Reads a configuration file, falling back to the legacy encoding when the
     * contents are not valid UTF-8.
     *
     * @param file The configuration file to read
     * @return The loaded configuration
     * @throws IOException                   If the file cannot be read
     * @throws InvalidConfigurationException If the contents are not valid YAML
     */
    private YamlConfiguration readConfiguration(File file) throws IOException, InvalidConfigurationException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.loadFromString(decode(bytes));
        return configuration;
    }

    private String decode(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            return new String(bytes, LEGACY_CHARSET);
        }
    }

    /**
     * Resolves a backup file name that never overwrites an existing backup, so an
     * earlier migration can always be recovered.
     *
     * @return The backup file to create
     */
    private File resolveBackupFile() {
        File backupFile = new File(this.dataFolder, "config-legacy.yml");
        for (int index = 2; backupFile.exists(); index++) {
            backupFile = new File(this.dataFolder, "config-legacy-" + index + ".yml");
        }
        return backupFile;
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
                String command = lootItem.getString("command");
                if (command != null) {
                    modern.set(itemPrefix + "Command", migrateCommandPlaceholders(command));
                }
            }
            modern.set(itemPrefix + "Chance", lootItem.getDouble("chance", 0.0));
        }
    }

    private String migrateCommandPlaceholders(String command) {
        return command.replace("%player%", "<player>")
                .replace("%type%", "<type>")
                .replace("%xPos%", "<x-pos>")
                .replace("%yPos%", "<y-pos>")
                .replace("%zPos%", "<z-pos>");
    }

    private String sanitizeLegacyAxeKey(String key) {
        return key.trim().toLowerCase(Locale.ROOT).replace(' ', '_').replaceAll("[^a-z0-9._/-]", "");
    }
}
