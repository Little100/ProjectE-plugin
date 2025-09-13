package org.Little_100.projecte.gui;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class DiviningRodGUI {

    private final ProjectE plugin;

    public DiviningRodGUI(ProjectE plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, plugin.getLanguageManager().get("clientside.divining_rod.gui.title"));

        ItemStack low = new ItemStack(Material.STONE);
        ItemMeta lowMeta = low.getItemMeta();
        lowMeta.setDisplayName(plugin.getLanguageManager().get("clientside.divining_rod.gui.low.name"));
        lowMeta.setLore(List.of(plugin.getLanguageManager().get("clientside.divining_rod.gui.low.lore")));
        low.setItemMeta(lowMeta);

        ItemStack medium = new ItemStack(Material.IRON_INGOT);
        ItemMeta mediumMeta = medium.getItemMeta();
        mediumMeta.setDisplayName(plugin.getLanguageManager().get("clientside.divining_rod.gui.medium.name"));
        mediumMeta.setLore(List.of(plugin.getLanguageManager().get("clientside.divining_rod.gui.medium.lore")));
        medium.setItemMeta(mediumMeta);

        ItemStack high = new ItemStack(Material.DIAMOND);
        ItemMeta highMeta = high.getItemMeta();
        highMeta.setDisplayName(plugin.getLanguageManager().get("clientside.divining_rod.gui.high.name"));
        highMeta.setLore(List.of(plugin.getLanguageManager().get("clientside.divining_rod.gui.high.lore")));
        high.setItemMeta(highMeta);

        gui.setItem(2, low);
        gui.setItem(4, medium);
        gui.setItem(6, high);

        player.openInventory(gui);
    }
}