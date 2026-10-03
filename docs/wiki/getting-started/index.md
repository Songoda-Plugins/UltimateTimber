---
title: "Getting started"
description: "Install UltimateTimber, configure it, and find the command reference."
order: 0
hidden: false
categoryOrder: 1
---

# Getting started

UltimateTimber works on Paper and declares Folia support. Download the current jar from the [Songoda Reborn plugin page](https://songoda-reborn.com/plugins/ultimatetimber).

## Install the plugin

1. Stop the server.
2. Put the UltimateTimber jar in the server's <Text color="#38bdf8"><code>plugins</code></Text> folder.
3. Start the server and let the plugin create its configuration files.
4. Edit <Text color="#38bdf8"><code>plugins/UltimateTimber/config.yml</code></Text>, then restart or run <Text color="#fbbf24"><code>/ut reload</code></Text> after changing settings.

<Callout variant="success">
**Upgrading from an older version? UltimateTimber updates your configuration automatically on startup.** You do not need to rename keys, copy settings into a new file, or run a migration command. The plugin keeps a copy of the older configuration before converting it.
</Callout>

## Supported plugins

Optional integrations add features such as block logging, skill experience, job rewards, and player statistics. See [Compatibility](../features/compatibility.md) for supported plugins and setup requirements.

## Next steps

1. Break a natural tree's log with a vanilla axe to try the default felling animation.
2. Use [commands and permissions](commands-permissions.md) to set up player access and the chopping toggle.
3. Choose an animation, configure drops, or change replanting in the [configuration guide](../configuration/index.md).

## Why does breaking a log not fell the tree?

UltimateTimber needs matching logs and leaves, an accepted tool, and an enabled chopping state. The default configuration requires at least five matching leaves. Check [Tree felling](../features/tree-felling.md) for the full checklist, including worlds, cooldowns, and permissions.
