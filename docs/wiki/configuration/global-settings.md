---
title: "Global settings"
description: "UltimateTimber configuration defaults for tree detection, animations, tools, drops, replanting, player controls, and performance."
order: 1
hidden: false
---

# Global settings

UltimateTimber's global settings control every configured tree. The defaults below come from the bundled <Text color="#38bdf8"><code>config.yml</code></Text>. Your saved configuration can differ, especially after upgrading. Change only the values you want to customize, then run <Text color="#fbbf24"><code>/ut reload</code></Text> or restart the server.

<Callout variant="info">
**Start with the behavior you want to change.** Use the grouped reference below, then check [tree definitions](tree-definitions.md) for species settings or [loot and required axe](loot-axe.md) for rewards and custom items.
</Callout>

## Tree detection and removal

| Setting | Default | What it controls |
| --- | --- | --- |
| <Text color="#38bdf8"><code>Disabled Worlds</code></Text> | <Text color="#38bdf8"><code>disabled_world_name</code></Text> | Exact world names where tree felling is disabled. Replace the placeholder or use an empty list. |
| <Text color="#38bdf8"><code>Max Logs Per Chop</code></Text> | <Text color="#34d399"><code>150</code></Text> | Maximum detected logs included in one chop. |
| <Text color="#38bdf8"><code>Leaves Required For Tree</code></Text> | <Text color="#34d399"><code>5</code></Text> | Minimum matching leaves needed to recognize a tree. |
| <Text color="#38bdf8"><code>Destroy Leaves</code></Text> | <Text color="#34d399"><code>true</code></Text> | Whether detected leaves are removed with the tree. |
| <Text color="#38bdf8"><code>Break Entire Tree Base</code></Text> | <Text color="#34d399"><code>false</code></Text> | Requires the remaining supported base logs to be broken before the tree can topple. |
| <Text color="#38bdf8"><code>Destroy Initiated Block</code></Text> | <Text color="#34d399"><code>false</code></Text> | Removes the starting log before the animation rather than including it in the animated block set. |
| <Text color="#38bdf8"><code>Only Detect Logs Upwards</code></Text> | <Text color="#34d399"><code>true</code></Text> | Restricts log detection below the starting height. |
| <Text color="#38bdf8"><code>Ignore Placed Blocks</code></Text> | <Text color="#34d399"><code>true</code></Text> | Skips blocks currently tracked as player-placed. |
| <Text color="#38bdf8"><code>Ignore Placed Blocks Memory Size</code></Text> | <Text color="#34d399"><code>5000</code></Text> | Maximum placed-block locations kept in memory. This tracking does not survive a restart. |

## Player access and cooldowns

| Setting | Default | What it controls |
| --- | --- | --- |
| <Text color="#38bdf8"><code>Only Topple While</code></Text> | <Text color="#8b5cf6"><code>ALWAYS</code></Text> | Accepts <Text color="#8b5cf6"><code>ALWAYS</code></Text>, <Text color="#8b5cf6"><code>SNEAKING</code></Text>, or <Text color="#8b5cf6"><code>NOT_SNEAKING</code></Text>. |
| <Text color="#38bdf8"><code>Allow Creative Mode</code></Text> | <Text color="#34d399"><code>true</code></Text> | Allows players in creative mode to fell trees. |
| <Text color="#38bdf8"><code>Require Chop Permission</code></Text> | <Text color="#34d399"><code>false</code></Text> | Requires <Text color="#38bdf8"><code>ultimatetimber.chop</code></Text> when enabled. |
| <Text color="#38bdf8"><code>Player Tree Topple Cooldown</code></Text> | <Text color="#34d399"><code>false</code></Text> | Enables a cooldown between successful felling actions. |
| <Text color="#38bdf8"><code>Player Tree Topple Cooldown Length</code></Text> | <Text color="#34d399"><code>5</code></Text> | Cooldown in seconds. Players with <Text color="#38bdf8"><code>ultimatetimber.bypasscooldown</code></Text> skip it. |

Players can also use <Text color="#fbbf24"><code>/ut toggle</code></Text> to enable or disable their own chopping.

## Tools and item drops

| Setting | Default | What it controls |
| --- | --- | --- |
| <Text color="#38bdf8"><code>Ignore Required Tools</code></Text> | <Text color="#34d399"><code>false</code></Text> | Bypasses both material checks and the custom required-axe check when enabled. |
| <Text color="#38bdf8"><code>Realistic Tool Damage</code></Text> | <Text color="#34d399"><code>true</code></Text> | Calculates tool damage from detected logs; otherwise uses a one-point base cost. |
| <Text color="#38bdf8"><code>Protect Tool</code></Text> | <Text color="#34d399"><code>false</code></Text> | Rejects a chop if the estimated damage would break the tool. |
| <Text color="#38bdf8"><code>Apply Silk Touch</code></Text> | <Text color="#34d399"><code>true</code></Text> | Uses original block drops instead of custom per-block loot when the axe has Silk Touch. |
| <Text color="#38bdf8"><code>Apply Silk Touch Tool Damage</code></Text> | <Text color="#34d399"><code>true</code></Text> | Counts detected logs and leaves toward durability when the axe has Silk Touch. |
| <Text color="#38bdf8"><code>Realistic Drops</code></Text> | <Text color="#34d399"><code>true</code></Text> | Drops loot at animated block loot locations. Disable it to use the initiating log's location. |
| <Text color="#38bdf8"><code>Add Items To Inventory</code></Text> | <Text color="#34d399"><code>false</code></Text> | Sends item drops to inventory, with overflow near the player. Overrides <Text color="#38bdf8"><code>Realistic Drops</code></Text>. |
| <Text color="#38bdf8"><code>Bonus Loot Multiplier</code></Text> | <Text color="#34d399"><code>2.0</code></Text> | Multiplies configured loot chances for players with <Text color="#38bdf8"><code>ultimatetimber.bonusloot</code></Text>. |

<Hint>Loot chances use percentages. <Text color="#38bdf8"><code>Chance: 5.0</code></Text> means 5 percent, or 10 percent with the default bonus multiplier for a player who has the bonus-loot permission.</Hint>

See [Tools and loot](../features/loot-and-tools.md) for how global and per-tree rewards combine.

## Sapling replanting

| Setting | Default | What it controls |
| --- | --- | --- |
| <Text color="#38bdf8"><code>Replant Saplings</code></Text> | <Text color="#34d399"><code>true</code></Text> | Replants the configured sapling at cleared log locations with valid soil. |
| <Text color="#38bdf8"><code>Always Replant Sapling</code></Text> | <Text color="#34d399"><code>false</code></Text> | Attempts initial-log replanting during detection even when normal toppling checks fail; still needs an empty location and valid soil. |
| <Text color="#38bdf8"><code>Replant Saplings Cooldown</code></Text> | <Text color="#34d399"><code>3</code></Text> | Seconds of temporary break protection for newly replanted saplings. |
| <Text color="#38bdf8"><code>Falling Blocks Replant Saplings</code></Text> | <Text color="#34d399"><code>true</code></Text> | Allows falling leaves to plant saplings at their impact locations. |
| <Text color="#38bdf8"><code>Falling Blocks Replant Saplings Chance</code></Text> | <Text color="#34d399"><code>1.0</code></Text> | Percentage chance for falling-leaf replanting, so the default is 1 percent. |

Valid planting soil comes from the global <Text color="#38bdf8"><code>Plantable Soil</code></Text> list plus the current tree's list. Changing a tree's soil list does not remove globally accepted soil.

## Animations, effects, and impact

| Setting | Default | What it controls |
| --- | --- | --- |
| <Text color="#38bdf8"><code>Tree Animation Type</code></Text> | <Text color="#8b5cf6"><code>FANCY</code></Text> | Accepts <Text color="#8b5cf6"><code>FANCY</code></Text>, <Text color="#8b5cf6"><code>CRUMBLE</code></Text>, <Text color="#8b5cf6"><code>DISINTEGRATE</code></Text>, or <Text color="#8b5cf6"><code>NONE</code></Text>. |
| <Text color="#38bdf8"><code>Use Custom Sounds</code></Text> | <Text color="#34d399"><code>true</code></Text> | Enables the plugin's animation and impact sounds. |
| <Text color="#38bdf8"><code>Use Custom Particles</code></Text> | <Text color="#34d399"><code>true</code></Text> | Enables the plugin's animation and impact particles. |
| <Text color="#38bdf8"><code>Scatter Tree Blocks On Ground</code></Text> | <Text color="#34d399"><code>false</code></Text> | Allows animated blocks to remain on the ground rather than disappearing after impact. |
| <Text color="#38bdf8"><code>Falling Blocks Deal Damage</code></Text> | <Text color="#34d399"><code>true</code></Text> | Allows falling tree blocks to damage players. |
| <Text color="#38bdf8"><code>Falling Block Damage</code></Text> | <Text color="#34d399"><code>1</code></Text> | Damage amount used for a falling-block hit. |
| <Text color="#38bdf8"><code>Fragile Blocks</code></Text> | <Text color="#8b5cf6"><code>GLASS</code></Text>, <Text color="#8b5cf6"><code>ICE</code></Text>, <Text color="#8b5cf6"><code>PACKED_ICE</code></Text>, <Text color="#8b5cf6"><code>BLUE_ICE</code></Text> | Materials treated as fragile by the falling-block impact logic. |

## Block logging, queueing, and hooks

<Text color="#38bdf8"><code>CoreProtect Logging</code></Text> defaults to <Text color="#34d399"><code>false</code></Text>. With CoreProtect API 10 or newer installed, enable it to record tree block removals. See [Compatibility](../features/compatibility.md).

```yaml
Queued Block Replacement:
  Mode: NEVER
  Threshold: 20
  Max Per Tick: 1000
```

The queue mode accepts <Text color="#8b5cf6"><code>NEVER</code></Text>, <Text color="#8b5cf6"><code>ALWAYS</code></Text>, or <Text color="#8b5cf6"><code>DYNAMIC</code></Text>. Dynamic mode enables queueing at the configured online-player threshold. [Performance settings](performance-and-integrations.md) explains how to choose a mode and limit.

The <Text color="#38bdf8"><code>Hooks</code></Text> section controls optional integration behavior. See [Compatibility](../features/compatibility.md) for the implemented integrations and the meaning of each switch.

## Player statistics and leaderboards

Statistics are enabled by default. When enabled, UltimateTimber records successful tree fellings and the logs and leaves removed. PlaceholderAPI can show a player's totals or the cached top 10 for trees, logs, leaves, and total blocks. See the [PlaceholderAPI guide](../features/placeholderapi.md) for every placeholder pattern.

```yaml
Statistics:
  Enabled: true
  Top Player Refresh: 60
```

| Setting | Default | What it controls |
| --- | --- | --- |
| <Text color="#38bdf8"><code>Statistics.Enabled</code></Text> | <Text color="#34d399"><code>true</code></Text> | Tracks player totals and enables the statistics placeholders. |
| <Text color="#38bdf8"><code>Statistics.Top Player Refresh</code></Text> | <Text color="#34d399"><code>60</code></Text> | Seconds between top 10 leaderboard refreshes. Values below 1 are treated as 1 second. |

Leaderboard queries run asynchronously and cache only the top 10 players for each statistic. Placeholder requests read that cache instead of querying the database.

## Global loot, custom axes, and individual trees

The rest of <Text color="#38bdf8"><code>config.yml</code></Text> is organized into three sections:

- <Text color="#38bdf8"><code>Global Loot</code></Text> defines planting soil, accepted tools, and log, leaf, and whole-tree rewards.
- Top-level <Text color="#38bdf8"><code>Required Axe</code></Text> defines the custom item given by <Text color="#fbbf24"><code>/ut give &lt;player&gt;</code></Text>. The global or per-tree <Text color="#38bdf8"><code>Required Axe</code></Text> Boolean enables that requirement.
- <Text color="#38bdf8"><code>Trees</code></Text> defines each species' materials, sapling, detection distances, original drops, and additional tools and rewards.

See [Loot and required axe](loot-axe.md) and [Tree definitions](tree-definitions.md) for focused YAML examples.
