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

public class EnergyCollectorGUI implements Listener {

    private final ProjectE plugin;
    private final Player player;
    private final Location collectorLocation;
    private final EnergyCollectorManager.CollectorData data;
    private Inventory inventory;
    private long currentEmc;

    private static final int EMC_DISPLAY_SLOT = 49;

    public EnergyCollectorGUI(ProjectE plugin, Player player, Location location,
            EnergyCollectorManager.CollectorData data) {
        this.plugin = plugin;
        this.player = player;
        this.collectorLocation = location;
        this.data = data;
        this.currentEmc = data.storedEmc;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        String title = getTitleForType(data.type);
        inventory = Bukkit.createInventory(null, 54, title);

        int itemSlots = EnergyCollector.getInventorySize(data.type);
        int[] slotPositions = getSlotPositions(data.type);

        for (int i = 0; i < itemSlots && i < slotPositions.length; i++) {
            if (data.inventory[i] != null) {
                inventory.setItem(slotPositions[i], data.inventory[i]);
            }
        }

        ItemStack glassPane = createGlassPane();
        for (int i = 0; i < 54; i++) {
            if (!isItemSlot(i, data.type) && i != EMC_DISPLAY_SLOT) {
                inventory.setItem(i, glassPane);
            }
        }

        updateEmcDisplay();

        player.openInventory(inventory);
    }

    private String getTitleForType(int type) {
        switch (type) {
            case EnergyCollector.TYPE_MK1:
                return plugin.getLanguageManager().get("gui.energy_collector_mk1.title");
            case EnergyCollector.TYPE_MK2:
                return plugin.getLanguageManager().get("gui.energy_collector_mk2.title");
            case EnergyCollector.TYPE_MK3:
                return plugin.getLanguageManager().get("gui.energy_collector_mk3.title");
            default:
                return "Energy Collector";
        }
    }

    private int[] getSlotPositions(int type) {
        switch (type) {
            case EnergyCollector.TYPE_MK1:
                return new int[] { 20, 21, 22, 23 };
            case EnergyCollector.TYPE_MK2:
                return new int[] { 19, 20, 21, 22, 28, 29, 30, 31 };
            case EnergyCollector.TYPE_MK3:
                return new int[] { 19, 20, 21, 22, 28, 29, 30, 31, 37, 38, 39, 40 };
            default:
                return new int[0];
        }
    }

    private boolean isItemSlot(int slot, int type) {
        int[] validSlots = getSlotPositions(type);
        for (int validSlot : validSlots) {
            if (validSlot == slot) {
                return true;
            }
        }
        return false;
    }

    private void updateEmcDisplay() {
        ItemStack emcDisplay = new ItemStack(Material.GLOWSTONE_DUST);
        ItemMeta meta = emcDisplay.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getLanguageManager().get("gui.energy_collector.emc_display")
                    .replace("%emc%", String.valueOf(currentEmc)));
            emcDisplay.setItemMeta(meta);
        }
        inventory.setItem(EMC_DISPLAY_SLOT, emcDisplay);
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

        if (!isItemSlot(slot, data.type)) {
            event.setCancelled(true);
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

        for (int slot : event.getRawSlots()) {
            if (slot >= 0 && slot < 54) {
                if (!isItemSlot(slot, data.type)) {
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

        int[] slotPositions = getSlotPositions(data.type);
        for (int i = 0; i < slotPositions.length; i++) {
            data.inventory[i] = inventory.getItem(slotPositions[i]);
        }

        plugin.getEnergyCollectorManager().closeCollectorGUI(player, currentEmc, data.inventory);

        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
        InventoryDragEvent.getHandlerList().unregister(this);
    }
}
