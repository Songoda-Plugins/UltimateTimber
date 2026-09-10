package com.songoda.ultimatetimber.config.importer;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

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

        Logger logger = Logger.getLogger("UltimateTimberTest");
        LegacyConfigImporter importer = new LegacyConfigImporter(dataFolder, logger);

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
        assertTrue(modernConfig.getBoolean("Protect Tool"));
        assertEquals("DYNAMIC", modernConfig.getString("Queued Block Replacement.Mode"));
        assertEquals(25, modernConfig.getInt("Queued Block Replacement.Threshold"));
        assertEquals(500, modernConfig.getInt("Queued Block Replacement.Max Per Tick"));
        assertEquals("OAK_SAPLING", modernConfig.getString("Trees.oak.Sapling"));

        // Running importer again on already migrated config should return false
        assertFalse(importer.isLegacy(configFile));
        assertFalse(importer.importIfLegacy(configFile));
    }
}
