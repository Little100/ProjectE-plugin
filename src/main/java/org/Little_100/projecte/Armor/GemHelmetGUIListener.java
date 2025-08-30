package org.Little_100.projecte.Armor;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GemHelmetGUIListener implements Listener {

    private final ProjectE plugin;

    public GemHelmetGUIListener(ProjectE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = plugin.getLanguageManager().get("item.gem_helmet.gui.title");
        if (!event.getView().getTitle().equals(title)) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (clickedItem.getType() == Material.ENDER_EYE) {
            GemHelmet.toggleNightVision(player);
            GemHelmetGUI.open(player);
        } else if (clickedItem.getType() == Material.BARRIER) {
            player.closeInventory();
        }
    }
}