---
title: "Configuration"
description: "Reference for UltimateTimber config.yml, global settings, tree definitions, and loot."
order: 0
hidden: false
categoryOrder: 3
---

# Configuration

UltimateTimber reads its settings from <Text color="#38bdf8"><code>plugins/UltimateTimber/config.yml</code></Text>. Use this file to choose an animation, control replanting and tool damage, add rewards, and configure individual tree species. Keep setting names, spaces, and capitalization exactly as shown in the examples.

<Callout variant="info">
  You can edit the config while the server is stopped, then start it again. For small changes on a running server, use <br /><Text color="#fbbf24"><code>/ut reload</code></Text>.
</Callout>

- [Global settings](global-settings.md) groups the main settings by behavior, with shipped defaults and explanations.
- [Tree definitions](tree-definitions.md) shows species settings, materials, saplings, and detection limits.
- [Loot and required axe](loot-axe.md) covers item drops, console commands, tools, and custom axe metadata.
- [Performance settings](performance-and-integrations.md) explains queued block replacement and links to integration details.
- [Leaderboards](leaderboards.md) explains stat holograms, display formats, and saved placements.

<Hint>All YAML samples use the same key names as the shipped <Text color="#38bdf8"><code>config.yml</code></Text>.</Hint>

## Which settings should I change first?

| Goal | Setting | Shipped default |
| --- | --- | --- |
| Change how trees fall | <Text color="#38bdf8"><code>Tree Animation Type</code></Text> | <Text color="#8b5cf6"><code>FANCY</code></Text> |
| Fell trees only while sneaking | <Text color="#38bdf8"><code>Only Topple While</code></Text> | <Text color="#8b5cf6"><code>ALWAYS</code></Text> |
| Disable tree felling in selected worlds | <Text color="#38bdf8"><code>Disabled Worlds</code></Text> | A placeholder world name |
| Require a player permission | <Text color="#38bdf8"><code>Require Chop Permission</code></Text> | <Text color="#34d399"><code>false</code></Text> |
| Send drops to the inventory | <Text color="#38bdf8"><code>Add Items To Inventory</code></Text> | <Text color="#34d399"><code>false</code></Text> |
| Automatically replace felled trees with saplings | <Text color="#38bdf8"><code>Replant Saplings</code></Text> | <Text color="#34d399"><code>true</code></Text> |
| Stop a chop that would break the axe | <Text color="#38bdf8"><code>Protect Tool</code></Text> | <Text color="#34d399"><code>false</code></Text> |
| Limit logs handled per chop | <Text color="#38bdf8"><code>Max Logs Per Chop</code></Text> | <Text color="#34d399"><code>150</code></Text> |
| Enable player stats or change leaderboard refresh | <Text color="#38bdf8"><code>Statistics</code></Text> | Enabled, refreshes every 60 seconds |
| Change leaderboard titles or row formatting | <Text color="#38bdf8"><code>leaderboard.yml</code></Text> | Four stat types, 10 rows each |

<Callout variant="success">
**Existing settings are migrated automatically when upgrading from a supported older format.** You do not need to rebuild your configuration. These guides explain changes you choose to make after installation.
</Callout>
