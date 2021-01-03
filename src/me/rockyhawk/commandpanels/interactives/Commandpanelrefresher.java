package me.rockyhawk.commandpanels.interactives;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.ioclasses.NBTEditor;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class Commandpanelrefresher implements Listener {
    CommandPanels plugin;
    public Commandpanelrefresher(CommandPanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e){ //Handles when Players open inventory
        //I have to convert HumanEntity to a player
        if (plugin.config.contains("config.refresh-panels")) {
            if (Objects.requireNonNull(plugin.config.getString("config.refresh-panels")).trim().equalsIgnoreCase("false")) {
                return;
            }
        }
        Player p = (Player) e.getPlayer();

        if(!plugin.openPanels.hasPanelOpen(p.getName())){
            return;
        }
        ConfigurationSection cf = plugin.openPanels.getOpenPanel(p.getName()); //this is the panel cf section
        String panelName = plugin.openPanels.getOpenPanelName(p.getName()); //get panel name

        //remove sound-on-open on 1.8 for those who do not read the wiki ;)
        if(cf.contains("sound-on-open")){
            if(Bukkit.getVersion().contains("1.8")){
                cf.set("sound-on-open", null);
            }
        }

        if(cf.contains("panelType")) {
            if (cf.getStringList("panelType").contains("static")) {
                //do not update temporary panels, only default panels
                return;
            }
        }

        final ConfigurationSection cfFinal = cf;
        ItemStack[] panelItemList = plugin.createGUI.openGui(null, p, cf,2, -1).getContents();
        ItemStack[] playerItemList = plugin.legacy.getStorageContents(p.getInventory());
        new BukkitRunnable(){
            int c = 0;
            int animatecount = 0;
            @Override
            public void run() {
                int animatevalue = -1;
                if(cfFinal.contains("animatevalue")){
                    animatevalue = cfFinal.getInt("animatevalue");
                }
                //counter counts to refresh delay (in seconds) then restarts
                if(c < Double.parseDouble(Objects.requireNonNull(plugin.config.getString("config.refresh-delay")).trim())){
                    c+=1;
                }else{
                    c=0;
                }
                //refresh here
                if(plugin.openPanels.hasPanelOpen(p.getName(),panelName)){
                    if(c == 0) {
                        //animation counter
                        if(animatevalue != -1) {
                            if (animatecount < animatevalue) {
                                animatecount += 1;
                            } else {
                                animatecount = 0;
                            }
                        }
                        try {
                            plugin.createGUI.openGui(null, p, cfFinal, 0,animatecount);
                        } catch (Exception e) {
                            //error opening gui
                            p.closeInventory();
                            plugin.openPanels.closePanelForLoader(p.getName(),panelName);
                            this.cancel();
                        }
                    }
                }else{
                    if(Objects.requireNonNull(plugin.config.getString("config.stop-sound")).trim().equalsIgnoreCase("true")){
                        try {
                            p.stopSound(Sound.valueOf(Objects.requireNonNull(cfFinal.getString("sound-on-open")).toUpperCase()));
                        }catch(Exception sou){
                            //skip
                        }
                    }
                    c = 0;
                    this.cancel();
                }
            }
        }.runTaskTimer(this.plugin, 5, 5); //20 ticks == 1 second (5 ticks = 0.25 of a second)

    }
}
