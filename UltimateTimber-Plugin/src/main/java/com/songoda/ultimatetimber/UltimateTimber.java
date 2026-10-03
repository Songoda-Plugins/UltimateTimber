package com.songoda.ultimatetimber;

import com.songoda.core.SongodaPlugin;
import com.songoda.ultimatetimber.config.importer.LegacyConfigImporter;
import com.songoda.ultimatetimber.integration.protection.UltimateClaimsProtectionHook;
import net.vortexdevelopment.vinject.annotation.component.Root;
import net.vortexdevelopment.vinject.annotation.template.TemplateDependency;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * UltimateTimber plugin bootstrap.
 */
@Root(
        packageName = "com.songoda.ultimatetimber",
        createInstance = false,
        templateDependencies = {
                @TemplateDependency(
                        groupId = "com.songoda",
                        artifactId = "SongodaCore",
                        version = "5.0.0-SNAPSHOT"
                )
        }
)
public final class UltimateTimber extends SongodaPlugin {

    @Override
    protected void verifyLicense() {
    }

    @Override
    public void onPreComponentLoad() {
    }

    @Override
    public void onPluginLoad() {
        initDatabase();
        getHookRegistry().registerProtectionHook("UltimateClaims", () -> new UltimateClaimsProtectionHook());

        File configFile = new File(getDataFolder(), "config.yml");
        LegacyConfigImporter.sanitizeEncoding(configFile);
        LegacyConfigImporter importer = new LegacyConfigImporter(getDataFolder());
        importer.importIfLegacy(configFile);
    }

    @Override
    protected void onPluginEnable() {
    }

    @Override
    protected void onPluginDisable() {
    }

    @Override
    protected @Nullable Integer getBstatsPluginId() {
        return 4184;
    }
}
