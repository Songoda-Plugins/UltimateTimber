package com.songoda.ultimatetimber.config.importer;

import net.vortexdevelopment.vinject.config.yaml.YamlConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyConfigImporterTest {

    @Test
    void testLegacyConfigDetectionAndMigration(@TempDir Path tempDir) throws IOException {
        File dataFolder = tempDir.toFile();
        File configFile = new File(dataFolder, "config.yml");

        String legacyYaml = ""
                + "server-type: CURRENT\n"
                + "locale: en_US\n"
                + "disabled-worlds:\n"
                + "  - disabled_world_name\n"
                + "max-logs-per-chop: 120\n"
                + "leaves-required-for-tree: 4\n"
                + "destroy-leaves: true\n"
                + "realistic-tool-damage: true\n"
                + "protect-tool: true\n"
                + "queued-block-replacement: DYNAMIC\n"
                + "queued-block-replacement-threshold: 25\n"
                + "queued-block-replacement-max-per-tick: 500\n"
                + "global-plantable-soil:\n"
                + "  - DIRT\n"
                + "  - GRASS_BLOCK\n"
                + "trees:\n"
                + "  oak:\n"
                + "    logs:\n"
                + "      - OAK_LOG\n"
                + "    leaves:\n"
                + "      - OAK_LEAVES\n"
                + "    sapling: OAK_SAPLING\n"
                + "    max-log-distance-from-trunk: 5\n"
                + "    max-leaf-distance-from-log: 5\n"
                + "    search-for-leaves-diagonally: false\n"
                + "    drop-original-log: true\n"
                + "    drop-original-leaf: false\n";

        Files.writeString(configFile.toPath(), legacyYaml);

        LegacyConfigImporter importer = new LegacyConfigImporter(dataFolder);

        assertTrue(importer.isLegacy(configFile));

        boolean migrated = importer.importIfLegacy(configFile);
        assertTrue(migrated);

        File backupFile = new File(dataFolder, "config-legacy.yml");
        assertTrue(backupFile.exists());

        YamlConfiguration modernConfig = YamlConfiguration.loadConfiguration(configFile);
        assertFalse(modernConfig.contains("server-type"));
        assertFalse(modernConfig.contains("max-logs-per-chop"));

        assertEquals(120, modernConfig.getInt("Max Logs Per Chop"));
        assertEquals(4, modernConfig.getInt("Leaves Required For Tree"));
        assertTrue(modernConfig.getBoolean("Realistic Drops"));
        assertTrue(modernConfig.getBoolean("Protect Tool"));
        assertEquals("DYNAMIC", modernConfig.getString("Queued Block Replacement.Mode"));
        assertEquals(25, modernConfig.getInt("Queued Block Replacement.Threshold"));
        assertEquals(500, modernConfig.getInt("Queued Block Replacement.Max Per Tick"));
        assertEquals("OAK_SAPLING", modernConfig.getString("Trees.oak.Sapling"));

        String migratedText = Files.readString(configFile.toPath(), StandardCharsets.UTF_8);
        assertTrue(migratedText.contains("\n\nQueued Block Replacement:\n"));
        assertTrue(migratedText.contains("\n\nTrees:\n"));

        // Running importer again on already migrated config should return false
        assertFalse(importer.isLegacy(configFile));
        assertFalse(importer.importIfLegacy(configFile));
    }

    @Test
    void testLegacyConfigWithLongLootCommandAndLegacyEncoding(@TempDir Path tempDir) throws IOException {
        File dataFolder = tempDir.toFile();
        File configFile = new File(dataFolder, "config.yml");

        String broadcastCommand = "broadcast %player% found a golden apple in a %type% tree at %xPos% %yPos% %zPos%!";
        String migratedBroadcastCommand = "broadcast <player> found a golden apple in a <type> tree at <x-pos> <y-pos> <z-pos>!";
        String axeLore = "&7Sie läßt Bäume im Ganzen fallen.";

        String legacyYaml = ""
                + "server-type: CURRENT\n"
                + "locale: de_DE\n"
                + "max-logs-per-chop: 150\n"
                + "global-log-loot:\n"
                + "  0:\n"
                + "    material: DIAMOND\n"
                + "    chance: 0\n"
                + "  2:\n"
                + "    material: GOLDEN_APPLE\n"
                + "    command: '" + broadcastCommand + "'\n"
                + "    chance: 0\n"
                + "required-axe:\n"
                + "  type: DIAMOND_AXE\n"
                + "  name: '&4Die ultimative Timber-Axt'\n"
                + "  lore:\n"
                + "  - '&7Diese Axt ist &4magisch.'\n"
                + "  - '" + axeLore + "'\n"
                + "  enchants:\n"
                + "  - DURABILITY:1\n"
                + "  nbt: ultimatetimber_axe\n";

        // The reported legacy file was saved as Windows-1252, not UTF-8.
        Files.write(configFile.toPath(), legacyYaml.getBytes(Charset.forName("windows-1252")));

        File existingBackup = new File(dataFolder, "config-legacy.yml");
        Files.writeString(existingBackup.toPath(), "server-type: CURRENT\n");

        LegacyConfigImporter importer = new LegacyConfigImporter(dataFolder);

        assertTrue(importer.isLegacy(configFile));
        assertTrue(importer.importIfLegacy(configFile));

        // An earlier backup is never overwritten.
        assertEquals("server-type: CURRENT\n", Files.readString(existingBackup.toPath()));
        assertTrue(new File(dataFolder, "config-legacy-2.yml").exists());

        String migratedText = Files.readString(configFile.toPath(), StandardCharsets.UTF_8);
        assertTrue(migratedText.contains("\n\nGlobal Loot:\n"));
        assertTrue(migratedText.contains("\n\nRequired Axe:\n"));

        // The migrated file is read by VInject's line based YAML reader at startup,
        // so it must never contain a folded value on a continuation line.
        YamlConfig migrated = YamlConfig.load(migratedText);

        assertEquals(migratedBroadcastCommand, migrated.get("Global Loot.Log Loot.2.Command"));
        assertEquals(1L, migratedText.lines().filter(line -> line.contains("broadcast <player>")).count());
        assertTrue(migratedText.lines().anyMatch(line -> line.contains("broadcast <player>") && line.contains("<z-pos>!")));

        // Non-ASCII text from the legacy encoding survives the migration.
        assertTrue(migratedText.contains(axeLore));
        assertTrue(migrated.get("Required Axe.Lore") instanceof List<?>);
        assertTrue(((List<?>) migrated.get("Required Axe.Lore")).contains(axeLore));
    }

    @Test
    void testSanitizeEncodingWithBomAndWindows1252(@TempDir Path tempDir) throws IOException {
        File dataFolder = tempDir.toFile();
        File configFile = new File(dataFolder, "config.yml");

        // Create file with UTF-8 BOM
        byte[] content = "test: value\n".getBytes(StandardCharsets.UTF_8);
        byte[] withBom = new byte[content.length + 3];
        withBom[0] = (byte) 0xEF;
        withBom[1] = (byte) 0xBB;
        withBom[2] = (byte) 0xBF;
        System.arraycopy(content, 0, withBom, 3, content.length);
        Files.write(configFile.toPath(), withBom);

        LegacyConfigImporter.sanitizeEncoding(configFile);
        byte[] sanitized = Files.readAllBytes(configFile.toPath());
        assertEquals("test: value\n", new String(sanitized, StandardCharsets.UTF_8));
        assertFalse(sanitized[0] == (byte) 0xEF && sanitized[1] == (byte) 0xBB && sanitized[2] == (byte) 0xBF);

        // Test non-UTF8 Windows-1252 character (e.g. 0xDF for ß)
        byte[] windows1252 = new byte[]{(byte) 'a', (byte) ':', (byte) ' ', (byte) 0xDF, (byte) '\n'};
        Files.write(configFile.toPath(), windows1252);
        LegacyConfigImporter.sanitizeEncoding(configFile);
        String result = Files.readString(configFile.toPath(), StandardCharsets.UTF_8);
        assertTrue(result.contains("ß"));
    }
}
