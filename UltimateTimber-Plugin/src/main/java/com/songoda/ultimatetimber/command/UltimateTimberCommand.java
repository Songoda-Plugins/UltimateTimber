package com.songoda.ultimatetimber.command;

import com.songoda.core.vortexcore.command.annotation.BaseCommand;
import com.songoda.core.vortexcore.command.annotation.Command;
import com.songoda.core.vortexcore.command.annotation.Current;
import com.songoda.core.vortexcore.command.annotation.Param;
import com.songoda.core.vortexcore.command.annotation.Permission;
import com.songoda.core.vortexcore.command.annotation.Sender;
import com.songoda.core.vortexcore.command.annotation.SubCommand;
import com.songoda.core.vortexcore.command.annotation.TabComplete;
import com.songoda.core.vortexcore.text.MiniMessagePlaceholder;
import com.songoda.core.vortexcore.text.lang.Lang;
import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.api.manager.ChoppingManager;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import com.songoda.ultimatetimber.hologram.LeaderboardHologramService;
import net.vortexdevelopment.vinject.annotation.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;

/**
 * Main command handler for /ultimatetimber (/ut).
 */
@Command(value = "ultimatetimber", aliases = {"ut"})
public class UltimateTimberCommand {

    @Inject
    private ChoppingManager choppingManager;

    @Inject
    private TreeDefinitionManager treeDefinitionManager;

    @Inject
    private UltimateTimber plugin;

    @Inject
    private LeaderboardHologramService leaderboardHologramService;

    @BaseCommand
    public void baseCommand(@Sender CommandSender sender) {
        Lang.send(sender, "Command.Help.Header");
        Lang.send(sender, "Command.Help.Toggle");
        Lang.send(sender, "Command.Help.Give");
        Lang.send(sender, "Command.Help.Leaderboard");
        Lang.send(sender, "Command.Help.Reload");
    }

    @SubCommand("toggle")
    @Permission("ultimatetimber.toggle")
    public void toggle(@Sender Player player) {
        if (this.choppingManager.togglePlayer(player)) {
            Lang.send(player, "Command.Toggle.Enabled");
        } else {
            Lang.send(player, "Command.Toggle.Disabled");
        }
    }

    @SubCommand("give {player}")
    @Permission("ultimatetimber.give")
    public void give(@Sender CommandSender sender, @Param("player") Player target) {
        ItemStack axe = this.treeDefinitionManager.getRequiredAxe();
        if (axe == null) {
            Lang.send(sender, "Command.Give.No Axe");
            return;
        }

        target.getInventory().addItem(axe);
        Lang.send(sender, "Command.Give.Given", new MiniMessagePlaceholder("player", target.getName()));
    }

    @TabComplete(param = "player")
    public List<String> completePlayers(@Current String current) {
        String prefix = current.toLowerCase(Locale.ROOT);
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                .toList();
    }

    @SubCommand("leaderboard spawn {type}")
    @Permission("ultimatetimber.leaderboard")
    public void spawnLeaderboard(@Sender Player player, @Param("type") String type) {
        if (!this.leaderboardHologramService.isEnabled()) {
            Lang.send(player, "Command.Leaderboard.Disabled");
            return;
        }

        String id = this.leaderboardHologramService.spawn(player, type);
        if (id == null) {
            if (this.leaderboardHologramService.getPlacedTypes().contains(type.toLowerCase(Locale.ROOT))) {
                Lang.send(player, "Command.Leaderboard.Already Exists", new MiniMessagePlaceholder("type", type.toLowerCase(Locale.ROOT)));
            } else {
                Lang.send(player, "Command.Leaderboard.Invalid Type");
            }
            return;
        }

        Lang.send(player, "Command.Leaderboard.Spawned",
                new MiniMessagePlaceholder("type", id),
                new MiniMessagePlaceholder("id", id));
    }

    @TabComplete(param = "type")
    public List<String> completeLeaderboardTypes(@Current String current) {
        String prefix = current.toLowerCase(Locale.ROOT);
        return this.leaderboardHologramService.getTypes().stream()
                .filter(type -> type.startsWith(prefix))
                .toList();
    }

    @SubCommand("leaderboard remove {type}")
    @Permission("ultimatetimber.leaderboard")
    public void removeLeaderboard(@Sender Player player, @Param("type") String type) {
        if (!this.leaderboardHologramService.remove(type)) {
            Lang.send(player, "Command.Leaderboard.Not Found");
            return;
        }

        Lang.send(player, "Command.Leaderboard.Removed", new MiniMessagePlaceholder("type", type.toLowerCase(Locale.ROOT)));
    }

    @SubCommand("reload")
    @Permission("ultimatetimber.reload")
    public void reload(@Sender CommandSender sender) {
        this.plugin.runReloadHooks();
        Lang.send(sender, "Command.Reload.Reloaded");
    }
}
