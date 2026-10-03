package com.songoda.ultimatetimber.manager;

import com.songoda.core.vortexcore.compatibility.folia.SchedulerUtils;
import com.songoda.core.vortexcore.hooks.internal.ReloadHook;
import com.songoda.core.vortexcore.text.lang.Lang;
import com.songoda.core.vortexcore.vinject.annotation.RegisterReloadHook;
import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.api.manager.ChoppingManager;
import com.songoda.ultimatetimber.config.TimberConfig;
import net.vortexdevelopment.vinject.annotation.Inject;
import net.vortexdevelopment.vinject.annotation.component.Component;
import net.vortexdevelopment.vinject.annotation.lifecycle.PostConstruct;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing player chopping toggles and topple cooldowns.
 */
@Component
@RegisterReloadHook
public class ChoppingManagerImpl implements ChoppingManager, ReloadHook {

    private final Set<UUID> disabledPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Boolean> cooldownPlayers = new ConcurrentHashMap<>();
    @Inject
    private TimberConfig config;
    @Inject
    private UltimateTimber plugin;
    private volatile boolean useCooldown = false;
    private volatile int cooldownAmount = 5;

    @PostConstruct
    public void initialize() {
        onReload();
    }

    @Override
    public void onReload() {
        this.useCooldown = this.config.isPlayerTreeToppleCooldown();
        this.cooldownAmount = this.config.getPlayerTreeToppleCooldownLength();
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
        this.cooldownPlayers.put(uuid, false);

        SchedulerUtils.runTaskLater(plugin, () -> this.cooldownPlayers.remove(uuid), this.cooldownAmount * 20L);
    }

    @Override
    public boolean isInCooldown(@NotNull Player player) {
        boolean cooldown = this.useCooldown && this.cooldownPlayers.containsKey(player.getUniqueId());
        if (cooldown && !Boolean.TRUE.equals(this.cooldownPlayers.get(player.getUniqueId()))) {
            Lang.send(player, "Event.On Cooldown");
            this.cooldownPlayers.replace(player.getUniqueId(), true);
        }
        return cooldown;
    }
}
