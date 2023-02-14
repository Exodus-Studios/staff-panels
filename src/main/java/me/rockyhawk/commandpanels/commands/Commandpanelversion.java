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
                    //version command
                    String latestVersion = plugin.updater.getLatestVersion(false);
                    sender.sendMessage(plugin.tex.colour(plugin.tag));
                    sender.sendMessage(ChatColor.GREEN + "This Version   " + ChatColor.GRAY + plugin.getDescription().getVersion());
                    sender.sendMessage(ChatColor.GREEN + "Latest Version " + ChatColor.GRAY + latestVersion);
                    sender.sendMessage(ChatColor.GRAY + "-------------------");
                    sender.sendMessage(ChatColor.GREEN + "Developer " + ChatColor.GRAY + "RockyHawk");
                    sender.sendMessage(ChatColor.GREEN + "Command   " + ChatColor.GRAY + "/sp");
                } else {
                    sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
                }
            }else if(args.length == 1){
                if (sender.hasPermission("staffpanel.update")) {
                    if (args[0].equals("cancel")) {
                        plugin.updater.downloadVersionManually = null;
                        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.GREEN + "Will not download a new version on restart."));
                    } else {
                        plugin.updater.downloadVersionManually = args[0];
                        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.GREEN + "Downloading version " + ChatColor.GRAY + args[0] + ChatColor.GREEN + " upon server restart."));
                    }
                }else{
                    sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
                }
            }else{
                sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Usage: /spv [update:latest:cancel]"));
            }
            return true;
        }
        return true;
    }
}
