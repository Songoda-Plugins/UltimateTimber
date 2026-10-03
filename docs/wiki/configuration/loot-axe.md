---
title: "Loot & Axe"
description: "Build global and per-tree item or command loot tables and configure custom axe requirements."
order: 3
hidden: false
---

# Loot and required axe configuration

Loot tables can be defined under <Text color="#38bdf8"><code>Global Loot</code></Text> or inside one tree under <Text color="#38bdf8"><code>Trees.&lt;key&gt;</code></Text>. Each table is a map of entries. An entry can drop a material, run a console command, or do both.

<Callout variant="info">
  <Text color="#38bdf8"><code>Chance</code></Text> is a percentage. A value of <Text color="#34d399"><code>2.5</code></Text> means 2.5 percent before any bonus loot multiplier.
</Callout>

```yaml
Global Loot:
  Log Loot:
    bonus-log:
      Material: DIAMOND
      Chance: 2.5
  Leaf Loot: {}
  Entire Tree Loot:
    tree-command:
      Command: "say <player> felled a <type> tree at <x-pos>,<y-pos>,<z-pos>" # Placeholders are replaced when the reward runs.
      Chance: 100.0
```

## Loot entry fields

- <Text color="#38bdf8"><code>Material</code></Text> is a Bukkit material name for an item reward.
- <Text color="#38bdf8"><code>Command</code></Text> is dispatched from the console when the entry succeeds.
- <Text color="#38bdf8"><code>Chance</code></Text> is the percentage chance for the entry. <Text color="#34d399"><code>2.5</code></Text> means 2.5 percent before any bonus multiplier.
- Command placeholders are <Text color="#38bdf8"><code>&lt;player&gt;</code></Text>, <Text color="#38bdf8"><code>&lt;type&gt;</code></Text>, <Text color="#38bdf8"><code>&lt;x-pos&gt;</code></Text>, <Text color="#38bdf8"><code>&lt;y-pos&gt;</code></Text>, and <Text color="#38bdf8"><code>&lt;z-pos&gt;</code></Text>.

Position values use the block that produced the loot.

## Global loot and tools

<Text color="#38bdf8"><code>Global Loot.Plantable Soil</code></Text> sets soil accepted by all trees. Its shipped defaults are <Text color="#8b5cf6"><code>GRASS_BLOCK</code></Text>, <Text color="#8b5cf6"><code>DIRT</code></Text>, <Text color="#8b5cf6"><code>COARSE_DIRT</code></Text>, <Text color="#8b5cf6"><code>PODZOL</code></Text>, and <Text color="#8b5cf6"><code>ROOTED_DIRT</code></Text>. <Text color="#38bdf8"><code>Global Loot.Required Tools</code></Text> defaults to the six vanilla axes: wood, stone, iron, gold, diamond, and netherite. Global and per-tree required-tool lists are both considered when checking a tool. <Text color="#38bdf8"><code>Global Loot.Required Axe</code></Text> defaults to <Text color="#34d399"><code>false</code></Text>.

## Custom required axe

The top-level <Text color="#38bdf8"><code>Required Axe</code></Text> section describes an ItemStack. <Text color="#38bdf8"><code>Global Loot.Required Axe</code></Text> is a separate Boolean that requires that item for every tree. Each tree can also set its own <Text color="#38bdf8"><code>Required Axe</code></Text> Boolean.

```yaml
Required Axe:
  Material: DIAMOND_AXE
  Name: "<green>An Epic Axe"
  Lore:
    - "<gray>This axe... it's awesome."
  Enchants:
    - "unbreaking:3"
    - "efficiency:5"
  PDC:
    - "ultimatetimber:axe:BYTE:1"
    - "custom_pdc_key:value:STRING:My custom key"

```

The required axe is matched against the configured item metadata. Use <Text color="#fbbf24"><code>/ut give &lt;player&gt;</code></Text> to give it to a player.
