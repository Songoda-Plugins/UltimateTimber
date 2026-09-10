package com.songoda.ultimatetimber;

import com.songoda.core.SongodaPlugin;
import com.songoda.ultimatetimber.api.manager.BlockReplacementManager;
import com.songoda.ultimatetimber.api.manager.ChoppingManager;
import com.songoda.ultimatetimber.api.manager.PlacedBlockManager;
import com.songoda.ultimatetimber.api.manager.SaplingManager;
import com.songoda.ultimatetimber.api.manager.TreeAnimationManager;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.manager.TreeDetectionManager;
import com.songoda.ultimatetimber.api.manager.TreeFallManager;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.config.importer.LegacyConfigImporter;
import lombok.Getter;
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.annotation.component.Root;
import net.vortexdevelopment.vinject.annotation.template.TemplateDependency;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * UltimateTimber plugin bootstrap.
 */
@Getter
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

    @Inject
    private TimberConfig timberConfig;

    @Inject
    private TreeDefinitionManager treeDefinitionManager;

    @Inject
    private TreeDetectionManager treeDetectionManager;

    @Inject
    private TreeFallManager treeFallManager;

    @Inject
    private TreeAnimationManager treeAnimationManager;

    @Inject
    private ChoppingManager choppingManager;

    @Inject
    private SaplingManager saplingManager;

    @Inject
    private BlockReplacementManager blockReplacementManager;

    @Inject
    private PlacedBlockManager placedBlockManager;

    public static UltimateTimber getInstance() {
        return (UltimateTimber) SongodaPlugin.getInstance();
    }

    @Override
    protected void verifyLicense() {
    }

    @Override
    public void onPreComponentLoad() {
    }

    @Override
    public void onPluginLoad() {
        File configFile = new File(getDataFolder(), "config.yml");
        LegacyConfigImporter importer = new LegacyConfigImporter(getDataFolder(), getLogger());
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
