package org.Little_100.projecte.devices;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AlchemicalChestGUI implements Listener {

    private final ProjectE plugin;
    private final Player player;
    private final Location chestLocation;
    private final ItemStack[] allContents;
    private int currentPage = 0;
    private Inventory inventory;

    private static final int TOTAL_SLOTS = 104;
    private static final int PAGE_SIZE = 45;
    private static final int TOTAL_PAGES = 3;

    private static final int PREV_PAGE_SLOT = 45;
    private static final int PAGE_INFO_SLOT = 49;
    private static final int NEXT_PAGE_SLOT = 53;

    public AlchemicalChestGUI(ProjectE plugin, Player player, Location location, ItemStack[] contents) {
        this.plugin = plugin;
        this.player = player;
        this.chestLocation = location;
        this.allContents = contents != null && contents.length == TOTAL_SLOTS ? contents : new ItemStack[TOTAL_SLOTS];

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        openPage(0);
    }

    private void openPage(int page) {
        this.currentPage = page;

        String title = plugin.getLanguageManager().get("gui.alchemical_chest.title")
                .replace("%page%", String.valueOf(page + 1))
                .replace("%total%", String.valueOf(TOTAL_PAGES));

        inventory = Bukkit.createInventory(null, 54, title);

        int startIndex = page * PAGE_SIZE;
        int itemsOnThisPage = Math.min(PAGE_SIZE, TOTAL_SLOTS - startIndex);

        for (int i = 0; i < itemsOnThisPage; i++) {
            int contentIndex = startIndex + i;
            if (contentIndex < allContents.length && allContents[contentIndex] != null) {
                inventory.setItem(i, allContents[contentIndex]);
            }
        }

        ItemStack glassPane = createGlassPane();
        for (int i = itemsOnThisPage; i < 45; i++) {
            inventory.setItem(i, glassPane);
        }

        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, glassPane);
        }

        if (currentPage > 0) {
            inventory.setItem(PREV_PAGE_SLOT, createControlButton(Material.ARROW,
                    plugin.getLanguageManager().get("gui.alchemical_chest.prev_page")));
        }

        inventory.setItem(PAGE_INFO_SLOT, createControlButton(Material.PAPER,
                plugin.getLanguageManager().get("gui.alchemical_chest.page_info")
                        .replace("%page%", String.valueOf(page + 1))
                        .replace("%total%", String.valueOf(TOTAL_PAGES))));

        if (currentPage < TOTAL_PAGES - 1) {
            inventory.setItem(NEXT_PAGE_SLOT, createControlButton(Material.ARROW,
                    plugin.getLanguageManager().get("gui.alchemical_chest.next_page")));
        }

        player.openInventory(inventory);
    }

    private ItemStack createControlButton(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGlassPane() {
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glassPane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glassPane.setItemMeta(meta);
        }
        return glassPane;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        if (!event.getWhoClicked().equals(player))
            return;
        if (event.getInventory() != inventory)
            return;

        int slot = event.getRawSlot();

        if (slot < 0 || slot >= 54) {
            return;
        }

        int startIndex = currentPage * PAGE_SIZE;
        int itemsOnThisPage = Math.min(PAGE_SIZE, TOTAL_SLOTS - startIndex);

        if (slot >= itemsOnThisPage && slot < 45) {
            event.setCancelled(true);
            return;
        }

        if (slot >= 45) {
            event.setCancelled(true);

            if (slot == PREV_PAGE_SLOT && currentPage > 0) {
                saveCurrentPage();
                openPage(currentPage - 1);
            } else if (slot == NEXT_PAGE_SLOT && currentPage < TOTAL_PAGES - 1) {
                saveCurrentPage();
                openPage(currentPage + 1);
            }
            return;
        }

    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        if (!event.getWhoClicked().equals(player))
            return;
        if (event.getInventory() != inventory)
            return;

        int startIndex = currentPage * PAGE_SIZE;
        int itemsOnThisPage = Math.min(PAGE_SIZE, TOTAL_SLOTS - startIndex);

        for (int slot : event.getRawSlots()) {
            if (slot >= 0 && slot < 54) {
                if (slot >= itemsOnThisPage && slot < 45) {
                    event.setCancelled(true);
                    return;
                }
                if (slot >= 45 && slot < 54) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player))
            return;
        if (!event.getPlayer().equals(player))
            return;
        if (event.getInventory() != inventory)
            return;

        saveCurrentPage();

        plugin.getAlchemicalChestManager().closeChestGUI(player, allContents);

        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
        InventoryDragEvent.getHandlerList().unregister(this);
    }

    private void saveCurrentPage() {
        int startIndex = currentPage * PAGE_SIZE;
        int itemsOnThisPage = Math.min(PAGE_SIZE, TOTAL_SLOTS - startIndex);

        for (int i = 0; i < itemsOnThisPage; i++) {
            int contentIndex = startIndex + i;
            if (contentIndex < allContents.length) {
                allContents[contentIndex] = inventory.getItem(i);
            }
        }
    }
}
