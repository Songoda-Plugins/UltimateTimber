package com.songoda.ultimatetimber.manager;

import com.songoda.core.vortexcore.hooks.internal.ReloadHook;
import com.songoda.core.vortexcore.text.AdventureUtils;
import com.songoda.core.vortexcore.vinject.annotation.RegisterReloadHook;
import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockType;
import com.songoda.ultimatetimber.api.tree.TreeDefinition;
import com.songoda.ultimatetimber.api.tree.TreeLoot;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.config.entry.GlobalLootConfig;
import com.songoda.ultimatetimber.config.entry.LootConfigEntry;
import com.songoda.ultimatetimber.config.entry.RequiredAxeConfig;
import com.songoda.ultimatetimber.config.entry.TreeConfigEntry;
import com.songoda.ultimatetimber.tree.TreeDefinitionImpl;
import com.songoda.ultimatetimber.utils.BlockUtils;
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.annotation.component.Component;
import net.vortexdevelopment.vinject.annotation.lifecycle.PostConstruct;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
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

    @Inject
    private TimberConfig config;

    private final Random random = new Random();
    private final Map<String, TreeDefinition> treeDefinitions = new LinkedHashMap<>();
    private final Set<Material> globalPlantableSoil = new HashSet<>();
    private final Set<TreeLoot> globalLogLoot = new HashSet<>();
    private final Set<TreeLoot> globalLeafLoot = new HashSet<>();
    private final Set<TreeLoot> globalEntireTreeLoot = new HashSet<>();
    private final Set<Material> globalRequiredTools = new HashSet<>();

    private boolean globalAxeRequired = false;
    private ItemStack requiredAxe;
    private NamespacedKey requiredAxePdcKey;

    @PostConstruct
    public void initialize() {
        onReload();
    }

    @Override
    public void onReload() {
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

    private void loadRequiredAxe(RequiredAxeConfig axeConfig) {
        if (axeConfig == null) {
            return;
        }

        String nbtKey = axeConfig.getNbt() != null && !axeConfig.getNbt().trim().isEmpty()
                ? axeConfig.getNbt().trim().toLowerCase()
                : "ultimatetimber_axe";

        UltimateTimber plugin = UltimateTimber.getInstance();
        this.requiredAxePdcKey = new NamespacedKey(plugin, nbtKey);

        Material material = axeConfig.getResolvedMaterial();
        if (material == null || material.isAir()) {
            return;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (axeConfig.getName() != null && !axeConfig.getName().isEmpty()) {
                meta.displayName(AdventureUtils.formatComponent(axeConfig.getName()));
            }

            if (axeConfig.getLore() != null && !axeConfig.getLore().isEmpty()) {
                meta.lore(axeConfig.getLore().stream().map(AdventureUtils::formatComponent).toList());
            }

            if (axeConfig.getEnchants() != null) {
                for (String enchantEntry : axeConfig.getEnchants()) {
                    String[] parts = enchantEntry.split(":");
                    String enchantName = parts[0].trim().toLowerCase();
                    int level = parts.length > 1 ? parseInt(parts[1], 1) : 1;

                    Enchantment enchant = resolveEnchantment(enchantName);
                    if (enchant != null) {
                        meta.addEnchant(enchant, Math.max(1, level), true);
                    }
                }
            }

            meta.getPersistentDataContainer().set(this.requiredAxePdcKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }

        this.requiredAxe = item;
    }

    private Enchantment resolveEnchantment(String name) {
        NamespacedKey key = NamespacedKey.minecraft(name);
        Enchantment enchantment = Registry.ENCHANTMENT.get(key);
        if (enchantment != null) {
            return enchantment;
        }

        return Enchantment.getByName(name.toUpperCase());
    }

    private int parseInt(String str, int fallback) {
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public @NotNull Set<TreeDefinition> getTreeDefinitions() {
        return Collections.unmodifiableSet(new HashSet<>(this.treeDefinitions.values()));
    }

    @Override
    public @Nullable TreeDefinition getTreeDefinition(@NotNull String key) {
        return this.treeDefinitions.get(key.toLowerCase());
    }

    @Override
    public @NotNull Set<TreeDefinition> getTreeDefinitionsForLog(@NotNull Block block) {
        return narrowTreeDefinition(new HashSet<>(this.treeDefinitions.values()), block, TreeBlockType.LOG);
    }

    @Override
    public @NotNull Set<TreeDefinition> narrowTreeDefinition(@NotNull Set<TreeDefinition> possibleTreeDefinitions,
                                                             @NotNull Block block,
                                                             @NotNull TreeBlockType treeBlockType) {
        return narrowTreeDefinition(possibleTreeDefinitions, block.getType(), treeBlockType);
    }

    @Override
    public @NotNull Set<TreeDefinition> narrowTreeDefinition(@NotNull Set<TreeDefinition> possibleTreeDefinitions,
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
    public boolean isToolValidForAnyTreeDefinition(@Nullable ItemStack tool) {
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
    public boolean isToolValidForTreeDefinition(@NotNull TreeDefinition treeDefinition, @Nullable ItemStack tool) {
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
        if (tool == null || tool.getType().isAir() || !tool.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            return false;
        }

        if (this.requiredAxePdcKey != null && meta.getPersistentDataContainer().has(this.requiredAxePdcKey, PersistentDataType.BYTE)) {
            return true;
        }

        return this.requiredAxe != null && tool.isSimilar(this.requiredAxe);
    }

    @Override
    public void dropTreeLoot(@NotNull TreeDefinition treeDefinition,
                             @NotNull TreeBlock<?> treeBlock,
                             @NotNull Player player,
                             boolean hasSilkTouch,
                             boolean isForEntireTree) {
        boolean addToInventory = this.config != null && this.config.isAddItemsToInventory();
        boolean hasBonusChance = player.hasPermission("ultimatetimber.bonusloot");
        List<ItemStack> lootedItems = new ArrayList<>();
        List<String> lootedCommands = new ArrayList<>();

        List<TreeLoot> toTry = new ArrayList<>();
        if (isForEntireTree) {
            toTry.addAll(treeDefinition.getEntireTreeLoot());
            toTry.addAll(this.globalEntireTreeLoot);
        } else {
            if (this.config != null && this.config.isApplySilkTouch() && hasSilkTouch) {
                lootedItems.addAll(BlockUtils.getBlockDrops(treeBlock));
            } else {
                if (treeBlock.getTreeBlockType() == TreeBlockType.LOG) {
                    toTry.addAll(treeDefinition.getLogLoot());
                    toTry.addAll(this.globalLogLoot);
                    if (treeDefinition.shouldDropOriginalLog()) {
                        lootedItems.addAll(BlockUtils.getBlockDrops(treeBlock));
                    }
                } else if (treeBlock.getTreeBlockType() == TreeBlockType.LEAF) {
                    toTry.addAll(treeDefinition.getLeafLoot());
                    toTry.addAll(this.globalLeafLoot);
                    if (treeDefinition.shouldDropOriginalLeaf()) {
                        lootedItems.addAll(BlockUtils.getBlockDrops(treeBlock));
                    }
                }
            }
        }

        double multiplier = (this.config != null) ? this.config.getBonusLootMultiplier() : 2.0;
        for (TreeLoot treeLoot : toTry) {
            if (treeLoot == null) {
                continue;
            }

            double chance = hasBonusChance ? treeLoot.getChance() * multiplier : treeLoot.getChance();
            if (this.random.nextDouble() > chance / 100.0) {
                continue;
            }

            if (treeLoot.hasItem()) {
                lootedItems.add(treeLoot.getItem().clone());
            }

            if (treeLoot.hasCommand()) {
                lootedCommands.add(treeLoot.getCommand());
            }
        }

        Location blockLocation = treeBlock.getLocation();
        if (addToInventory && player.getWorld().equals(blockLocation.getWorld())) {
            List<ItemStack> extraItems = new ArrayList<>();
            for (ItemStack item : lootedItems) {
                extraItems.addAll(player.getInventory().addItem(item).values());
            }
            Location playerLoc = player.getLocation().clone().subtract(0.5, 0.0, 0.5);
            for (ItemStack extra : extraItems) {
                if (playerLoc.getWorld() != null) {
                    playerLoc.getWorld().dropItemNaturally(playerLoc, extra);
                }
            }
        } else {
            Location dropLoc = blockLocation.clone().add(0.5, 0.5, 0.5);
            for (ItemStack item : lootedItems) {
                if (dropLoc.getWorld() != null) {
                    dropLoc.getWorld().dropItemNaturally(dropLoc, item);
                }
            }
        }

        for (String command : lootedCommands) {
            String processed = command.replace("%player%", player.getName())
                    .replace("%type%", treeDefinition.getKey())
                    .replace("%xPos%", String.valueOf(blockLocation.getBlockX()))
                    .replace("%yPos%", String.valueOf(blockLocation.getBlockY()))
                    .replace("%zPos%", String.valueOf(blockLocation.getBlockZ()));
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), processed);
        }
    }

    @Override
    public @NotNull Set<Material> getPlantableSoilMaterials(@NotNull TreeDefinition treeDefinition) {
        Set<Material> soils = new HashSet<>(treeDefinition.getPlantableSoilMaterials());
        soils.addAll(this.globalPlantableSoil);
        return soils;
    }

    @Override
    public @Nullable ItemStack getRequiredAxe() {
        return this.requiredAxe != null ? this.requiredAxe.clone() : null;
    }

    @Override
    public boolean isGlobalAxeRequired() {
        return this.globalAxeRequired;
    }
}
