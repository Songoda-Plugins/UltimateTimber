---
title: "Performance settings"
description: "Configure queued block replacement and find integration compatibility details."
order: 5
hidden: false
---

# Performance settings

Large trees can replace many blocks at once. <Text color="#38bdf8"><code>Queued Block Replacement</code></Text> can spread those replacements across ticks instead of processing them all immediately.

<Hint>
  Leave the queue mode at <Text color="#8b5cf6"><code>NEVER</code></Text> unless you want block replacement spread over time. <Text color="#8b5cf6"><code>DYNAMIC</code></Text> switches to the queue at the configured player threshold.
</Hint>

```yaml
Queued Block Replacement:
  Mode: NEVER # Shipped default. Change to ALWAYS or DYNAMIC to queue replacements.
  Threshold: 20 # DYNAMIC mode starts queueing at this many online players.
  Max Per Tick: 1000 # Maximum queued block replacements handled per tick.
```

- <Text color="#38bdf8"><code>Mode</code></Text> is <Text color="#8b5cf6"><code>NEVER</code></Text>, <Text color="#8b5cf6"><code>ALWAYS</code></Text>, or <Text color="#8b5cf6"><code>DYNAMIC</code></Text>. Unknown values fall back to <Text color="#8b5cf6"><code>NEVER</code></Text>.
- <Text color="#38bdf8"><code>Threshold</code></Text> is the online-player count at which <Text color="#8b5cf6"><code>DYNAMIC</code></Text> mode starts queueing. The default is 20.
- <Text color="#38bdf8"><code>Max Per Tick</code></Text> is the replacement limit processed per tick. The default is 1000.

For supported plugin integrations and the current <Text color="#38bdf8"><code>Hooks</code></Text> status, see [Compatibility](../features/compatibility.md).
