package com.songoda.ultimatetimber.config.entry;

import lombok.Getter;
import lombok.Setter;
import net.vortexdevelopment.vinject.annotation.yaml.Key;
import net.vortexdevelopment.vinject.annotation.yaml.YamlItem;

/**
 * Configuration DTO for queued block replacements to mitigate lag.
 */
@Getter
@Setter
@YamlItem
public class QueuedBlockReplacementConfig {

    @Key("Mode")
    private String mode = "NEVER";

    @Key("Threshold")
    private int threshold = 20;

    @Key("Max Per Tick")
    private int maxPerTick = 1000;
}
