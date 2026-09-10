package com.songoda.ultimatetimber.config;

import com.songoda.ultimatetimber.config.entry.GlobalLootConfig;
import com.songoda.ultimatetimber.config.entry.HooksConfig;
import com.songoda.ultimatetimber.config.entry.QueuedBlockReplacementConfig;
import com.songoda.ultimatetimber.config.entry.RequiredAxeConfig;
import com.songoda.ultimatetimber.config.entry.TreeConfigEntry;
import lombok.Getter;
import lombok.Setter;
import com.songoda.core.vortexcore.vinject.annotation.RegisterReloadHook;
import net.vortexdevelopment.vinject.annotation.lifecycle.OnLoad;
import net.vortexdevelopment.vinject.annotation.yaml.Comment;
import net.vortexdevelopment.vinject.annotation.yaml.Key;
import net.vortexdevelopment.vinject.annotation.yaml.YamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Main configuration container mapped to config.yml via VInject YamlConfiguration.
 */
@Getter
@Setter
@YamlConfiguration(file = "config.yml")
public class TimberConfig {

    @Comment("The locale to use for messages.")
    @Key("Locale")
    private String locale = "en_US";

    @Comment("A list of worlds where UltimateTimber is disabled.")
    @Key("Disabled Worlds")
    private List<String> disabledWorlds = new ArrayList<>(List.of("disabled_world_name"));

    @Comment("The max number of logs that can be broken at one time.")
    @Key("Max Logs Per Chop")
    private int maxLogsPerChop = 150;

    @Comment("The minimum number of leaves required for something to be considered a tree.")
    @Key("Leaves Required For Tree")
    private int leavesRequiredForTree = 5;

    @Comment("If leaves should be destroyed when toppling.")
    @Key("Destroy Leaves")
    private boolean destroyLeaves = true;

    @Comment("Apply realistic damage to tools based on the number of logs chopped.")
    @Key("Realistic Tool Damage")
    private boolean realisticToolDamage = true;

    @Comment("Protect the tool used to chop down the tree from breaking.")
    @Key("Protect Tool")
    private boolean protectTool = false;

    @Comment("Use silk touch enchantment if present on the tool.")
    @Key("Apply Silk Touch")
    private boolean applySilkTouch = true;

    @Comment("Damage the tool extra for each leaf block broken.")
    @Key("Apply Silk Touch Tool Damage")
    private boolean applySilkTouchToolDamage = true;

    @Comment("Require the entire base of the tree to be broken before it topples.")
    @Key("Break Entire Tree Base")
    private boolean breakEntireTreeBase = false;

    @Comment("Don't drop a block for the block that initiates the tree fall.")
    @Key("Destroy Initiated Block")
    private boolean destroyInitiatedBlock = false;

    @Comment("Only detect logs above the initiated block.")
    @Key("Only Detect Logs Upwards")
    private boolean onlyDetectLogsUpwards = true;

    @Comment("Only topple trees while the player is doing something. Options: ALWAYS, SNEAKING, NOT_SNEAKING")
    @Key("Only Topple While")
    private String onlyToppleWhile = "ALWAYS";

    @Comment("Allow toppling trees in creative mode.")
    @Key("Allow Creative Mode")
    private boolean allowCreativeMode = true;

    @Comment("Require permission 'ultimatetimber.chop' to topple trees.")
    @Key("Require Chop Permission")
    private boolean requireChopPermission = false;

    @Comment("If player should have cooldown between chopping trees.")
    @Key("Player Tree Topple Cooldown")
    private boolean playerTreeToppleCooldown = false;

    @Comment("Seconds player must wait before chopping again.")
    @Key("Player Tree Topple Cooldown Length")
    private int playerTreeToppleCooldownLength = 5;

    @Comment("Allow players to topple trees regardless of held item.")
    @Key("Ignore Required Tools")
    private boolean ignoreRequiredTools = false;

    @Comment("Automatically replant saplings when a tree is toppled.")
    @Key("Replant Saplings")
    private boolean replantSaplings = true;

    @Comment("Always replant sapling at the base, even if tree topple condition fails.")
    @Key("Always Replant Sapling")
    private boolean alwaysReplantSapling = false;

    @Comment("Cooldown in seconds to protect newly replanted saplings from breaking. 0 to disable.")
    @Key("Replant Saplings Cooldown")
    private int replantSaplingsCooldown = 3;

    @Comment("Chance for fallen leaf blocks to replant saplings on impact.")
    @Key("Falling Blocks Replant Saplings")
    private boolean fallingBlocksReplantSaplings = true;

    @Comment("Percent chance (0-100) for fallen leaves to plant a sapling.")
    @Key("Falling Blocks Replant Saplings Chance")
    private double fallingBlocksReplantSaplingsChance = 1.0;

    @Comment("Make falling tree blocks deal damage to players on impact.")
    @Key("Falling Blocks Deal Damage")
    private boolean fallingBlocksDealDamage = true;

    @Comment("Amount of damage falling tree blocks inflict.")
    @Key("Falling Block Damage")
    private int fallingBlockDamage = 1;

    @Comment("Automatically add chopped tree drops to player inventory.")
    @Key("Add Items To Inventory")
    private boolean addItemsToInventory = false;

    @Comment("Use custom sound effects when toppling trees.")
    @Key("Use Custom Sounds")
    private boolean useCustomSounds = true;

    @Comment("Use custom particles when toppling trees.")
    @Key("Use Custom Particles")
    private boolean useCustomParticles = true;

    @Comment("Bonus loot multiplier for players with 'ultimatetimber.bonusloot'.")
    @Key("Bonus Loot Multiplier")
    private double bonusLootMultiplier = 2.0;

    @Comment("Ignore player-placed blocks from being toppled.")
    @Key("Ignore Placed Blocks")
    private boolean ignorePlacedBlocks = true;

    @Comment("Max number of placed block locations to track in memory.")
    @Key("Ignore Placed Blocks Memory Size")
    private int ignorePlacedBlocksMemorySize = 5000;

    @Comment("Tree animation type: FANCY, DISINTEGRATE, CRUMBLE, NONE")
    @Key("Tree Animation Type")
    private String treeAnimationType = "FANCY";

    @Comment("Stick fallen blocks to the ground for FANCY or CRUMBLE.")
    @Key("Scatter Tree Blocks On Ground")
    private boolean scatterTreeBlocksOnGround = false;

    @Comment("Blocks that shatter and drop when falling tree blocks hit them.")
    @Key("Fragile Blocks")
    private List<String> fragileBlocks = new ArrayList<>(List.of(
            "GLASS",
            "ICE",
            "PACKED_ICE",
            "BLUE_ICE"
    ));

    @Comment("Queued block replacement settings to reduce server lag on large trees.")
    @Key("Queued Block Replacement")
    private QueuedBlockReplacementConfig queuedBlockReplacement = new QueuedBlockReplacementConfig();

    @Comment("Third-party plugin integration hooks (mcMMO, Jobs).")
    @Key("Hooks")
    private HooksConfig hooks = new HooksConfig();

    @Comment("Global drop and tool rules applicable to all trees.")
    @Key("Global Loot")
    private GlobalLootConfig globalLoot = new GlobalLootConfig();

    @Comment("Custom required axe item.")
    @Key("Required Axe")
    private RequiredAxeConfig requiredAxe = new RequiredAxeConfig();

    @Comment("Individual tree definitions.")
    @Key("Trees")
    private Map<String, TreeConfigEntry> trees = new LinkedHashMap<>();

    private transient Set<Material> resolvedFragileBlocks = new HashSet<>();

    @OnLoad
    public void onLoad() {
        this.resolvedFragileBlocks = new HashSet<>();
        if (this.fragileBlocks != null) {
            for (String blockName : this.fragileBlocks) {
                if (blockName != null && !blockName.trim().isEmpty()) {
                    Material mat = Material.matchMaterial(blockName.trim());
                    if (mat != null) {
                        this.resolvedFragileBlocks.add(mat);
                    } else {
                        Bukkit.getLogger().warning("[UltimateTimber] Warning: Invalid material '" + blockName + "' in fragile blocks.");
                    }
                }
            }
        }
    }
}
