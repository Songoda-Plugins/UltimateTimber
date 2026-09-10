package com.songoda.ultimatetimber.manager;

import com.songoda.core.vortexcore.hooks.internal.ReloadHook;
import com.songoda.core.vortexcore.text.lang.Lang;
import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.api.manager.ChoppingManager;
import com.songoda.ultimatetimber.config.TimberConfig;
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.annotation.component.Component;
import com.songoda.core.vortexcore.vinject.annotation.RegisterReloadHook;
import net.vortexdevelopment.vinject.annotation.lifecycle.OnLoad;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Service managing player chopping toggles and topple cooldowns.
 */
@Component
@RegisterReloadHook
public class ChoppingManagerImpl implements ChoppingManager, ReloadHook {

    @Inject
    private TimberConfig config;

    private final Set<UUID> disabledPlayers = new HashSet<>();
    private final Map<UUID, Boolean> cooldownedPlayers = new HashMap<>();
    private boolean useCooldown = false;
    private int cooldownAmount = 5;

    @OnLoad
    public void onLoad() {
        onReload();
    }

    @Override
    public void onReload() {
        if (this.config != null) {
            this.useCooldown = this.config.isPlayerTreeToppleCooldown();
            this.cooldownAmount = this.config.getPlayerTreeToppleCooldownLength();
        }
    }

    @Override
    public boolean togglePlayer(@NotNull Player player) {
        if (this.disabledPlayers.contains(player.getUniqueId())) {
            this.disabledPlayers.remove(player.getUniqueId());
            return true;
        } else {
            this.disabledPlayers.add(player.getUniqueId());
            return false;
        }
    }

    @Override
    public boolean isChopping(@NotNull Player player) {
        return !this.disabledPlayers.contains(player.getUniqueId());
    }

    @Override
    public void cooldownPlayer(@NotNull Player player) {
        if (!this.useCooldown || player.hasPermission("ultimatetimber.bypasscooldown")) {
            return;
        }

        UUID uuid = player.getUniqueId();
        this.cooldownedPlayers.put(uuid, false);

        UltimateTimber plugin = UltimateTimber.getInstance();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> this.cooldownedPlayers.remove(uuid), this.cooldownAmount * 20L);
    }

    @Override
    public boolean isInCooldown(@NotNull Player player) {
        boolean cooldowned = this.useCooldown && this.cooldownedPlayers.containsKey(player.getUniqueId());
        if (cooldowned && !Boolean.TRUE.equals(this.cooldownedPlayers.get(player.getUniqueId()))) {
            Lang.send(player, "Event.On Cooldown");
            this.cooldownedPlayers.replace(player.getUniqueId(), true);
        }
        return cooldowned;
    }
}
