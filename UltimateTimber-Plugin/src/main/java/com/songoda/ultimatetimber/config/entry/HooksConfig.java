package com.songoda.ultimatetimber.config.entry;

import lombok.Getter;
import lombok.Setter;
import net.vortexdevelopment.vinject.annotation.yaml.Key;
import net.vortexdevelopment.vinject.annotation.yaml.YamlItem;

/**
 * Configuration DTO for external plugin integrations (mcMMO, Jobs).
 */
@Getter
@Setter
@YamlItem
public class HooksConfig {

    @Key("Apply Experience")
    private boolean applyExperience = true;

    @Key("Apply Extra Drops")
    private boolean applyExtraDrops = true;

    @Key("Require Ability Active")
    private boolean requireAbilityActive = false;
}
