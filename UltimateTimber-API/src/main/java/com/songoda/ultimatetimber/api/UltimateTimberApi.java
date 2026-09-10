package com.songoda.ultimatetimber.api;

import com.songoda.core.vortexcore.vinject.annotation.Api;
import com.songoda.ultimatetimber.api.manager.BlockReplacementManager;
import com.songoda.ultimatetimber.api.manager.ChoppingManager;
import com.songoda.ultimatetimber.api.manager.PlacedBlockManager;
import com.songoda.ultimatetimber.api.manager.SaplingManager;
import com.songoda.ultimatetimber.api.manager.TreeAnimationManager;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.manager.TreeDetectionManager;
import com.songoda.ultimatetimber.api.manager.TreeFallManager;
import lombok.Getter;
import net.vortexdevelopment.vinject.annotation.Inject;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * The main API entrypoint for UltimateTimber.
 * Provides static access to all plugin services and managers.
 */
@Api
public final class UltimateTimberApi {

    @Getter
    private static final NamespacedKey CUSTOM_EXE_KEY = new NamespacedKey("ultimatetimber", "custom_axe");

    @Inject @Getter private static Plugin plugin;
    @Inject @Getter private static TreeDetectionManager treeDetectionManager;
    @Inject @Getter private static TreeFallManager treeFallManager;
    @Inject @Getter private static TreeAnimationManager treeAnimationManager;
    @Inject @Getter private static ChoppingManager choppingManager;
    @Inject @Getter private static SaplingManager saplingManager;
    @Inject @Getter private static BlockReplacementManager blockReplacementManager;
    @Inject @Getter private static PlacedBlockManager placedBlockManager;
    @Inject @Getter private static TreeDefinitionManager treeDefinitionManager;

    private UltimateTimberApi() {
    }

    public static boolean isCustomAxe(ItemStack itemStack) {
        return itemStack.getPersistentDataContainer().has(CUSTOM_EXE_KEY);
    }
}
