package org.Little_100.projecte.listeners;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.gui.PdcItemDebugGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PdcItemDebugGUIListener implements Listener {

    private final ProjectE plugin;
    private final Map<UUID, PdcItemDebugGUI> openGuis = new HashMap<>();

    public PdcItemDebugGUIListener(ProjectE plugin) {
        this.plugin = plugin;
    }

    public void registerGui(Player player, PdcItemDebugGUI gui) {
        openGuis.put(player.getUniqueId(), gui);
    }

    public void unregisterGui(Player player) {
        openGuis.remove(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!title.startsWith(ChatColor.DARK_PURPLE + "PDC物品调试")) {
            return;
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        PdcItemDebugGUI gui = openGuis.get(player.getUniqueId());
        if (gui == null) {
            return;
        }

        if (clicked.getType() == Material.ARROW) {
            String displayName = clicked.getItemMeta().getDisplayName();
            if (displayName.contains("下一页")) {
                gui.nextPage();
            } else if (displayName.contains("上一页")) {
                gui.previousPage();
            }
            return;
        }

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            unregisterGui(player);
            return;
        }

        // 点击物品
        if (plugin.getEmcManager().isPdcItem(clicked)) {
            // 移除lore以获取原始物品
            ItemStack originalItem = clicked.clone();
            if (originalItem.hasItemMeta()) {
                originalItem.getItemMeta().setLore(null);
            }
            
            gui.showItemDebug(clicked);
        }
    }
}
