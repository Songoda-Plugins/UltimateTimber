package com.songoda.ultimatetimber.database.repository;

import com.songoda.core.vortexcore.vinject.database.PreloadedCrudRepository;
import com.songoda.ultimatetimber.database.entity.PlayerTreeStats;
import net.vortexdevelopment.vinject.annotation.component.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Cached repository for per-player tree-felling totals.
 */
@Repository
public interface PlayerTreeStatsRepository extends PreloadedCrudRepository<PlayerTreeStats, UUID> {

    List<PlayerTreeStats> findTop10ByOrderByTriggersDesc();

    List<PlayerTreeStats> findTop10ByOrderByLogsChoppedDesc();

    List<PlayerTreeStats> findTop10ByOrderByLeavesChoppedDesc();
}
