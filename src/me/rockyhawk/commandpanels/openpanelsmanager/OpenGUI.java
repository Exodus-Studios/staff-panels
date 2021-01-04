package me.rockyhawk.commandpanels.openpanelsmanager;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.ioclasses.NBTEditor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Iterator;
import java.util.Objects;

public class OpenGUI {
    CommandPanels plugin;
    public OpenGUI(CommandPanels pl) {
        this.plugin = pl;
    }

    @SuppressWarnings("deprecation")
    public Inventory openGui(String panels, Player p, ConfigurationSection pconfig, int onOpen, int animateValue) {
        if (Integer.parseInt(Objects.requireNonNull(pconfig.getString("rows"))) < 7 && Integer.parseInt(Objects.requireNonNull(pconfig.getString("rows"))) > 0) {
            Inventory i;
            if (onOpen != 3) {
                //use the regular inventory
                i = Bukkit.createInventory(null, Integer.parseInt(Objects.requireNonNull(pconfig.getString("rows"))) * 9, plugin.papi(p, Objects.requireNonNull(pconfig.getString("title"))));
            } else {
                //this means it is the Editor window
                i = Bukkit.createInventory(null, Integer.parseInt(Objects.requireNonNull(pconfig.getString("rows"))) * 9, "Editing Panel: " + panels);
            }
            String item = "";

            String key;
            for (Iterator var6 = Objects.requireNonNull(pconfig.getConfigurationSection("item")).getKeys(false).iterator(); var6.hasNext(); item = item + key + " ") {
                key = (String) var6.next();
            }

            item = item.trim();
            int c;
            for (c = 0; item.split("\\s").length - 1 >= c; ++c) {
                if(item.equals("")){
                    //skip putting any items in the inventory if it is empty
                    break;
                }
                String section = "";
                //onOpen needs to not be 3 so the editor won't include hasperm and hasvalue, etc items
                if (onOpen != 3) {
                    section = plugin.itemCreate.hasSection(pconfig.getConfigurationSection("item." + Integer.parseInt(item.split("\\s")[c])), p);
                    //This section is for animations below here: VISUAL ONLY

                    //check for if there is animations inside the items section
                    if (pconfig.contains("item." + item.split("\\s")[c] + section + ".animate" + animateValue)) {
                        //check for if it contains the animate that has the animvatevalue
                        if (pconfig.contains("item." + item.split("\\s")[c] + section + ".animate" + animateValue)) {
                            section = section + ".animate" + animateValue;
                        }
                    }
                }
                ItemStack s = plugin.itemCreate.makeItemFromConfig(Objects.requireNonNull(pconfig.getConfigurationSection("item." + item.split("\\s")[c] + section)), p, onOpen != 3, onOpen != 3, true);

                //This is for CUSTOM ITEMS
                if(pconfig.contains("item." + item.split("\\s")[c] + section + ".itemType")) {
                    //this is for contents in the itemType section
                    if (pconfig.getStringList("item." + item.split("\\s")[c] + section + ".itemType").contains("placeable") && onOpen == 0) {
                        //keep item the same, onOpen == 0 meaning panel is refreshing
                        i.setItem(Integer.parseInt(item.split("\\s")[c]), p.getOpenInventory().getItem(Integer.parseInt(item.split("\\s")[c])));
                        continue;
                    }
                }

                try {
                    //place item into the GUI
                    i.setItem(Integer.parseInt(item.split("\\s")[c]), s);
                    //only place duplicate items in without the editor mode. These are merely visual and will not carry over commands
                    if(pconfig.contains("item." + item.split("\\s")[c] + section + ".duplicate") && onOpen != 3) {
                        try {
                            String[] duplicateItems = pconfig.getString("item." + item.split("\\s")[c] + section + ".duplicate").split(",");
                            for (String tempDupe : duplicateItems) {
                                if (tempDupe.contains("-")) {
                                    //if there is multiple dupe items, convert numbers to ints
                                    int[] bothNumbers = new int[]{Integer.parseInt(tempDupe.split("-")[0]), Integer.parseInt(tempDupe.split("-")[1])};
                                    for(int n = bothNumbers[0]; n <= bothNumbers[1]; n++){
                                        try{
                                            if(!pconfig.contains("item." + n)){
                                                i.setItem(n, s);
                                            }
                                        }catch(NullPointerException ignore){
                                            i.setItem(n, s);
                                        }
                                    }
                                } else {
                                    //if there is only one dupe item
                                    try{
                                        if(!pconfig.contains("item." + Integer.parseInt(tempDupe))){
                                            i.setItem(Integer.parseInt(tempDupe), s);
                                        }
                                    }catch(NullPointerException ignore){
                                        i.setItem(Integer.parseInt(tempDupe), s);
                                    }
                                }
                            }
                        }catch(NullPointerException nullp){
                            plugin.debug(nullp);
                            p.closeInventory();
                            plugin.openPanels.closePanelForLoader(p.getName(),panels);
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException var24) {
                    plugin.debug(var24);
                    if (plugin.debug) {
                        p.sendMessage(plugin.papi(plugin.tag + plugin.config.getString("config.format.error") + " item: One of the items does not fit in the Panel!"));
                        p.closeInventory();
                        plugin.openPanels.closePanelForLoader(p.getName(),panels);
                    }
                }
            }
            if (pconfig.contains("empty") && !Objects.equals(pconfig.getString("empty"), "AIR")) {
                for (c = 0; Integer.parseInt(Objects.requireNonNull(pconfig.getString("rows"))) * 9 - 1 >= c; ++c) {
                    boolean found = false;
                    if(!item.equals("")) {
                        for (int f = 0; item.split("\\s").length - 1 >= f; ++f) {
                            if (Integer.parseInt(item.split("\\s")[f]) == c) {
                                //check to ensure slot is empty
                                if(i.getItem(f) == null){
                                    found = true;
                                }
                            }
                        }
                    }
                    if (!found) {
                        ItemStack empty;
                        try {
                            short id = 0;
                            if(pconfig.contains("emptyID")){
                                id = Short.parseShort(pconfig.getString("emptyID"));
                            }
                            empty = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(pconfig.getString("empty")).toUpperCase())), 1,id);
                            empty = NBTEditor.set(empty,"CommandPanels","plugin");
                            if (empty.getType() == Material.AIR) {
                                continue;
                            }
                        } catch (IllegalArgumentException | NullPointerException var26) {
                            plugin.debug(var26);
                            p.sendMessage(plugin.papi(plugin.tag + plugin.config.getString("config.format.error") + " empty: " + pconfig.getString("empty")));
                            p.closeInventory();
                            plugin.openPanels.closePanelForLoader(p.getName(),panels);
                            return null;
                        }

                        ItemMeta renamedMeta = empty.getItemMeta();
                        assert renamedMeta != null;
                        renamedMeta.setDisplayName(" ");
                        empty.setItemMeta(renamedMeta);
                        if (onOpen != 3) {
                            //only place empty items if not editing
                            if(i.getItem(c) == null && !pconfig.contains("item." + c)) {
                                i.setItem(c, empty);
                            }
                        }
                    }
                }
            }
            if (onOpen == 1 || onOpen == 3) {
                //onOpen 1 is default and 3 is for the editor
                p.openInventory(i);
            } else if (onOpen == 0) {
                //onOpen 0 will just refresh the panel
                plugin.legacy.setStorageContents(p,plugin.legacy.getStorageContents(i));
            } else if (onOpen == 2) {
                //will return the inventory, not opening it at all
                return i;
            }
            return i;
        } else {
            p.sendMessage(plugin.papi(plugin.tag + plugin.config.getString("config.format.error") + " rows: " + pconfig.getString("rows")));
            p.closeInventory();
            plugin.openPanels.closePanelForLoader(p.getName(),panels);
            return null;
        }
    }
}
