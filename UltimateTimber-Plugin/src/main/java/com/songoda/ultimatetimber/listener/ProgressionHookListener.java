package com.songoda.ultimatetimber.listener;

import com.songoda.core.SongodaPlugin;
import com.songoda.core.hooks.auraskills.AuraSkillsHook;
import com.songoda.core.hooks.betonquest.BetonQuestHook;
import com.songoda.core.hooks.ecojobs.EcoJobsHook;
import com.songoda.core.hooks.ecoskills.EcoSkillsHook;
import com.songoda.core.hooks.mmocore.MMOCoreHook;
import com.songoda.core.vinject.annotation.RegisterListener;
import com.songoda.ultimatetimber.api.event.TreeFellEvent;
import com.songoda.ultimatetimber.config.TimberConfig;
import com.songoda.ultimatetimber.config.entry.HooksConfig;
import net.vortexdevelopment.vinject.annotation.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Applies configured optional progression rewards after a tree finishes falling.
 */
@RegisterListener
public final class ProgressionHookListener implements Listener {

    @Inject
    private TimberConfig config;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTreeFell(TreeFellEvent event) {
        if (this.config == null || this.config.getHooks() == null) {
            return;
        }

        HooksConfig hooks = this.config.getHooks();
        runAuraSkillsHook(event, hooks.getAuraSkills());
        runEcoSkillsHook(event, hooks.getEcoSkills());
        runEcoJobsHook(event, hooks.getEcoJobs());
        runMMOCoreHook(event, hooks.getMmoCore());
        runBetonQuestHook(event, hooks.getBetonQuest());
    }

    private void runAuraSkillsHook(TreeFellEvent event, HooksConfig.AuraSkillsConfig hookConfig) {
        if (hookConfig == null) {
            return;
        }

        SongodaPlugin.getInstance().getHookRegistry().runIfAvailable(
                "AureliumSkills",
                () -> hookConfig.isEnabled() && hookConfig.getSkill() != null && !hookConfig.getSkill().isBlank(),
                plugin -> AuraSkillsHook.addSkillExperience(event.getPlayer(), hookConfig.getSkill(), hookConfig.getExperiencePerTree())
        );
    }

    private void runEcoSkillsHook(TreeFellEvent event, HooksConfig.EcoSkillsConfig hookConfig) {
        if (hookConfig == null) {
            return;
        }

        SongodaPlugin.getInstance().getHookRegistry().runIfAvailable(
                "EcoSkills",
                () -> hookConfig.isEnabled() && hookConfig.getSkill() != null && !hookConfig.getSkill().isBlank(),
                plugin -> EcoSkillsHook.addSkillExperience(event.getPlayer(), hookConfig.getSkill(), hookConfig.getExperiencePerTree())
        );
    }

    private void runEcoJobsHook(TreeFellEvent event, HooksConfig.EcoJobsConfig hookConfig) {
        if (hookConfig == null) {
            return;
        }

        SongodaPlugin.getInstance().getHookRegistry().runIfAvailable(
                "EcoJobs",
                () -> hookConfig.isEnabled() && hookConfig.getJob() != null && !hookConfig.getJob().isBlank(),
                plugin -> EcoJobsHook.addJobExperience(event.getPlayer(), hookConfig.getJob(), hookConfig.getExperiencePerTree())
        );
    }

    private void runMMOCoreHook(TreeFellEvent event, HooksConfig.MMOCoreConfig hookConfig) {
        if (hookConfig == null) {
            return;
        }

        SongodaPlugin.getInstance().getHookRegistry().runIfAvailable(
                "MMOCore",
                hookConfig::isClassExperienceEnabled,
                plugin -> MMOCoreHook.addClassExperience(event.getPlayer(), hookConfig.getClassExperiencePerTree())
        );
        SongodaPlugin.getInstance().getHookRegistry().runIfAvailable(
                "MMOCore",
                () -> hookConfig.isProfessionExperienceEnabled()
                        && hookConfig.getProfession() != null
                        && !hookConfig.getProfession().isBlank(),
                plugin -> MMOCoreHook.addProfessionExperience(
                        event.getPlayer(),
                        hookConfig.getProfession(),
                        hookConfig.getProfessionExperiencePerTree()
                )
        );
    }

    private void runBetonQuestHook(TreeFellEvent event, HooksConfig.BetonQuestConfig hookConfig) {
        if (hookConfig == null) {
            return;
        }

        SongodaPlugin.getInstance().getHookRegistry().runIfAvailable(
                "BetonQuest",
                () -> hookConfig.isEnabled()
                        && hookConfig.getPackageName() != null
                        && !hookConfig.getPackageName().isBlank()
                        && hookConfig.getActionName() != null
                        && !hookConfig.getActionName().isBlank(),
                plugin -> BetonQuestHook.runAction(
                        plugin,
                        event.getPlayer(),
                        hookConfig.getPackageName(),
                        hookConfig.getActionName()
                )
        );
    }
}
