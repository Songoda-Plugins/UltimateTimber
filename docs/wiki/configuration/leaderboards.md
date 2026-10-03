---
title: "Leaderboards"
description: "Configure and spawn one persistent top player hologram for each UltimateTimber statistic."
order: 4
hidden: false
---

# Leaderboards

UltimateTimber can show the top players for blocks, trees, logs, and leaves in a floating hologram. It uses the same cached top 10 statistics as the [PlaceholderAPI leaderboards](../features/placeholderapi.md). The cache refreshes on its normal interval and after a successful tree fall.

<Callout variant="info">
  Player statistics need to be enabled in <Text color="#38bdf8"><code>config.yml</code></Text> under <Text color="#38bdf8"><code>Statistics.Enabled</code></Text>. This is enabled by default. If there are no saved stats yet, the board still shows ten rows with <Text color="#38bdf8"><code>No players yet</code></Text> and <Text color="#38bdf8"><code>0</code></Text> values.
</Callout>

## Configure titles and rows

Edit <Text color="#38bdf8"><code>plugins/UltimateTimber/leaderboard.yml</code></Text>. Each type has a <Text color="#38bdf8"><code>Title</code></Text> and up to ten independently formatted <Text color="#38bdf8"><code>Lines</code></Text>. The first line is rank 1, the second is rank 2, and so on. The number of entries in <Text color="#38bdf8"><code>Lines</code></Text> is the number of player rows the hologram shows. Add MiniMessage icons, colors, and formatting to each line.

```yaml
Types:
  blocks:
    Title: "<gold><bold>Top 10 Broken Blocks"
    Lines:
      - "<gold>🥇 <white><player> <dark_gray>- <yellow><value>"
      - "<gray>🥈 <white><player> <dark_gray>- <gold><value>"
      - "<#cd7f32>🥉 <white><player> <dark_gray>- <gold><value>"
      - "<gray>4. <white><player> <dark_gray>- <gold><value>"
      - "<gray>5. <white><player> <dark_gray>- <gold><value>"
      - "<gray>6. <white><player> <dark_gray>- <gold><value>"
      - "<gray>7. <white><player> <dark_gray>- <gold><value>"
      - "<gray>8. <white><player> <dark_gray>- <gold><value>"
      - "<gray>9. <white><player> <dark_gray>- <gold><value>"
      - "<gray>10. <white><player> <dark_gray>- <gold><value>"

  trees:
    Title: "<gold><bold>Top 10 Trees Felled"
    Lines:
      - "<gold>🥇 <white><player> <dark_gray>- <yellow><value>"
      - "<gray>🥈 <white><player> <dark_gray>- <gold><value>"
      - "<#cd7f32>🥉 <white><player> <dark_gray>- <gold><value>"
      - "<gray>4. <white><player> <dark_gray>- <gold><value>"
      - "<gray>5. <white><player> <dark_gray>- <gold><value>"
      - "<gray>6. <white><player> <dark_gray>- <gold><value>"
      - "<gray>7. <white><player> <dark_gray>- <gold><value>"
      - "<gray>8. <white><player> <dark_gray>- <gold><value>"
      - "<gray>9. <white><player> <dark_gray>- <gold><value>"
      - "<gray>10. <white><player> <dark_gray>- <gold><value>"
  logs:
    Title: "<gold><bold>Top 10 Logs Chopped"
    Lines:
      - "<gold>🥇 <white><player> <dark_gray>- <yellow><value>"
      - "<gray>🥈 <white><player> <dark_gray>- <gold><value>"
      - "<#cd7f32>🥉 <white><player> <dark_gray>- <gold><value>"
      - "<gray>4. <white><player> <dark_gray>- <gold><value>"
      - "<gray>5. <white><player> <dark_gray>- <gold><value>"
      - "<gray>6. <white><player> <dark_gray>- <gold><value>"
      - "<gray>7. <white><player> <dark_gray>- <gold><value>"
      - "<gray>8. <white><player> <dark_gray>- <gold><value>"
      - "<gray>9. <white><player> <dark_gray>- <gold><value>"
      - "<gray>10. <white><player> <dark_gray>- <gold><value>"
  leaves:
    Title: "<gold><bold>Top 10 Leaves Chopped"
    Lines:
      - "<gold>🥇 <white><player> <dark_gray>- <yellow><value>"
      - "<gray>🥈 <white><player> <dark_gray>- <gold><value>"
      - "<#cd7f32>🥉 <white><player> <dark_gray>- <gold><value>"
      - "<gray>4. <white><player> <dark_gray>- <gold><value>"
      - "<gray>5. <white><player> <dark_gray>- <gold><value>"
      - "<gray>6. <white><player> <dark_gray>- <gold><value>"
      - "<gray>7. <white><player> <dark_gray>- <gold><value>"
      - "<gray>8. <white><player> <dark_gray>- <gold><value>"
      - "<gray>9. <white><player> <dark_gray>- <gold><value>"
      - "<gray>10. <white><player> <dark_gray>- <gold><value>"

Placed: { }
```

Use as many entries as you want displayed, from one to ten. For example, three entries show only the top three. An empty <Text color="#38bdf8"><code>Lines</code></Text> list shows the title without player rows.

<Callout variant="success">The cache already contains the sorted top 10 for each stat. Boards use that shared cache and refresh after successful tree felling or on the configured interval.</Callout>

## Row placeholders

Each row supports these placeholders. They are replaced with values from that rank before MiniMessage formatting is applied.

| Placeholder | Value |
| --- | --- |
| <Text color="#38bdf8"><code>&lt;position&gt;</code></Text> or <Text color="#38bdf8"><code>&lt;rank&gt;</code></Text> | The row's position, from 1 through 10. |
| <Text color="#38bdf8"><code>&lt;player&gt;</code></Text> | Player name at that position. |
| <Text color="#38bdf8"><code>&lt;value&gt;</code></Text> | The count for this leaderboard type. |
| <Text color="#38bdf8"><code>&lt;blocks&gt;</code></Text> | This player's total chopped logs and leaves. |
| <Text color="#38bdf8"><code>&lt;trees&gt;</code></Text> | This player's successful tree fellings. |
| <Text color="#38bdf8"><code>&lt;logs&gt;</code></Text> | This player's chopped logs. |
| <Text color="#38bdf8"><code>&lt;leaves&gt;</code></Text> | This player's chopped leaves. |

For example, the third line can show an icon and include the player's tree count on a blocks leaderboard:

```yaml
- "<gold>🏆 <white><player> <dark_gray>- <yellow><value> blocks <gray>(<trees> trees)"
```

## Place and remove a board

Stand where the board should appear and spawn one of the supported types:

```text
/ut leaderboard spawn blocks
/ut leaderboard spawn trees
/ut leaderboard spawn logs
/ut leaderboard spawn leaves
```

Only one board of each type can be placed at a time. Each board's saved anchor is half a block above your feet. Its location is saved in <Text color="#38bdf8"><code>leaderboard.yml</code></Text>, and it returns after a restart or <Text color="#38bdf8"><code>/ut reload</code></Text>.

Remove a board by its type:

```text
/ut leaderboard remove blocks
```

Both commands require <Text color="#38bdf8"><code>ultimatetimber.leaderboard</code></Text>. If a board of that type already exists, remove it before spawning another. Existing saved UUID placements are migrated to one placement per type when the config loads. Remove and respawn an existing board to apply the new spawn height.

<Hint>Edit a type's <Text color="#38bdf8"><code>Title</code></Text> or <Text color="#38bdf8"><code>Lines</code></Text>, then run <Text color="#38bdf8"><code>/ut reload</code></Text>. Every placed board of that type updates to the new formatting.</Hint>
