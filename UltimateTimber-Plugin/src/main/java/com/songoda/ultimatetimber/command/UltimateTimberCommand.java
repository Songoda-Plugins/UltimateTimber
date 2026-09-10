package com.songoda.ultimatetimber.command;

import com.songoda.core.vortexcore.command.annotation.BaseCommand;
import com.songoda.core.vortexcore.command.annotation.Command;
import com.songoda.core.vortexcore.command.annotation.Param;
import com.songoda.core.vortexcore.command.annotation.Permission;
import com.songoda.core.vortexcore.command.annotation.Sender;
import com.songoda.core.vortexcore.command.annotation.SubCommand;
import com.songoda.core.vortexcore.text.MiniMessagePlaceholder;
import com.songoda.core.vortexcore.text.lang.Lang;
import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.api.manager.ChoppingManager;
import com.songoda.ultimatetimber.api.manager.TreeDefinitionManager;
import net.vortexdevelopment.vinject.annotation.Inject;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Main command handler for /ultimatetimber (/ut).
 */
@Command(value = "ultimatetimber", aliases = {"ut"})
public class UltimateTimberCommand {

    @Inject
    private ChoppingManager choppingManager;

    @Inject
    private TreeDefinitionManager treeDefinitionManager;

    @BaseCommand
    public void baseCommand(@Sender CommandSender sender) {
        Lang.send(sender, "Command.Help.Header");
        Lang.send(sender, "Command.Help.Toggle");
        Lang.send(sender, "Command.Help.Give");
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

    @SubCommand("reload")
    @Permission("ultimatetimber.reload")
    public void reload(@Sender CommandSender sender) {
        UltimateTimber.getInstance().runReloadHooks();
        Lang.send(sender, "Command.Reload.Reloaded");
    }
}
