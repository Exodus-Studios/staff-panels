package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;


public class Commandpanelslist implements CommandExecutor {
    CommandPanels plugin;
    public Commandpanelslist(CommandPanels pl) { this.plugin = pl; }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("spl") || label.equalsIgnoreCase("staffpanellist") || label.equalsIgnoreCase("spanell")) {
            if (sender.hasPermission("staffpanel.list")) {
                //command /spl
                //check to make sure the panels isn't empty
                try {
                    if (plugin.panelList == null) {
                        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "No panels found!"));
                        return true;
                    }
                }catch(Exception b){
                    sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "No panels found!"));
                    return true;
                }

                ArrayList<Panel> panels = new ArrayList<>(plugin.panelList);
                int page = 1;
                int skip = 0;
                if(args.length == 1){
                    try {
                        page = Integer.parseInt(args[0]);
                        skip = page*8-8;
                    }catch (Exception e){
                        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Inaccessible Page"));
                    }
                }
                sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.DARK_AQUA + "Panels: (Page " + page + ")"));
                for (int f = skip; panels.size() > f && skip+8 > f; f++) {
                    sender.sendMessage(ChatColor.DARK_GREEN + panels.get(f).getFile().getAbsolutePath().replace(plugin.panelsf.getAbsolutePath(),"") + ChatColor.GREEN + " " + panels.get(f).getName());
                    if(panels.size()-1 == f){
                        return true;
                    }
                }
                sender.sendMessage(ChatColor.AQUA + "Type /spl " + (page+1) + " to read next page");
            }else{
                sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
            }
            return true;
        }
        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Usage: /spl"));
        return true;
    }
}