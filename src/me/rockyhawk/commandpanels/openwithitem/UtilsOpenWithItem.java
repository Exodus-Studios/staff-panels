package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class UtilsOpenWithItem implements Listener {
    CommandPanels plugin;
    public UtilsOpenWithItem(CommandPanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onAnyClick(InventoryClickEvent e) {
        //on a click when in any inventory
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        ItemStack clicked = e.getCurrentItem();
        Player p = (Player)e.getWhoClicked();
        //get the item clicked, then loop through panel names after action isn't nothing
        if(e.getAction() == InventoryAction.NOTHING){return;}
        if (e.getRawSlot() == -999) {return;}
        if (e.getSlotType() != InventoryType.SlotType.QUICKBAR) {return;}
        for(String[] panelName : plugin.panelNames){
            ConfigurationSection tempFile = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + plugin.panelFiles.get(Integer.parseInt(panelName[1])))).getConfigurationSection("panels." + panelName[0]);
            String tempName = panelName[0];
            if(tempFile.contains("open-with-item") && Objects.requireNonNull(e.getClickedInventory()).getType() == InventoryType.PLAYER) {
                try{
                    assert clicked != null;
                    if (clicked.getType() == new ItemStack(Objects.requireNonNull(plugin.itemCreate.makeItemFromConfig(Objects.requireNonNull(tempFile.getConfigurationSection("open-with-item")), p, false, true).getType()), 1).getType()) {
                        if ((plugin.papi( Objects.requireNonNull(clicked.getItemMeta()).getDisplayName()).equals(plugin.papi( Objects.requireNonNull(tempFile.getString("open-with-item.name")))))) {
                            //cancel the click item event
                            if (tempFile.contains("open-with-item.stationary")) {
                                if (e.getSlot() == Integer.parseInt(Objects.requireNonNull(tempFile.getString("open-with-item.stationary")))) {
                                    e.setCancelled(true);
                                    p.updateInventory();
                                    if(plugin.openPanels.hasPanelOpen(p.getName())) {
                                        plugin.openPanels.skipPanels.add(p.getName());
                                    }
                                    plugin.openVoids.openCommandPanel(p,p,tempName,tempFile,false);
                                    return;
                                }
                            }
                            return;
                        }
                    }
                }catch(NullPointerException cancel){
                    //do nothing skip item
                }
            }
        }
    }
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent e){
        //item right clicked only (not left because that causes issues when things are interacted with)
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        try {
            if(e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK || Objects.requireNonNull(e.getItem()).getType() == Material.AIR){
                return;
            }
            if (plugin.panelFiles == null) {
                return;
            }
        }catch(Exception b){
            return;
        }
        ItemStack clicked = e.getItem();
        Player p = e.getPlayer();
        ConfigurationSection tempFile;
        String tempName;
        for(String[] panelName  : plugin.panelNames){
            tempFile = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + plugin.panelFiles.get(Integer.parseInt(panelName[1])))).getConfigurationSection("panels." + panelName[0]);
            tempName = panelName[0];
            if(tempFile.contains("open-with-item")) {
                try{
                    assert clicked != null;
                    if (clicked.getType() == new ItemStack(Objects.requireNonNull(plugin.itemCreate.makeItemFromConfig(Objects.requireNonNull(tempFile.getConfigurationSection("open-with-item")), p, false, true).getType()), 1).getType()) {
                        if ((plugin.papi( Objects.requireNonNull(clicked.getItemMeta()).getDisplayName()).equals(plugin.papi(Objects.requireNonNull(tempFile.getString("open-with-item.name")))))) {
                            //cancel the click item event
                            if (tempFile.contains("open-with-item.stationary")) {
                                if (p.getInventory().getHeldItemSlot() != Integer.parseInt(Objects.requireNonNull(tempFile.getString("open-with-item.stationary")))) {
                                    return;
                                }
                            }
                            e.setCancelled(true);
                            p.updateInventory();
                            plugin.openVoids.openCommandPanel(p,p,tempName,tempFile,false);
                            return;
                        }
                    }
                }catch(NullPointerException cancel){
                    //do nothing skip item
                }
            }
        }
    }
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e){
        /*
        This world change event is added so if the player is using disabled-worlds
        and they change worlds, it will check if the player can have the item
        and if they can, it gives the item. This is because onRespawn doesn't
        give the item to the player in all the worlds that it could automatically.

        The player will of course need a plugin to split inventories between worlds
        for this to take effect. I don't want to delete the item on the wrong world
        because then it might overwrite one of their actual slots upon rejoining the enabled world.
         */
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        Player p = e.getPlayer();
        try {
            if (plugin.panelFiles == null) {
                return;
            }
        }catch(Exception b){
            return;
        }
        String tpanels; //tpanels is the temp to check through the files
        for(String fileName : plugin.panelFiles) { //will loop through all the files in folder
            YamlConfiguration temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + fileName));
            String key;
            tpanels = "";
            if(!plugin.checkPanels(temp)){
                continue;
            }
            for (Iterator var10 = Objects.requireNonNull(temp.getConfigurationSection("panels")).getKeys(false).iterator(); var10.hasNext(); tpanels = tpanels + key + " ") {
                key = (String) var10.next();
                if(temp.contains("panels." + key + ".disabled-worlds")){
                    List<String> disabledWorlds = temp.getStringList("panels." + key + ".disabled-worlds");
                    assert disabledWorlds != null;
                    if(disabledWorlds.contains(p.getWorld().getName())){
                        continue;
                    }
                }
                if (p.hasPermission("commandpanel.panel." + temp.getString("panels." + key + ".perm")) && temp.contains("panels." + key + ".open-with-item")) {
                    ItemStack s = plugin.itemCreate.makeItemFromConfig(Objects.requireNonNull(temp.getConfigurationSection("panels." + key + ".open-with-item")), p, false, true);
                    if(temp.contains("panels." + key + ".open-with-item.stationary")) {
                        if (0 <= Integer.parseInt(Objects.requireNonNull(temp.getString("panels." + key + ".open-with-item.stationary"))) && 8 >= Integer.parseInt(Objects.requireNonNull(temp.getString("panels." + key + ".open-with-item.stationary")))) {
                            p.getInventory().setItem(Integer.parseInt(Objects.requireNonNull(temp.getString("panels." + key + ".open-with-item.stationary"))), s);
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e){
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        Player p = e.getPlayer();
        try {
            if (plugin.panelFiles == null) {
                return;
            }
        }catch(Exception b){
            return;
        }
        String tpanels; //tpanels is the temp to check through the files
        for(String fileName : plugin.panelFiles) { //will loop through all the files in folder
            YamlConfiguration temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + fileName));
            String key;
            tpanels = "";
            if(!plugin.checkPanels(temp)){
                continue;
            }
            for (Iterator var10 = temp.getConfigurationSection("panels").getKeys(false).iterator(); var10.hasNext(); tpanels = tpanels + key + " ") {
                key = (String) var10.next();
                if(temp.contains("panels." + key + ".disabled-worlds")){
                    List<String> disabledWorlds = temp.getStringList("panels." + key + ".disabled-worlds");
                    assert disabledWorlds != null;
                    if(disabledWorlds.contains(p.getWorld().getName())){
                        continue;
                    }
                }
                if (p.hasPermission("commandpanel.panel." + temp.getString("panels." + key + ".perm")) && temp.contains("panels." + key + ".open-with-item")) {
                    ItemStack s = plugin.itemCreate.makeItemFromConfig(Objects.requireNonNull(temp.getConfigurationSection("panels." + key + ".open-with-item")), p, false, true);
                    if(temp.contains("panels." + key + ".open-with-item.stationary") && 0 <= Integer.parseInt(Objects.requireNonNull(temp.getString("panels." + key + ".open-with-item.stationary"))) && 8 >= Integer.parseInt(Objects.requireNonNull(temp.getString("panels." + key + ".open-with-item.stationary")))){
                        p.getInventory().setItem(Integer.parseInt(Objects.requireNonNull(temp.getString("panels." + key + ".open-with-item.stationary"))), s);
                    }
                }
            }
        }
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        Player p = e.getEntity();
        try {
            if (plugin.panelFiles == null) {
                return;
            }
        }catch(Exception b){
            return;
        }
        String tpanels; //tpanels is the temp to check through the files
        for(String fileName : plugin.panelFiles) { //will loop through all the files in folder
            YamlConfiguration temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + fileName));
            String key;
            tpanels = "";
            if(!plugin.checkPanels(temp)){
                continue;
            }
            for (Iterator var10 = Objects.requireNonNull(temp.getConfigurationSection("panels")).getKeys(false).iterator(); var10.hasNext(); tpanels = tpanels + key + " ") {
                key = (String) var10.next();
                if (p.hasPermission("commandpanel.panel." + temp.getString("panels." + key + ".perm")) && temp.contains("panels." + key + ".open-with-item")) {
                    if(temp.contains("panels." + key + ".open-with-item.stationary")){
                        ItemStack s = plugin.itemCreate.makeItemFromConfig(Objects.requireNonNull(temp.getConfigurationSection("panels." + key + ".open-with-item")), p, false, true);
                        e.getDrops().remove(s);
                    }
                }
            }
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        Player p = e.getPlayer();
        try {
            if (plugin.panelFiles == null) {
                return;
            }
        }catch(Exception b){
            return;
        }
        String tpanels; //tpanels is the temp to check through the files
        for(String fileName : plugin.panelFiles) { //will loop through all the files in folder
            YamlConfiguration temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + fileName));
            String key;
            tpanels = "";
            if(!plugin.checkPanels(temp)){
                continue;
            }
            for (Iterator var10 = Objects.requireNonNull(temp.getConfigurationSection("panels")).getKeys(false).iterator(); var10.hasNext(); tpanels = tpanels + key + " ") {
                key = (String) var10.next();
                if (p.hasPermission("commandpanel.panel." + temp.getString("panels." + key + ".perm")) && temp.contains("panels." + key + ".open-with-item")) {
                    if(temp.contains("panels." + key + ".disabled-worlds")){
                        List<String> disabledWorlds = temp.getStringList("panels." + key + ".disabled-worlds");
                        if(disabledWorlds.contains(p.getWorld().getName())){
                            continue;
                        }
                    }
                    ItemStack s = plugin.itemCreate.makeItemFromConfig(Objects.requireNonNull(temp.getConfigurationSection("panels." + key + ".open-with-item")), p, false, true);
                    if(temp.contains("panels." + key + ".open-with-item.stationary") && 0 <= Integer.parseInt(Objects.requireNonNull(temp.getString("panels." + key + ".open-with-item.stationary"))) && 8 >= Integer.parseInt(Objects.requireNonNull(temp.getString("panels." + key + ".open-with-item.stationary")))){
                        p.getInventory().setItem(Integer.parseInt(Objects.requireNonNull(temp.getString("panels." + key + ".open-with-item.stationary"))), s);
                    }
                }else{
                    //if the player has an item that they have no permission for, remove it
                    ItemStack s;
                    try {
                        s = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(temp.getString("panels." + key + ".open-with-item.material")))), 1);
                    }catch(Exception n){
                        continue;
                    }
                    plugin.setName(s, temp.getString("panels." + key + ".open-with-item.name"), temp.getStringList("panels." + key + ".open-with-item.lore"),p,true, true);
                    if(temp.contains("panels." + key + ".open-with-item.stationary") && 0 <= Integer.parseInt(Objects.requireNonNull(temp.getString("panels." + key + ".open-with-item.stationary"))) && 8 >= Integer.parseInt(Objects.requireNonNull(temp.getString("panels." + key + ".open-with-item.stationary")))){
                        try {
                            if (Objects.requireNonNull(p.getInventory().getItem(Integer.parseInt(Objects.requireNonNull(temp.getString("panels." + key + ".open-with-item.stationary"))))).isSimilar(s)) {
                                p.getInventory().setItem(Integer.parseInt(Objects.requireNonNull(temp.getString("panels." + key + ".open-with-item.stationary"))), null);
                            }
                        }catch(NullPointerException nex){
                            //skip as player has no item in slot
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e){
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        //if item dropped
        Player p = e.getPlayer();
        try {
            if (plugin.panelFiles == null) {
                return;
            }
        }catch(Exception b){
            return;
        }
        ItemStack clicked = e.getItemDrop().getItemStack();
        for(String[] panelName : plugin.panelNames){
            YamlConfiguration tempFile = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + plugin.panelFiles.get(Integer.parseInt(panelName[1]))));
            String tempName = panelName[0];
            if(tempFile.contains("panels." + tempName + ".open-with-item")) {
                try{
                    assert clicked != null;
                    if (clicked.getType() == new ItemStack(Objects.requireNonNull(plugin.itemCreate.makeItemFromConfig(Objects.requireNonNull(tempFile.getConfigurationSection("panels." + tempName + ".open-with-item")), p, false, true).getType()), 1).getType()) {
                        if ((plugin.papi( Objects.requireNonNull(clicked.getItemMeta()).getDisplayName()).equals(plugin.papi( Objects.requireNonNull(tempFile.getString("panels." + tempName + ".open-with-item.name")))))) {
                            //cancel the click item event
                            if (tempFile.contains("panels." + tempName + ".open-with-item.stationary")) {
                                e.setCancelled(true);
                                p.updateInventory();
                                return;
                            }
                            return;
                        }
                    }
                }catch(NullPointerException cancel){
                    //do nothing skip item
                }
            }
        }
    }
    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e){
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        //cancel everything if holding item (item frames eg)
        Player p = e.getPlayer();
        try {
            if (plugin.panelFiles == null) {
                return;
            }
        }catch(Exception b){
            return;
        }
        ItemStack clicked = e.getPlayer().getInventory().getItemInMainHand();
        String tempName;
        ConfigurationSection tempFile;
        for(String[] panelName : plugin.panelNames){
            tempName = panelName[0];
            tempFile = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + plugin.panelFiles.get(Integer.parseInt(panelName[1])))).getConfigurationSection("panels." + tempName);

            if (tempFile.contains("open-with-item")) {
                if (clicked.getType() != Material.AIR) {
                    //if loop has material first to stop 1.12.2 from spitting errors
                    //try and catch loop to stop errors with the same material type but different name
                    try {
                        if (clicked.getType() == new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(tempFile.getString("open-with-item.material")))), 1).getType()) {
                            if ((plugin.papi( Objects.requireNonNull(clicked.getItemMeta()).getDisplayName()).equals(plugin.papi( Objects.requireNonNull(tempFile.getString("open-with-item.name")))))) {
                                //cancel the click item event
                                if (tempFile.contains("open-with-item.stationary")) {
                                    if (p.getInventory().getHeldItemSlot() == Integer.parseInt(Objects.requireNonNull(tempFile.getString("open-with-item.stationary")))) {
                                        e.setCancelled(true);
                                        p.updateInventory();
                                        if(plugin.openPanels.hasPanelOpen(p.getName())) {
                                            plugin.openPanels.skipPanels.add(p.getName());
                                        }
                                        plugin.openVoids.openCommandPanel(p,p,tempName,tempFile,false);
                                    }
                                }
                                return;
                            }
                        }
                    } catch (NullPointerException | IllegalArgumentException n) {
                        plugin.debug(n);
                    }
                }
            }
        }
    }
}
