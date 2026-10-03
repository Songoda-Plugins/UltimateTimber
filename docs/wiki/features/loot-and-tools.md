---
title: "Tools and loot"
description: "Learn how tool checks, durability, original drops, bonus loot, and custom rewards work."
order: 2
hidden: false
---

# Tools and loot

UltimateTimber can keep the original log and leaf drops, add configured item drops, and run console commands when a tree is felled. Loot can be set globally or for an individual tree definition.

<Hint>A tool is accepted if it appears in either the global required-tool list or the current tree's list.</Hint>

- Log loot is rolled for each detected log.
- Leaf loot is rolled for each detected leaf.
- Entire Tree loot is rolled once for the tree.

## Where do items drop?

| Setting | Result |
| --- | --- |
| <Text color="#38bdf8"><code>Realistic Drops: true</code></Text> | Drops appear at each animated block's loot location. |
| <Text color="#38bdf8"><code>Realistic Drops: false</code></Text> | Drops appear at the log that started the felling. |
| <Text color="#38bdf8"><code>Add Items To Inventory: true</code></Text> | Items go to the player's inventory; overflow drops near the player. This takes precedence over <Text color="#38bdf8"><code>Realistic Drops</code></Text>. |

## How does Silk Touch affect loot?

When <Text color="#38bdf8"><code>Apply Silk Touch</code></Text> is enabled and the axe has Silk Touch, detected logs and leaves use original block drops instead of configured per-block bonus loot. Whole-tree loot still runs separately. Disable <Text color="#38bdf8"><code>Apply Silk Touch</code></Text> to keep the configured log and leaf loot selection even when the tool has Silk Touch.

## Tools and durability

<Text color="#38bdf8"><code>Global Loot.Required Tools</code></Text> lists tools accepted for every tree. A tree's own <Text color="#38bdf8"><code>Required Tools</code></Text> adds tools for that tree. A tool is accepted if it appears in either list. Set <Text color="#38bdf8"><code>Ignore Required Tools: true</code></Text> to bypass tool checks.

```yaml
Global Loot:
  Required Tools:
    - WOODEN_AXE
    - STONE_AXE

Trees:
  oak:
    Required Tools:
      - DIAMOND_AXE
```

Set global <Text color="#38bdf8"><code>Required Axe: true</code></Text> or a tree's <Text color="#38bdf8"><code>Required Axe: true</code></Text> to require the custom item defined at the top-level <Text color="#38bdf8"><code>Required Axe</code></Text> key. The item is compared with its configured metadata. <Text color="#fbbf24"><code>/ut give &lt;player&gt;</code></Text> gives this item.

With <Text color="#38bdf8"><code>Realistic Tool Damage</code></Text> enabled, tool durability is based on the number of logs chopped. If the axe has Silk Touch and <Text color="#38bdf8"><code>Apply Silk Touch Tool Damage</code></Text> is enabled, durability counts detected logs and leaves. This durability setting is separate from <Text color="#38bdf8"><code>Apply Silk Touch</code></Text>, which controls loot. Unbreaking can reduce the applied damage. <Text color="#38bdf8"><code>Protect Tool</code></Text> rejects a chop when the estimated damage would break the tool.

<Callout variant="info">
**Global and tree-specific rewards are combined.** Adding an oak loot entry does not replace the global loot table. Keep this in mind when balancing bonus drops.
</Callout>

Players with <Text color="#38bdf8"><code>ultimatetimber.bonusloot</code></Text> use <Text color="#38bdf8"><code>Bonus Loot Multiplier</code></Text> to multiply each configured loot chance. A chance is a percentage, so <Text color="#34d399"><code>5.0</code></Text> means 5 percent before the bonus multiplier.

## Configure item and command rewards

Use the [loot and required axe configuration](../configuration/loot-axe.md) guide for YAML examples of item drops, console commands, and custom axe metadata.

