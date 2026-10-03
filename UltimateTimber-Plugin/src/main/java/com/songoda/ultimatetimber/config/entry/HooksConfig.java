package com.songoda.ultimatetimber.config.entry;

import lombok.Getter;
import lombok.Setter;
import net.vortexdevelopment.vinject.annotation.yaml.Key;
import net.vortexdevelopment.vinject.annotation.yaml.YamlItem;

/**
 * Configuration for optional plugin integrations.
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

    @Key("AuraSkills")
    private AuraSkillsConfig auraSkills = new AuraSkillsConfig();

    @Key("EcoSkills")
    private EcoSkillsConfig ecoSkills = new EcoSkillsConfig();

    @Key("EcoJobs")
    private EcoJobsConfig ecoJobs = new EcoJobsConfig();

    @Key("MMOCore")
    private MMOCoreConfig mmoCore = new MMOCoreConfig();

    @Key("BetonQuest")
    private BetonQuestConfig betonQuest = new BetonQuestConfig();

    @Getter
    @Setter
    @YamlItem
    public static class AuraSkillsConfig {
        @Key("Enabled")
        private boolean enabled = false;

        @Key("Skill")
        private String skill = "";

        @Key("Experience Per Tree")
        private double experiencePerTree = 1.0;
    }

    @Getter
    @Setter
    @YamlItem
    public static class EcoSkillsConfig {
        @Key("Enabled")
        private boolean enabled = false;

        @Key("Skill")
        private String skill = "";

        @Key("Experience Per Tree")
        private double experiencePerTree = 1.0;
    }

    @Getter
    @Setter
    @YamlItem
    public static class EcoJobsConfig {
        @Key("Enabled")
        private boolean enabled = false;

        @Key("Job")
        private String job = "";

        @Key("Experience Per Tree")
        private double experiencePerTree = 1.0;
    }

    @Getter
    @Setter
    @YamlItem
    public static class MMOCoreConfig {
        @Key("Class Experience Enabled")
        private boolean classExperienceEnabled = false;

        @Key("Class Experience Per Tree")
        private double classExperiencePerTree = 1.0;

        @Key("Profession Experience Enabled")
        private boolean professionExperienceEnabled = false;

        @Key("Profession")
        private String profession = "";

        @Key("Profession Experience Per Tree")
        private double professionExperiencePerTree = 1.0;
    }

    @Getter
    @Setter
    @YamlItem
    public static class BetonQuestConfig {
        @Key("Enabled")
        private boolean enabled = false;

        @Key("Package")
        private String packageName = "";

        @Key("Action")
        private String actionName = "";
    }
}
