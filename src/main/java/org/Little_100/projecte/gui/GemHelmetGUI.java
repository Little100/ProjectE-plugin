package org.Little_100.projecte.gui;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.armor.GemHelmet;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class GemHelmetGUI {

    public static void open(Player player) {
        ProjectE plugin = ProjectE.getInstance();
        String title = plugin.getLanguageManager().get("item.gem_helmet.gui.title");
        Inventory gui = Bukkit.createInventory(null, 27, title);

        ItemStack glassPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glassPane.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 27; i++) {
            if (i != 12 && i != 14) {
                gui.setItem(i, glassPane);
            }
        }

        ItemStack nightVisionButton = new ItemStack(Material.ENDER_EYE);
        ItemMeta nightVisionMeta = nightVisionButton.getItemMeta();
        if (nightVisionMeta != null) {
            boolean isNightVisionOn = GemHelmet.isNightVisionActive(player);

            String statusKey = isNightVisionOn ? "item.gem_helmet.gui.night_vision_on" : "item.gem_helmet.gui.night_vision_off";
            nightVisionMeta.setDisplayName(plugin.getLanguageManager().get(statusKey));
            nightVisionMeta.setLore(Collections.singletonList(plugin.getLanguageManager().get("item.gem_helmet.gui.toggle_lore")));
            nightVisionButton.setItemMeta(nightVisionMeta);
        }
        gui.setItem(12, nightVisionButton);

        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(plugin.getLanguageManager().get("item.gem_helmet.gui.close"));
            closeButton.setItemMeta(closeMeta);
        }
        gui.setItem(14, closeButton);


        player.openInventory(gui);
    }
}