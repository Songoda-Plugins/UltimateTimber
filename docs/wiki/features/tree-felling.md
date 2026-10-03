---
title: Tree felling
description: See how UltimateTimber detects and fells trees, handles animations, and replants saplings.
order: 1
hidden: false
---

# Tree felling

<Callout variant="info">
Break a configured log with an accepted tool and UltimateTimber detects the connected tree, checks the player's settings and permissions, then handles the tree using the selected animation.
</Callout>

## What happens when you chop

UltimateTimber starts from the broken log and searches for matching trunk logs, branches, and leaves. Tree definitions control which materials match and how far connected blocks can be from each other. Global settings set limits such as the minimum number of leaves and the maximum logs handled in one chop.

The plugin checks the world, game mode, sneak state, chop toggle, cooldown, permissions, and required tools before felling the tree. It can also ignore blocks tracked as player-placed to help protect builds.

<h2 id="animations-and-impact">Animations and impact</h2>

Set <Text color="#38bdf8"><code>Tree Animation Type</code></Text> to one of these values:

| Value | What players see |
| --- | --- |
| <Text color="#8b5cf6"><code>FANCY</code></Text> | The tree topples using animated falling blocks. This is the default. |
| <Text color="#8b5cf6"><code>CRUMBLE</code></Text> | Blocks fall in a crumbling animation. |
| <Text color="#8b5cf6"><code>DISINTEGRATE</code></Text> | Tree blocks disappear in sequence. |
| <Text color="#8b5cf6"><code>NONE</code></Text> | The detected tree is removed immediately. |

Sounds, particles, fragile blocks, and falling-block damage have separate settings. <Text color="#38bdf8"><code>Falling Blocks Deal Damage</code></Text> is enabled by default, with <Text color="#38bdf8"><code>Falling Block Damage: 1</code></Text>. Disable it if you do not want animated trees to damage players.

## Can I fell trees only while sneaking?

Yes. Set <Text color="#38bdf8"><code>Only Topple While</code></Text> to <Text color="#8b5cf6"><code>SNEAKING</code></Text>. Use <Text color="#8b5cf6"><code>NOT_SNEAKING</code></Text> for the opposite behavior, or <Text color="#8b5cf6"><code>ALWAYS</code></Text> to allow either state. Players can also use <Text color="#fbbf24"><code>/ut toggle</code></Text> to switch their own chopping state.

<h2 id="sapling-regrowth">Sapling regrowth</h2>

<Text color="#38bdf8"><code>Replant Saplings</code></Text> plants the configured sapling in a cleared log location when the block below is valid soil. <Text color="#38bdf8"><code>Replant Saplings Cooldown</code></Text> protects a newly planted sapling from breaking for the configured number of seconds, with a default of three seconds.

Falling leaves can also plant saplings when they land, controlled separately by <Text color="#38bdf8"><code>Falling Blocks Replant Saplings</code></Text> and its percentage chance. <Text color="#38bdf8"><code>Always Replant Sapling</code></Text> attempts to replant the initial log during detection even when ordinary toppling checks fail. It still requires <Text color="#38bdf8"><code>Replant Saplings</code></Text>, an empty target block, and valid soil; it does not guarantee regrowth in every situation.

<Hint>Falling leaf replant chance is a percentage. The default <Text color="#34d399"><code>1.0</code></Text> means a 1 percent chance.</Hint>

## Why is a tree not falling?

Check these conditions in order:

1. The world is not in <Text color="#38bdf8"><code>Disabled Worlds</code></Text>, and <Text color="#fbbf24"><code>/ut toggle</code></Text> has not disabled your chopping.
2. You hold an accepted tool. By default, all six vanilla axes are accepted.
3. The blocks match a tree definition and the tree meets <Text color="#38bdf8"><code>Leaves Required For Tree</code></Text>, which defaults to <Text color="#34d399"><code>5</code></Text>.
4. Your sneak state matches <Text color="#38bdf8"><code>Only Topple While</code></Text>, and creative mode is allowed if you use it.
5. You have <Text color="#38bdf8"><code>ultimatetimber.chop</code></Text> if <Text color="#38bdf8"><code>Require Chop Permission</code></Text> is enabled, and any enabled cooldown has expired.
6. The starting block's break event has not been cancelled by another plugin, and the block is not already animated or temporarily protected.
7. If <Text color="#38bdf8"><code>Protect Tool</code></Text> is enabled, the axe has enough durability for the estimated chop.
8. If <Text color="#38bdf8"><code>Hooks.Require Ability Active</code></Text> is enabled and mcMMO is available, your Tree Feller ability is active. If mcMMO is missing or disabled, UltimateTimber skips this check.

## Does player-placed block tracking protect every build?

<Text color="#38bdf8"><code>Ignore Placed Blocks</code></Text> skips blocks tracked as placed by players. The tracking list is held in memory and bounded by <Text color="#38bdf8"><code>Ignore Placed Blocks Memory Size</code></Text>, which defaults to <Text color="#34d399"><code>5000</code></Text>. Older entries can be evicted and restarting resets tracking. It is not a permanent record of every placed block.

## More feature guides

- [Tools and loot](loot-and-tools.md) covers tool checks, durability, original drops, bonus loot, and custom rewards.
- [Compatibility](compatibility.md) lists supported server platforms and plugin integrations.
