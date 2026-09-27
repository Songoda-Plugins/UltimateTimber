package com.songoda.ultimatetimber.test;

import com.songoda.ultimatetimber.api.manager.PlacedBlockManager;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.manager.TreeDetectionManagerImpl;

import java.lang.reflect.Field;

/**
 * Wires a detection manager to test doubles, mirroring what VInject does at startup.
 */
public final class DetectionTestSupport {

    private DetectionTestSupport() {
    }

    /**
     * Creates a configuration with the detection settings under test.
     *
     * @param maxLogsPerChop        The configured log limit
     * @param leavesRequiredForTree The configured leaf minimum
     * @param onlyDetectLogsUpwards Whether only logs above the initial block are detected
     * @param destroyLeaves         Whether detected leaves are part of the tree
     * @param breakEntireTreeBase   Whether the tree base is validated
     * @return The configuration
     */
    public static TimberConfig config(int maxLogsPerChop, int leavesRequiredForTree, boolean onlyDetectLogsUpwards,
                                      boolean destroyLeaves, boolean breakEntireTreeBase) {
        TimberConfig config = new TimberConfig();
        config.setMaxLogsPerChop(maxLogsPerChop);
        config.setLeavesRequiredForTree(leavesRequiredForTree);
        config.setOnlyDetectLogsUpwards(onlyDetectLogsUpwards);
        config.setDestroyLeaves(destroyLeaves);
        config.setBreakEntireTreeBase(breakEntireTreeBase);
        return config;
    }

    /**
     * Creates a detection manager wired to the given doubles.
     *
     * @param config             The configuration to use
     * @param definitionManager  The definition manager double
     * @param placedBlockManager The placed block manager double
     * @return The detection manager
     * @throws ReflectiveOperationException When a field cannot be injected
     */
    public static TreeDetectionManagerImpl manager(TimberConfig config, TreeDefinitionManager definitionManager,
                                                   PlacedBlockManager placedBlockManager) throws ReflectiveOperationException {
        TreeDetectionManagerImpl manager = new TreeDetectionManagerImpl();
        inject(manager, "config", config);
        inject(manager, "treeDefinitionManager", definitionManager);
        inject(manager, "placedBlockManager", placedBlockManager);
        manager.onReload();
        return manager;
    }

    private static void inject(Object target, String fieldName, Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
