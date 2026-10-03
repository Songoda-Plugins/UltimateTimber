---
title: "Commands & Permissions"
description: "Learn UltimateTimber commands and the permission nodes that control felling."
order: 1
hidden: false
---

# Commands and permissions

Use <Text color="#fbbf24"><code>/ultimatetimber</code></Text> or its alias <Text color="#fbbf24"><code>/ut</code></Text> to manage UltimateTimber. The permission column shows the node required for each command.

## Commands

| Command | What it does | Permission |
| --- | --- | --- |
| <Text color="#fbbf24"><code>/ut</code></Text> | Shows command help. | No command-specific permission. |
| <Text color="#fbbf24"><code>/ut toggle</code></Text> | Turns your own tree chopping on or off. | <Text color="#a78bfa"><code>ultimatetimber.toggle</code></Text> |
| <Text color="#fbbf24"><code>/ut give &lt;player&gt;</code></Text> | Gives the target player the configured required axe. Reports an error if no required axe is configured. | <Text color="#a78bfa"><code>ultimatetimber.give</code></Text> |
| <Text color="#fbbf24"><code>/ut reload</code></Text> | Reloads the plugin configuration and language files. | <Text color="#a78bfa"><code>ultimatetimber.reload</code></Text> |
| <Text color="#fbbf24"><code>/ut leaderboard spawn &lt;type&gt;</code></Text> | Spawns a persistent top player stats hologram where you are standing. Types: <Text color="#38bdf8"><code>blocks</code></Text>, <Text color="#38bdf8"><code>trees</code></Text>, <Text color="#38bdf8"><code>logs</code></Text>, <Text color="#38bdf8"><code>leaves</code></Text>. | <Text color="#a78bfa"><code>ultimatetimber.leaderboard</code></Text> |
| <Text color="#fbbf24"><code>/ut leaderboard remove &lt;type&gt;</code></Text> | Removes the placed leaderboard hologram for that type. | <Text color="#a78bfa"><code>ultimatetimber.leaderboard</code></Text> |

## Tree-felling permissions

These permissions affect tree felling and loot rather than access to commands.

<Table>
  <Row>
    <Cell>
      Permission
    </Cell>
    <Cell>
      Effect
    </Cell>
  </Row>
  <Row>
    <Cell>
      <Text color="#a78bfa"><code>ultimatetimber.chop</code></Text>
    </Cell>
    <Cell>
      Allows tree felling when <Text color="#38bdf8"><code>Require Chop Permission</code></Text> is enabled.
    </Cell>
  </Row>
  <Row>
    <Cell>
      <Text color="#a78bfa"><code>ultimatetimber.bonusloot</code></Text>
    </Cell>
    <Cell>
      Multiplies configured loot chances by <Text color="#38bdf8"><code>Bonus Loot Multiplier</code></Text>.
    </Cell>
  </Row>
  <Row>
    <Cell>
      <Text color="#a78bfa"><code>ultimatetimber.bypasscooldown</code></Text>
    </Cell>
    <Cell>
      Lets the player skip the tree-felling cooldown.
    </Cell>
  </Row>
  <Row>
    <Cell>
      <Text color="#a78bfa"><code>ultimatetimber.animation.fancy</code></Text>
    </Cell>
    <Cell>
      Selects the fancy animation when exactly one animation permission is granted.
    </Cell>
  </Row>
  <Row>
    <Cell>
      <Text color="#a78bfa"><code>ultimatetimber.animation.crumble</code></Text>
    </Cell>
    <Cell>
      Selects the crumble animation when exactly one animation permission is granted.
    </Cell>
  </Row>
  <Row>
    <Cell>
      <Text color="#a78bfa"><code>ultimatetimber.animation.disintegrate</code></Text>
    </Cell>
    <Cell>
      Selects the disintegrate animation when exactly one animation permission is granted.
    </Cell>
  </Row>
  <Row>
    <Cell>
      <Text color="#a78bfa"><code>ultimatetimber.animation.none</code></Text>
    </Cell>
    <Cell>
      Selects no animation when exactly one animation permission is granted.
    </Cell>
  </Row>
</Table>

If a player has zero or multiple animation permissions, UltimateTimber uses the configured <Text color="#38bdf8"><code>Tree Animation Type</code></Text>.

Command and cooldown messages are in <Text color="#38bdf8"><code>plugins/UltimateTimber/lang.yml</code></Text>. The reload command reloads language and configuration files.

Leaderboard titles and row formatting live in <Text color="#38bdf8"><code>plugins/UltimateTimber/leaderboard.yml</code></Text>. See [Leaderboard configuration](../configuration/leaderboards.md) for available types and placeholders.
