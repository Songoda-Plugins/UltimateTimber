package com.songoda.ultimatetimber.manager;

import com.songoda.core.SongodaPlugin;
import com.songoda.core.vortexcore.hooks.internal.ReloadHook;
import com.songoda.core.vortexcore.text.MiniMessagePlaceholder;
import com.songoda.core.vortexcore.compatibility.folia.SchedulerUtils;
import com.songoda.core.vortexcore.vinject.annotation.RegisterReloadHook;
import com.songoda.ultimatetimber.api.UltimateTimberApi;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockType;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import com.songoda.ultimatetimber.api.tree.TreeLoot;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.config.entry.GlobalLootConfig;
import com.songoda.ultimatetimber.config.entry.LootConfigEntry;
import com.songoda.ultimatetimber.config.entry.TreeConfigEntry;
import com.songoda.ultimatetimber.tree.TreeDefinitionImpl;
import com.songoda.ultimatetimber.utils.BlockUtils;
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.annotation.component.Component;
import net.vortexdevelopment.vinject.annotation.lifecycle.PostConstruct;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Service managing loaded tree definitions, drop tables, and required tools.
 */
@Component
@RegisterReloadHook
public class TreeDefinitionManagerImpl implements TreeDefinitionManager, ReloadHook {

    private final Random random = new Random();
    private final Map<String, TreeDefinition> treeDefinitions = new LinkedHashMap<>();
    private final Set<Material> globalPlantableSoil = new HashSet<>();
    private final Set<TreeLoot> globalLogLoot = new HashSet<>();
    private final Set<TreeLoot> globalLeafLoot = new HashSet<>();
    private final Set<TreeLoot> globalEntireTreeLoot = new HashSet<>();
    private final Set<Material> globalRequiredTools = new HashSet<>();
    @Inject
    private TimberConfig config;
    private boolean globalAxeRequired = false;
    private ItemStack requiredAxe;

    @PostConstruct
    public void initialize() {
        onReload();
    }

    @Override
    public synchronized void onReload() {
        this.treeDefinitions.clear();
        this.globalPlantableSoil.clear();
        this.globalLogLoot.clear();
        this.globalLeafLoot.clear();
        this.globalEntireTreeLoot.clear();
        this.globalRequiredTools.clear();
        this.requiredAxe = null;

        if (this.config == null) {
            return;
        }

        GlobalLootConfig globalLoot = this.config.getGlobalLoot();
        if (globalLoot != null) {
            this.globalPlantableSoil.addAll(globalLoot.getResolvedPlantableSoil());
            this.globalRequiredTools.addAll(globalLoot.getResolvedRequiredTools());
            this.globalAxeRequired = globalLoot.isRequiredAxe();

            populateLootSet(globalLoot.getLogLoot(), this.globalLogLoot, TreeBlockType.LOG);
            populateLootSet(globalLoot.getLeafLoot(), this.globalLeafLoot, TreeBlockType.LEAF);
            populateLootSet(globalLoot.getEntireTreeLoot(), this.globalEntireTreeLoot, TreeBlockType.LOG);
        }

        loadRequiredAxe(this.config.getRequiredAxe());

        Map<String, TreeConfigEntry> treeMap = this.config.getTrees();
        if (treeMap != null) {
            for (Map.Entry<String, TreeConfigEntry> entry : treeMap.entrySet()) {
                String key = entry.getKey();
                TreeConfigEntry treeConfig = entry.getValue();

                Set<TreeLoot> logLoot = new HashSet<>();
                Set<TreeLoot> leafLoot = new HashSet<>();
                Set<TreeLoot> entireTreeLoot = new HashSet<>();

                populateLootSet(treeConfig.getLogLoot(), logLoot, TreeBlockType.LOG);
                populateLootSet(treeConfig.getLeafLoot(), leafLoot, TreeBlockType.LEAF);
                populateLootSet(treeConfig.getEntireTreeLoot(), entireTreeLoot, TreeBlockType.LOG);

                TreeDefinition definition = new TreeDefinitionImpl(
                        key,
                        treeConfig.getResolvedLogs(),
                        treeConfig.getResolvedLeaves(),
                        treeConfig.getResolvedSapling(),
                        treeConfig.getResolvedPlantableSoil(),
                        treeConfig.getMaxLogDistanceFromTrunk(),
                        treeConfig.getMaxLeafDistanceFromLog(),
                        treeConfig.isSearchForLeavesDiagonally(),
                        treeConfig.isDropOriginalLog(),
                        treeConfig.isDropOriginalLeaf(),
                        logLoot,
                        leafLoot,
                        entireTreeLoot,
                        treeConfig.getResolvedRequiredTools().stream().map(ItemStack::new).collect(java.util.stream.Collectors.toSet()),
                        treeConfig.isRequiredAxe()
                );

                this.treeDefinitions.put(key.toLowerCase(), definition);
            }
        }
    }

    private void populateLootSet(Map<String, LootConfigEntry> map, Set<TreeLoot> targetSet, TreeBlockType type) {
        if (map == null) {
            return;
        }

        for (LootConfigEntry lootEntry : map.values()) {
            if (lootEntry == null) {
                continue;
            }

            ItemStack dropItem = lootEntry.getResolvedMaterial() != null
                    ? new ItemStack(lootEntry.getResolvedMaterial())
                    : null;

            targetSet.add(new TreeLoot(type, dropItem, lootEntry.getCommand(), lootEntry.getChance()));
        }
    }

    private void loadRequiredAxe(@Nullable ItemStack configuredAxe) {
        this.requiredAxe = configuredAxe == null ? null : configuredAxe.clone();
    }

    @Override
    public synchronized @NotNull Set<TreeDefinition> getTreeDefinitions() {
        return Set.copyOf(this.treeDefinitions.values());
    }

    @Override
    public synchronized @Nullable TreeDefinition getTreeDefinition(@NotNull String key) {
        return this.treeDefinitions.get(key.toLowerCase());
    }

    @Override
    public synchronized @NotNull Set<TreeDefinition> getTreeDefinitionsForLog(@NotNull Block block) {
        return narrowTreeDefinition(new HashSet<>(this.treeDefinitions.values()), block, TreeBlockType.LOG);
    }

    @Override
    public synchronized @NotNull Set<TreeDefinition> narrowTreeDefinition(@NotNull Set<TreeDefinition> possibleTreeDefinitions,
                                                             @NotNull Block block,
                                                             @NotNull TreeBlockType treeBlockType) {
        return narrowTreeDefinition(possibleTreeDefinitions, block.getType(), treeBlockType);
    }

    @Override
    public synchronized @NotNull Set<TreeDefinition> narrowTreeDefinition(@NotNull Set<TreeDefinition> possibleTreeDefinitions,
                                                             @Nullable Material material,
                                                             @NotNull TreeBlockType treeBlockType) {
        Set<TreeDefinition> matching = new HashSet<>();
        if (material == null) {
            return matching;
        }

        for (TreeDefinition definition : possibleTreeDefinitions) {
            if (treeBlockType == TreeBlockType.LOG && definition.getLogMaterials().contains(material)) {
                matching.add(definition);
            } else if (treeBlockType == TreeBlockType.LEAF && definition.getLeafMaterials().contains(material)) {
                matching.add(definition);
            }
        }
        return matching;
    }

    @Override
    public synchronized boolean isToolValidForAnyTreeDefinition(@Nullable ItemStack tool) {
        if (this.config != null && this.config.isIgnoreRequiredTools()) {
            return true;
        }

        if (isGlobalAxeRequired() || this.treeDefinitions.values().stream().anyMatch(TreeDefinition::isRequiredAxe)) {
            if (isValidAxe(tool)) {
                return true;
            }
        }

        if (tool == null || tool.getType().isAir()) {
            return false;
        }

        for (TreeDefinition definition : this.treeDefinitions.values()) {
            if (definition.getRequiredTools().contains(tool.getType())) {
                return true;
            }
        }

        return this.globalRequiredTools.contains(tool.getType());
    }

    @Override
    public synchronized boolean isToolValidForTreeDefinition(@NotNull TreeDefinition treeDefinition, @Nullable ItemStack tool) {
        if (this.config != null && this.config.isIgnoreRequiredTools()) {
            return true;
        }

        if (treeDefinition.isRequiredAxe() || isGlobalAxeRequired()) {
            return isValidAxe(tool);
        }

        if (treeDefinition.getRequiredTools().isEmpty() && this.globalRequiredTools.isEmpty()) {
            return true;
        }

        if (tool == null || tool.getType().isAir()) {
            return false;
        }

        return treeDefinition.getRequiredTools().contains(tool.getType())
                || this.globalRequiredTools.contains(tool.getType());
    }

    private boolean isValidAxe(@Nullable ItemStack tool) {
        return tool != null
                && !tool.getType().isAir()
                && this.requiredAxe != null
                && tool.isSimilar(this.requiredAxe);
    }

    @Override
    public synchronized void dropTreeLoot(@NotNull TreeDefinition treeDefinition,
                             @NotNull TreeBlock<?> treeBlock,
                             @NotNull Player player,
                             boolean hasSilkTouch,
                             boolean isForEntireTree) {
        Location blockLocation = treeBlock.getLocation().clone();
        if (!SchedulerUtils.isOwnedByCurrentRegion(blockLocation)) {
            SchedulerUtils.runLocationTask(SongodaPlugin.getInstance(), blockLocation,
                    () -> this.dropTreeLoot(treeDefinition, treeBlock, player, hasSilkTouch, isForEntireTree));
            return;
        }

        LootSelection lootSelection = selectLoot(treeDefinition, treeBlock, hasSilkTouch, isForEntireTree);
        List<ItemStack> originalDrops = new ArrayList<>();
        if (lootSelection.dropOriginalBlock()) {
            originalDrops.addAll(BlockUtils.getBlockDrops(treeBlock));
        }

        double multiplier = (this.config != null) ? this.config.getBonusLootMultiplier() : 2.0;
        SchedulerUtils.runEntityTask(SongodaPlugin.getInstance(), player, () -> this.completeLootDrop(
                treeDefinition,
                player,
                blockLocation,
                originalDrops,
                lootSelection.configuredLoot(),
                multiplier,
                this.config != null && this.config.isAddItemsToInventory()
        ));
    }

    private void completeLootDrop(@NotNull TreeDefinition treeDefinition,
                                  @NotNull Player player,
                                  @NotNull Location blockLocation,
                                  @NotNull List<ItemStack> originalDrops,
                                  @NotNull List<TreeLoot> configuredLoot,
                                  double multiplier,
                                  boolean addToInventory) {
        boolean hasBonusChance = player.hasPermission("ultimatetimber.bonusloot");
        List<ItemStack> lootedItems = new ArrayList<>(originalDrops);
        List<String> lootedCommands = new ArrayList<>();

        for (TreeLoot treeLoot : configuredLoot) {
            if (treeLoot == null) {
                continue;
            }

            double chance = hasBonusChance ? treeLoot.chance() * multiplier : treeLoot.chance();
            if (this.random.nextDouble() > chance / 100.0) {
                continue;
            }

            if (treeLoot.hasItem()) {
                lootedItems.add(treeLoot.item().clone());
            }

            if (treeLoot.hasCommand()) {
                lootedCommands.add(treeLoot.command());
            }
        }

        if (addToInventory && player.getWorld().equals(blockLocation.getWorld())) {
            List<ItemStack> extraItems = new ArrayList<>();
            for (ItemStack item : lootedItems) {
                extraItems.addAll(player.getInventory().addItem(item).values());
            }

            Location playerLocation = player.getLocation().clone().subtract(0.5, 0.0, 0.5);
            for (ItemStack extraItem : extraItems) {
                if (playerLocation.getWorld() != null) {
                    playerLocation.getWorld().dropItemNaturally(playerLocation, extraItem);
                }
            }
        } else {
            this.dropItemsAtBlock(blockLocation, lootedItems);
        }

        String playerName = player.getName();
        for (String command : lootedCommands) {
            String processed = resolveLootCommand(command, playerName, treeDefinition, blockLocation);
            SchedulerUtils.runTask(SongodaPlugin.getInstance(),
                    () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), processed));
        }
    }

    private void dropItemsAtBlock(@NotNull Location blockLocation, @NotNull List<ItemStack> items) {
        SchedulerUtils.runLocationTask(SongodaPlugin.getInstance(), blockLocation, () -> {
            Location dropLocation = blockLocation.clone().add(0.5, 0.5, 0.5);
            if (dropLocation.getWorld() == null) {
                return;
            }

            for (ItemStack item : items) {
                dropLocation.getWorld().dropItemNaturally(dropLocation, item);
            }
        });
    }

    private LootSelection selectLoot(@NotNull TreeDefinition treeDefinition,
                                     @NotNull TreeBlock<?> treeBlock,
                                     boolean hasSilkTouch,
                                     boolean isForEntireTree) {
        if (isForEntireTree) {
            return new LootSelection(
                    combineLoot(treeDefinition.getEntireTreeLoot(), this.globalEntireTreeLoot),
                    false
            );
        }

        if (this.config != null && this.config.isApplySilkTouch() && hasSilkTouch) {
            return new LootSelection(List.of(), true);
        }

        return switch (treeBlock.treeBlockType()) {
            case LOG -> new LootSelection(
                    combineLoot(treeDefinition.getLogLoot(), this.globalLogLoot),
                    treeDefinition.shouldDropOriginalLog()
            );
            case LEAF -> new LootSelection(
                    combineLoot(treeDefinition.getLeafLoot(), this.globalLeafLoot),
                    treeDefinition.shouldDropOriginalLeaf()
            );
        };
    }

    private List<TreeLoot> combineLoot(Collection<TreeLoot> treeLoot, Collection<TreeLoot> globalLoot) {
        List<TreeLoot> combinedLoot = new ArrayList<>(treeLoot);
        combinedLoot.addAll(globalLoot);
        return combinedLoot;
    }

    private @NotNull String resolveLootCommand(@NotNull String command,
                                               @NotNull String playerName,
                                               @NotNull TreeDefinition treeDefinition,
                                               @NotNull Location blockLocation) {
        List<MiniMessagePlaceholder> placeholders = List.of(
                new MiniMessagePlaceholder("player", playerName),
                new MiniMessagePlaceholder("type", treeDefinition.getKey()),
                new MiniMessagePlaceholder("x-pos", blockLocation.getBlockX()),
                new MiniMessagePlaceholder("y-pos", blockLocation.getBlockY()),
                new MiniMessagePlaceholder("z-pos", blockLocation.getBlockZ())
        );

        String resolvedCommand = command;
        for (MiniMessagePlaceholder placeholder : placeholders) {
            resolvedCommand = placeholder.replace(resolvedCommand);
        }
        return resolvedCommand;
    }

    @Override
    public synchronized @NotNull Set<Material> getPlantableSoilMaterials(@NotNull TreeDefinition treeDefinition) {
        Set<Material> soils = new HashSet<>(treeDefinition.getPlantableSoilMaterials());
        soils.addAll(this.globalPlantableSoil);
        return soils;
    }

    @Override
    public synchronized @Nullable ItemStack getRequiredAxe() {
        return this.requiredAxe != null ? this.requiredAxe.clone() : null;
    }

    @Override
    public synchronized boolean isGlobalAxeRequired() {
        return this.globalAxeRequired;
    }

    private record LootSelection(List<TreeLoot> configuredLoot, boolean dropOriginalBlock) {
    }
}
