---
title: "PlaceholderAPI"
description: "PlaceholderAPI placeholders for individual player statistics and top 10 tree-felling leaderboards in UltimateTimber."
order: 4
hidden: false
---

# PlaceholderAPI

Install PlaceholderAPI to use UltimateTimber's player statistics in scoreboards, chat formats, menus, and other supported plugin features. UltimateTimber registers the <Text color="#38bdf8"><code>ultimatetimber</code></Text> expansion automatically when PlaceholderAPI is available. Statistics are enabled by default. Set <Text color="#38bdf8"><code>Statistics.Enabled</code></Text> to <Text color="#38bdf8"><code>false</code></Text> to stop tracking and turn off these placeholders.

## Placeholders

| Placeholder | Value |
| --- | --- |
| <Text color="#38bdf8"><code>%ultimatetimber_chopped_trees%</code></Text> | Number of successful tree fellings. |
| <Text color="#38bdf8"><code>%ultimatetimber_chopped_logs%</code></Text> | Total logs chopped by successful tree fellings. |
| <Text color="#38bdf8"><code>%ultimatetimber_chopped_leaves%</code></Text> | Total leaves chopped by successful tree fellings. |
| <Text color="#38bdf8"><code>%ultimatetimber_chopped_blocks%</code></Text> | Total logs and leaves chopped, as a number. |

Player totals are saved and updated after a successful tree-felling event. These placeholders show the player passed to PlaceholderAPI. They are not server-wide totals.

<Hint>Player-stat placeholders need a player context. Top-player placeholders can be used in server-wide text because they read from the cached leaderboard.</Hint>

## Top 10 leaderboard placeholders

Use a rank from <Text color="#38bdf8"><code>1</code></Text> to <Text color="#38bdf8"><code>10</code></Text>. Each statistic has its own leaderboard, sorted from highest to lowest. The player placeholder returns the name of the player at that rank for that statistic. If there is no player at a valid rank, numeric placeholders return <Text color="#38bdf8"><code>0</code></Text> and player placeholders return an empty string.

| Placeholder pattern | Value |
| --- | --- |
| <Text color="#38bdf8"><code>%ultimatetimber_top_&lt;rank&gt;_blocks%</code></Text> | Total logs and leaves chopped by the player at this rank. |
| <Text color="#38bdf8"><code>%ultimatetimber_top_&lt;rank&gt;_blocks_player%</code></Text> | Player name at this rank on the blocks leaderboard. |
| <Text color="#38bdf8"><code>%ultimatetimber_top_&lt;rank&gt;_trees%</code></Text> | Trees felled by the player at this rank. |
| <Text color="#38bdf8"><code>%ultimatetimber_top_&lt;rank&gt;_trees_player%</code></Text> | Player name at this rank on the trees leaderboard. |
| <Text color="#38bdf8"><code>%ultimatetimber_top_&lt;rank&gt;_logs%</code></Text> | Logs chopped by the player at this rank. |
| <Text color="#38bdf8"><code>%ultimatetimber_top_&lt;rank&gt;_logs_player%</code></Text> | Player name at this rank on the logs leaderboard. |
| <Text color="#38bdf8"><code>%ultimatetimber_top_&lt;rank&gt;_leaves%</code></Text> | Leaves chopped by the player at this rank. |
| <Text color="#38bdf8"><code>%ultimatetimber_top_&lt;rank&gt;_leaves_player%</code></Text> | Player name at this rank on the leaves leaderboard. |

For example, rank <Text color="#38bdf8"><code>1</code></Text> on the blocks leaderboard uses <Text color="#38bdf8"><code>%ultimatetimber_top_1_blocks_player%</code></Text> for the name and <Text color="#38bdf8"><code>%ultimatetimber_top_1_blocks%</code></Text> for the count. The top 10 lists are loaded with database-side sorting and kept in memory. They refresh every 60 seconds by default. Change <Text color="#38bdf8"><code>Statistics.Top Player Refresh</code></Text> to adjust the interval in seconds.

<Callout variant="success">Leaderboard placeholders use cached top 10 results, so requests do not run a database query each time. The cache refreshes on the configured interval and after successful tree felling.</Callout>

## Example

Add a placeholder to any plugin that supports PlaceholderAPI:

```text
Trees felled: %ultimatetimber_chopped_trees%
Logs chopped: %ultimatetimber_chopped_logs%
Leaves chopped: %ultimatetimber_chopped_leaves%
Blocks chopped: %ultimatetimber_chopped_blocks%
```

Example leaderboard lines:

```text
1. %ultimatetimber_top_1_blocks_player% - %ultimatetimber_top_1_blocks% blocks
2. %ultimatetimber_top_2_blocks_player% - %ultimatetimber_top_2_blocks% blocks
Top tree chopper: %ultimatetimber_top_1_trees_player% (%ultimatetimber_top_1_trees% trees)
```
