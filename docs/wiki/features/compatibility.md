---
title: "Compatibility"
description: "Protection, logging, progression, quest, and PlaceholderAPI integrations for UltimateTimber."
order: 3
hidden: false
---

# Plugin compatibility

UltimateTimber has optional hooks for protection, block logging, progression rewards, quest actions, and player statistics. Install only the plugins you use. Missing optional plugins do not stop tree felling.

<Callout variant="info">
  The integrations listed here are optional. UltimateTimber checks whether each plugin is enabled before calling its hook. Most rewards also need to be enabled in <Text color="#38bdf8"><code>plugins/UltimateTimber/config.yml</code></Text>.
</Callout>

## At a glance

| Plugin | What UltimateTimber does | When it runs | Config |
| --- | --- | --- | --- |
| <Text color="#fbbf24"><b>WorldGuard</b></Text> | Checks whether the player can break a block in the region. | At the starting log and for each detected tree block. | Automatic when installed. |
| <Text color="#fbbf24"><b>GriefPrevention</b></Text> | Checks whether the player can break a block in a claim. | At the starting log and for each detected tree block. | Automatic when installed. |
| <Text color="#34d399"><b>UltimateClaims</b></Text> | Checks the player's break permission through the UltimateClaims API. | At the starting log and for each detected tree block. | Automatic when installed. |
| <Text color="#38bdf8"><b>CoreProtect</b></Text> | Logs removed tree blocks. | When a block is removed by tree felling. | <Text color="#38bdf8"><code>CoreProtect Logging</code></Text>, off by default. |
| <Text color="#34d399"><b>mcMMO</b></Text> | Gives Woodcutting experience, applies double drops, and can require Tree Feller. | Experience is checked as blocks are replaced, drops are checked when loot is selected, and the ability is checked before felling. | <Text color="#38bdf8"><code>Hooks</code></Text> settings. |
| <Text color="#34d399"><b>Jobs Reborn</b></Text> | Reports processed log breaks for the player's Jobs rewards. | Once per allowed log, except in Creative mode. | <Text color="#38bdf8"><code>Hooks.Apply Experience</code></Text>, enabled by default. |
| <Text color="#a78bfa"><b>AuraSkills</b></Text> | Gives XP to one configured skill. | Once after the tree's fall animation completes. | <Text color="#38bdf8"><code>Hooks.AuraSkills</code></Text>. |
| <Text color="#a78bfa"><b>EcoSkills</b></Text> | Gives XP to one configured skill. | Once after the tree's fall animation completes. | <Text color="#38bdf8"><code>Hooks.EcoSkills</code></Text>. |
| <Text color="#a78bfa"><b>EcoJobs</b></Text> | Gives XP to one configured active job. | Once after the tree's fall animation completes. | <Text color="#38bdf8"><code>Hooks.EcoJobs</code></Text>. |
| <Text color="#a78bfa"><b>MMOCore</b></Text> | Gives class XP, profession XP, or both. | Once per enabled reward after the tree's fall animation completes. | <Text color="#38bdf8"><code>Hooks.MMOCore</code></Text>. |
| <Text color="#fb7185"><b>BetonQuest</b></Text> | Runs a configured player action. | Once after the tree's fall animation completes. | <Text color="#38bdf8"><code>Hooks.BetonQuest</code></Text>. |
| <Text color="#38bdf8"><b>PlaceholderAPI</b></Text> | Registers player statistics and cached top-player placeholders. | When PlaceholderAPI requests a value. | See the [PlaceholderAPI guide](placeholderapi.md). |

## Protection checks

SongodaCore provides the WorldGuard and GriefPrevention protection checks. UltimateTimber adds an UltimateClaims provider using its public API. The hook registry checks every installed, enabled provider and denies a block if any provider says the player cannot break it.

UltimateTimber checks the log that starts the chop and checks detected tree blocks again before processing each one. A denied tree block stays in place and skips its block loot, mcMMO Woodcutting experience, and Jobs action. Other allowed blocks can still be processed. If the starting log is denied, the chop does not start.

<Callout variant="success">
  Protection checks need no UltimateTimber config switch. Install and configure your protection plugin as usual. UltimateTimber uses its break check automatically.
</Callout>

SongodaCore also exposes a protection check for interactions, but UltimateTimber currently calls the break check only.

## mcMMO and Jobs Reborn

These hooks run while the animation processes individual blocks. They are separate from the once-per-tree rewards below.

```yaml
Hooks:
  Apply Experience: true
  Apply Extra Drops: true
  Require Ability Active: false
```

| Setting | Default | Effect |
| --- | --- | --- |
| <Text color="#38bdf8"><code>Apply Experience</code></Text> | <Text color="#34d399"><code>true</code></Text> | Enables mcMMO Woodcutting experience and Jobs Reborn log-break actions. Jobs actions run once per allowed log and are skipped in Creative mode. |
| <Text color="#38bdf8"><code>Apply Extra Drops</code></Text> | <Text color="#34d399"><code>true</code></Text> | Lets mcMMO's Woodcutting double-drop check duplicate selected original drops, configured item loot, and configured loot commands. This setting is separate from <Text color="#38bdf8"><code>Apply Experience</code></Text>. |
| <Text color="#38bdf8"><code>Require Ability Active</code></Text> | <Text color="#34d399"><code>false</code></Text> | Requires mcMMO Tree Feller to be active before felling when mcMMO is enabled. |

<Callout variant="warning">
  If <Text color="#38bdf8"><code>Require Ability Active</code></Text> is on but mcMMO is missing or disabled, UltimateTimber skips the ability check. Players can still chop trees.
</Callout>

## Tree-completion rewards

These rewards are off by default. Enable the integration you want, then use an ID that exists in that plugin's configuration. UltimateTimber calls these rewards once after a tree's fall animation completes, not once per log.

```yaml
Hooks:
  AuraSkills:
    Enabled: false
    Skill: "foraging"
    Experience Per Tree: 1.0
  EcoSkills:
    Enabled: false
    Skill: "woodcutting"
    Experience Per Tree: 1.0
  EcoJobs:
    Enabled: false
    Job: "lumberjack"
    Experience Per Tree: 1.0
  MMOCore:
    Class Experience Enabled: false
    Class Experience Per Tree: 1.0
    Profession Experience Enabled: false
    Profession: "woodcutting"
    Profession Experience Per Tree: 1.0
  BetonQuest:
    Enabled: false
    Package: "my-quest-package"
    Action: "tree_chopped"
```

| Integration | Options |
| --- | --- |
| AuraSkills | Set <Text color="#38bdf8"><code>Enabled</code></Text>, a skill ID, and <Text color="#38bdf8"><code>Experience Per Tree</code></Text>. Both plugin names, <Text color="#8b5cf6"><code>AureliumSkills</code></Text> and <Text color="#8b5cf6"><code>AuraSkills</code></Text>, are recognized. |
| EcoSkills | Set <Text color="#38bdf8"><code>Enabled</code></Text>, a skill ID, and <Text color="#38bdf8"><code>Experience Per Tree</code></Text>. |
| EcoJobs | Set <Text color="#38bdf8"><code>Enabled</code></Text>, a job ID, and <Text color="#38bdf8"><code>Experience Per Tree</code></Text>. XP is awarded only if that job is active for the player. |
| MMOCore | Enable class and profession XP separately. Profession XP also needs a profession ID. |
| BetonQuest | Set the existing BetonQuest package and action names. |

<Callout variant="info">
  The BetonQuest hook runs the configured action for the player. It does not add a block-break objective or report one break for every log. If you want a quest to count felled trees, use a custom objective or listen for <Text color="#38bdf8"><code>TreeFellEvent</code></Text> in an integration plugin. See the [Developer API guide](../developers/index.md).
</Callout>

The skill, job, and profession names must match IDs registered by their plugins. AuraSkills accepts IDs such as <Text color="#8b5cf6"><code>foraging</code></Text> or <Text color="#8b5cf6"><code>namespace:skill</code></Text>. BetonQuest's <Text color="#38bdf8"><code>Package</code></Text> and <Text color="#38bdf8"><code>Action</code></Text> must already exist.

## CoreProtect logging

Install CoreProtect API 10 or newer and turn on logging:

```yaml
CoreProtect Logging: true
```

CoreProtect records block removals. It does not grant or deny permission to chop. Logging is disabled by default.

## Player statistics

Install PlaceholderAPI to use the player statistics and top-player values in scoreboards, chat formats, and other supported plugins. UltimateTimber registers its expansion automatically through its PlaceholderAPI hook. Statistics are enabled by default. See the [PlaceholderAPI guide](placeholderapi.md) for every placeholder and refresh setting.

## Does UltimateTimber need Vault?

No. Vault is not required for tree felling, item loot, or sapling replanting, and UltimateTimber does not have a built-in Vault payment or economy reward setting. For money rewards, use a [console-command loot entry](../configuration/loot-axe.md) with your economy plugin's command. That economy plugin may require Vault.
