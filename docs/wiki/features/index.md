---
title: "Features"
description: "UltimateTimber tree felling, four animations, automatic replanting, custom loot, tool requirements, and optional integrations."
order: 0
hidden: false
categoryOrder: 2
---

# UltimateTimber features

UltimateTimber turns chopping a configured tree into one felling action. It detects connected logs and leaves, applies your tool and gameplay rules, then removes or animates the detected blocks. You can configure each tree species separately.

## What can UltimateTimber do?

| Feature | What it does | Guide |
| --- | --- | --- |
| Tree detection | Matches configured logs and leaves, with distance and block-count limits. | [Tree felling](tree-felling.md) |
| Four animations | Topples, crumbles, disintegrates, or removes a tree instantly. | [Animations](tree-felling.md#animations-and-impact) |
| Automatic replanting | Plants configured saplings on valid soil and temporarily protects them from breaking. | [Sapling regrowth](tree-felling.md#sapling-regrowth) |
| Tools and durability | Accepts configured tools, supports a custom required axe, and applies durability costs. | [Tools and loot](loot-and-tools.md) |
| Custom rewards | Adds item drops or console commands per log, leaf, or whole tree. | [Loot configuration](../configuration/loot-axe.md) |
| Player controls | Provides a chopping toggle, optional permissions, sneak rules, and a cooldown. | [Commands and permissions](../getting-started/commands-permissions.md) |
| Block replacement queue | Spreads queued replacements across ticks when enabled. | [Performance settings](../configuration/performance-and-integrations.md) |
| Optional integrations | Provides block logging and a compatibility reference. | [Compatibility](compatibility.md) |
| Player statistics and leaderboards | Exposes player totals and cached top 10 trees, logs, leaves, and blocks through PlaceholderAPI, and can display those rankings as persistent holograms. | [PlaceholderAPI](placeholderapi.md) and [leaderboard setup](../configuration/leaderboards.md) |

<Callout variant="info">
**The defaults are ready for ordinary tree chopping.** Vanilla axes are accepted, the animation is <Text color="#8b5cf6"><code>FANCY</code></Text>, and sapling replanting is enabled. Customize the behavior in <Text color="#38bdf8"><code>plugins/UltimateTimber/config.yml</code></Text>.
</Callout>

## Which tree types are included?

The bundled configuration includes oak, spruce, birch, jungle, acacia, dark oak, azalea, cherry, mangrove, and pale oak. A material must exist in your server version to be usable. You can also add [custom tree definitions](../configuration/tree-definitions.md) using Bukkit material names.

## Can I change the behavior for one species?

Yes. Each entry under <Text color="#38bdf8"><code>Trees</code></Text> defines its materials, sapling, detection distances, original drops, extra loot, and additional accepted tools. Global and tree-specific loot are combined. A tree's soil and tool lists add to the global lists.

See [Global settings](../configuration/global-settings.md) for the shipped configuration and [Tree definitions](../configuration/tree-definitions.md) for a smaller example.
