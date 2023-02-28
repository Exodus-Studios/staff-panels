package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;

public class Commandpanelversion implements CommandExecutor {
    CommandPanels plugin;
    public Commandpanelversion(CommandPanels pl) { this.plugin = pl; }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (label.equalsIgnoreCase("spv") || label.equalsIgnoreCase("staffpanelversion") || label.equalsIgnoreCase("spanelv")) {
            if(args.length == 0) {
                if (sender.hasPermission("staffpanel.version")) {
                    sender.sendMessage(plugin.tex.colour(plugin.tag));
                    sender.sendMessage(ChatColor.GREEN + "This Version   " + ChatColor.GRAY + plugin.getDescription().getVersion());
                    sender.sendMessage(ChatColor.GRAY + "-------------------");
                    sender.sendMessage(ChatColor.GREEN + "Developer " + ChatColor.GRAY + "RockyHawk");
                    sender.sendMessage(ChatColor.GREEN + "Command   " + ChatColor.GRAY + "/sp");
                } else {
                    sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
                }
            }

            return true;
        }
        return true;
    }
}
