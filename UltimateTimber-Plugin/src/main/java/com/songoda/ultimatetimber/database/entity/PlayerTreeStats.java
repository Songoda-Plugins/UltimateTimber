package com.songoda.ultimatetimber.database.entity;

import com.songoda.core.vortexcore.vinject.database.cache.DefaultCacheKeys;
import lombok.Getter;
import lombok.Setter;
import net.vortexdevelopment.vinject.annotation.database.AutoLoad;
import net.vortexdevelopment.vinject.annotation.database.Column;
import net.vortexdevelopment.vinject.annotation.database.Entity;
import net.vortexdevelopment.vinject.annotation.database.Id;

import java.util.UUID;

/**
 * Persistent tree-felling totals for one player.
 */
@Entity(table = "player_tree_stats")
@Getter
@Setter
public final class PlayerTreeStats {

    @Id
    @Column(name = "player_uuid", nullable = false)
    @AutoLoad(DefaultCacheKeys.PLAYER_UUID)
    private UUID playerUuid;

    @Column(name = "player_name", length = 16)
    private String playerName;

    @Column(name = "triggers", nullable = false)
    private Long triggers = 0L;

    @Column(name = "logs_chopped", nullable = false)
    private Long logsChopped = 0L;

    @Column(name = "leaves_chopped", nullable = false)
    private Long leavesChopped = 0L;

    public PlayerTreeStats() {
    }

    public PlayerTreeStats(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }
}
