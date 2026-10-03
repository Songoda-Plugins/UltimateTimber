---
title: "Developer API"
description: "Add UltimateTimber to a plugin, access its managers, and listen for tree events."
order: 5
hidden: false
categoryOrder: 4
---

# Developer API

Use the UltimateTimber API to detect trees, access plugin managers, and listen for felling and damage events. The API is published as its own Maven artifact. UltimateTimber must also be installed on the server.

## Maven repository and dependency

Add the Songoda Maven repository and the API dependency to your plugin's <Text color="#38bdf8"><code>pom.xml</code></Text>:

```xml
<repositories>
    <repository>
        <id>songoda-repo</id>
        <url>https://repo.songoda-reborn.com/repository/maven-public/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.songoda</groupId>
        <artifactId>UltimateTimber-API</artifactId>
        <version>5.0.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

<Text color="#38bdf8"><code>provided</code></Text> makes the API available at compile time without bundling another copy in your plugin jar. UltimateTimber provides the API classes at runtime.

Add UltimateTimber as a plugin dependency so it loads before your plugin:

```yaml
depend:
  - UltimateTimber
```

Use <Text color="#38bdf8"><code>softdepend</code></Text> instead if the integration is optional, and check that UltimateTimber is enabled before calling its API.

## Get API access

<Text color="#38bdf8"><code>UltimateTimberApi</code></Text> is a static entry point. You do not create an API instance. Get the manager you need after UltimateTimber has loaded, such as in your plugin's <Text color="#38bdf8"><code>onEnable</code></Text> method:

```java
import com.songoda.ultimatetimber.api.UltimateTimberApi;
import com.songoda.ultimatetimber.api.manager.TreeDetectionManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExamplePlugin extends JavaPlugin {
    private TreeDetectionManager treeDetectionManager;

    @Override
    public void onEnable() {
        this.treeDetectionManager = UltimateTimberApi.getTreeDetectionManager();
    }
}
```

For example, detect a tree before the starting block is broken:

```java
import com.songoda.ultimatetimber.api.tree.DetectedTree;
import org.bukkit.block.Block;

public DetectedTree detectTree(Block initialBlock) {
    DetectedTree tree = this.treeDetectionManager.detectTree(initialBlock);
    if (tree != null) {
        int logCount = tree.detectedTreeBlocks().getLogBlocks().size();
    }
    return tree;
}
```

On Folia, <Text color="#38bdf8"><code>detectTree</code></Text> only detects trees within the current region. Use <Text color="#38bdf8"><code>detectTreeAsync</code></Text> for trees that may cross region boundaries. Call it on the starting block's owning region and before that block is broken.

## Available managers

| Manager | Purpose |
| --- | --- |
| <Text color="#38bdf8"><code>TreeDetectionManager</code></Text> | Detect trees and match log or leaf materials to definitions. |
| <Text color="#38bdf8"><code>TreeFallManager</code></Text> | Check toppling conditions and start the felling flow. |
| <Text color="#38bdf8"><code>TreeAnimationManager</code></Text> | Run animations and track animated blocks and impact handling. |
| <Text color="#38bdf8"><code>ChoppingManager</code></Text> | Toggle chopping and manage player cooldowns. |
| <Text color="#38bdf8"><code>SaplingManager</code></Text> | Replant saplings and check temporary break protection. |
| <Text color="#38bdf8"><code>BlockReplacementManager</code></Text> | Replace tree blocks immediately or through the configured queue. |
| <Text color="#38bdf8"><code>PlacedBlockManager</code></Text> | Track player-placed blocks for detection exclusions. |
| <Text color="#38bdf8"><code>TreeDefinitionManager</code></Text> | List definitions, check tools, resolve soil and drops, and return the configured required axe. |

The <Text color="#38bdf8"><code>com.songoda.ultimatetimber.api.tree</code></Text> package contains <Text color="#38bdf8"><code>TreeDefinition</code></Text>, <Text color="#38bdf8"><code>DetectedTree</code></Text>, <Text color="#38bdf8"><code>TreeBlockSet</code></Text>, <Text color="#38bdf8"><code>TreeBlock</code></Text>, and <Text color="#38bdf8"><code>TreeLoot</code></Text> for working with configured trees, detected blocks, and loot.

## Bukkit events

Events are in <Text color="#38bdf8"><code>com.songoda.ultimatetimber.api.event</code></Text> and can be registered with Bukkit's plugin manager.

```java
import com.songoda.ultimatetimber.api.event.TreeFallEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class TimberListener implements Listener {

    @EventHandler
    public void onTreeFall(TreeFallEvent event) {
        int logCount = event.getDetectedTree().detectedTreeBlocks().getLogBlocks().size();
        if (logCount > 100) {
            event.setCancelled(true);
        }
    }
}
```

- <Text color="#38bdf8"><code>TreeFallEvent</code></Text> fires after a valid tree is detected and before the felling flow starts. It is cancellable. Cancelling stops the tree felling, though the normal block-break event can still remove the log the player clicked.
- <Text color="#38bdf8"><code>TreeFellEvent</code></Text> fires after the felling animation completes.
- <Text color="#38bdf8"><code>TreeDamageEvent</code></Text> fires when a falling tree block hits a player. It is cancellable. Use <Text color="#38bdf8"><code>getVictim()</code></Text> for the player and <Text color="#38bdf8"><code>getAttacker()</code></Text> for the falling block.

