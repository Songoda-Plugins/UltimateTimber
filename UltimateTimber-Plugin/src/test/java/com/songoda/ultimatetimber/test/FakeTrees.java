package com.songoda.ultimatetimber.test;

import com.songoda.ultimatetimber.api.manager.PlacedBlockManager;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.tree.TreeBlockType;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import com.songoda.ultimatetimber.utils.LongSet;
import com.songoda.ultimatetimber.utils.TreeGeometry;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Definition, manager and placed block doubles used by the detection tests.
 */
public final class FakeTrees {

    private FakeTrees() {
    }

    /**
     * Creates a tree definition double.
     *
     * @param key                     The definition key
     * @param logs                    The log materials
     * @param leaves                  The leaf materials
     * @param maxLogDistanceFromTrunk The configured trunk distance
     * @param maxLeafDistanceFromLog  The configured leaf distance
     * @param detectLeavesDiagonally  Whether diagonal leaf search is enabled
     * @return The definition
     */
    public static TreeDefinition definition(String key, Set<Material> logs, Set<Material> leaves,
                                            double maxLogDistanceFromTrunk, int maxLeafDistanceFromLog,
                                            boolean detectLeavesDiagonally) {
        Map<String, Object> values = new HashMap<>();
        values.put("getKey", key);
        values.put("getLogMaterials", logs);
        values.put("getLeafMaterials", leaves);
        values.put("getSaplingMaterial", null);
        values.put("getPlantableSoilMaterials", Set.of());
        values.put("shouldDetectLeavesDiagonally", detectLeavesDiagonally);
        values.put("shouldDropOriginalLog", true);
        values.put("shouldDropOriginalLeaf", false);
        values.put("getLogLoot", Set.of());
        values.put("getLeafLoot", Set.of());
        values.put("getEntireTreeLoot", Set.of());
        values.put("getRequiredTools", Set.of());
        values.put("isRequiredAxe", false);
        values.put("getMaxLogDistanceFromTrunk", maxLogDistanceFromTrunk);
        values.put("getMaxLeafDistanceFromLog", maxLeafDistanceFromLog);

        return (TreeDefinition) Proxy.newProxyInstance(TreeDefinition.class.getClassLoader(), new Class<?>[]{TreeDefinition.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.equals("hashCode")) {
                        return System.identityHashCode(proxy);
                    }
                    if (name.equals("equals")) {
                        return proxy == args[0];
                    }
                    if (name.equals("toString")) {
                        return "FakeTreeDefinition[" + values.get("getKey") + "]";
                    }
                    return values.get(name);
                });
    }

    /**
     * Creates a tree definition manager double that narrows definitions the same way the real manager
     * does, without touching configuration.
     *
     * @param definitions   The definitions to serve
     * @param plantableSoil The plantable soil materials
     * @return The manager
     */
    public static TreeDefinitionManager definitionManager(List<TreeDefinition> definitions, Set<Material> plantableSoil) {
        return (TreeDefinitionManager) Proxy.newProxyInstance(TreeDefinitionManager.class.getClassLoader(), new Class<?>[]{TreeDefinitionManager.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getTreeDefinitionsForLog" ->
                            narrow(definitions, ((Block) args[0]).getType(), TreeBlockType.LOG);
                    case "narrowTreeDefinition" -> narrow(definitions, materialOf(args[1]), (TreeBlockType) args[2]);
                    case "getTreeDefinitions" -> new HashSet<>(definitions);
                    case "getTreeDefinition" -> definitions.isEmpty() ? null : definitions.get(0);
                    case "getPlantableSoilMaterials" -> new HashSet<>(plantableSoil);
                    case "isToolValidForAnyTreeDefinition", "isToolValidForTreeDefinition" -> true;
                    case "getRequiredAxe" -> null;
                    case "isGlobalAxeRequired" -> false;
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }

    private static Material materialOf(Object target) {
        if (target instanceof Block block) {
            return block.getType();
        }
        return (Material) target;
    }

    private static Set<TreeDefinition> narrow(List<TreeDefinition> definitions, Material material, TreeBlockType treeBlockType) {
        Set<TreeDefinition> matching = new HashSet<>();
        for (TreeDefinition definition : definitions) {
            Set<Material> materials = treeBlockType == TreeBlockType.LOG ? definition.getLogMaterials() : definition.getLeafMaterials();
            if (materials.contains(material)) {
                matching.add(definition);
            }
        }
        return matching;
    }

    /**
     * Placed block repository double backed by the same packed keys the implementation uses.
     */
    public static final class FakePlacedBlockManager implements PlacedBlockManager {

        private final LongSet placedBlocks = new LongSet();
        private int probes;

        @Override
        public boolean isBlockPlaced(@NonNull Block block) {
            this.probes++;
            return this.placedBlocks.contains(TreeGeometry.packBlockKey(block));
        }

        @Override
        public void protectBlock(@NonNull Block block, boolean isPlaced) {
            if (isPlaced) {
                this.placedBlocks.add(TreeGeometry.packBlockKey(block));
            } else {
                this.placedBlocks.remove(TreeGeometry.packBlockKey(block));
            }
        }

        /**
         * Marks a block as player placed.
         *
         * @param block The block to mark
         */
        public void markPlaced(Block block) {
            protectBlock(block, true);
        }

        /**
         * Resets the probe counter.
         */
        public void resetProbes() {
            this.probes = 0;
        }

        /**
         * Gets the number of repository probes.
         *
         * @return The probe count
         */
        public int probes() {
            return this.probes;
        }
    }
}
