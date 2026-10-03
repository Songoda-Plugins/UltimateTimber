---
title: "Tree definitions"
description: "Configure tree species, log and leaf materials, saplings, soil, detection, and tools."
order: 2
hidden: false
---

# Tree definitions

The <Text color="#38bdf8"><code>Trees</code></Text> section is a map of tree keys to tree definitions. The bundled configuration includes <Text color="#38bdf8"><code>oak</code></Text>, <Text color="#38bdf8"><code>spruce</code></Text>, <Text color="#38bdf8"><code>birch</code></Text>, <Text color="#38bdf8"><code>jungle</code></Text>, <Text color="#38bdf8"><code>acacia</code></Text>, <Text color="#38bdf8"><code>dark_oak</code></Text>, <Text color="#38bdf8"><code>azalea</code></Text>, <Text color="#38bdf8"><code>cherry</code></Text>, <Text color="#38bdf8"><code>mangrove</code></Text>, and <Text color="#38bdf8"><code>pale_oak</code></Text>. You can add keys for custom tree types.

<Hint>
  Start by copying an existing tree entry, then change its key, materials, sapling, and detection distances.
</Hint>

```yaml
Trees:
  oak:
    Logs:
      - OAK_LOG
      - STRIPPED_OAK_LOG
      - OAK_WOOD
      - STRIPPED_OAK_WOOD
    Leaves:
      - OAK_LEAVES
      - VINE
    Sapling: OAK_SAPLING
    Plantable Soil: [] # Empty means use the global soil list.
    Max Log Distance From Trunk: 6.0 # Maximum branch distance from the trunk.
    Max Leaf Distance From Log: 6 # Maximum leaf distance from a matching log.
    Search For Leaves Diagonally: false
    Drop Original Log: true
    Drop Original Leaf: false
    Required Tools: []
    Required Axe: false
```

## Tree fields

- <Text color="#38bdf8"><code>Logs</code></Text> lists materials that count as this tree's logs.
- <Text color="#38bdf8"><code>Leaves</code></Text> lists materials that count as its leaves.
- <Text color="#38bdf8"><code>Sapling</code></Text> is the material to plant when sapling replanting succeeds.
- <Text color="#38bdf8"><code>Plantable Soil</code></Text> adds soil materials accepted for this definition. Global soil is also accepted.
- <Text color="#38bdf8"><code>Max Log Distance From Trunk</code></Text> (<Text color="#34d399"><code>6.0</code></Text>) sets the branch-log distance limit.
- <Text color="#38bdf8"><code>Max Leaf Distance From Log</code></Text> (<Text color="#34d399"><code>6</code></Text>) sets the leaf distance limit.
- <Text color="#38bdf8"><code>Search For Leaves Diagonally</code></Text> (<Text color="#34d399"><code>false</code></Text>) allows diagonal leaf connections.
- <Text color="#38bdf8"><code>Drop Original Log</code></Text> (<Text color="#34d399"><code>true</code></Text>) and <Text color="#38bdf8"><code>Drop Original Leaf</code></Text> (<Text color="#34d399"><code>false</code></Text>) control whether matching blocks keep their normal drops.
- <Text color="#38bdf8"><code>Required Tools</code></Text> adds tool materials accepted for this tree.
- <Text color="#38bdf8"><code>Required Axe</code></Text> (<Text color="#34d399"><code>false</code></Text>) requires the custom top-level <Text color="#38bdf8"><code>Required Axe</code></Text> item for this tree.
- <Text color="#38bdf8"><code>Log Loot</code></Text>, <Text color="#38bdf8"><code>Leaf Loot</code></Text>, and <Text color="#38bdf8"><code>Entire Tree Loot</code></Text> define additional drops or console commands for this tree.

The values shown in the field list are class defaults. The bundled <Text color="#38bdf8"><code>config.yml</code></Text> may set a different value for a particular tree, so use that file as the source for the shipped species settings.
