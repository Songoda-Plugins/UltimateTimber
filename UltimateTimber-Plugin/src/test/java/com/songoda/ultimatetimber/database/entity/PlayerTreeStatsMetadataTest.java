package com.songoda.ultimatetimber.database.entity;

import net.vortexdevelopment.vinject.database.repository.EntityMetadata;
import net.vortexdevelopment.vinject.database.serializer.SerializerRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerTreeStatsMetadataTest {

    @Test
    void repositoryMetadataUsesTheMappedPrimaryKeyColumn() {
        EntityMetadata metadata = new EntityMetadata(PlayerTreeStats.class, new SerializerRegistry());

        assertEquals("player_uuid", metadata.getPrimaryKeyColumn());
        assertEquals("player_uuid", metadata.getColumnName("playerUuid"));
    }
}
