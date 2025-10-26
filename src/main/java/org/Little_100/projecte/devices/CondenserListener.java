package org.Little_100.projecte.devices;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CondenserListener implements Listener {

    private final ProjectE plugin;
    private final CondenserManager condenserManager;

    public CondenserListener(ProjectE plugin) {
        this.plugin = plugin;
        this.condenserManager = plugin.getCondenserManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        CondenserManager.CondenserState condenserState = null;
        for (Map.Entry<Location, CondenserManager.CondenserState> entry :
                condenserManager.getActiveCondensers().entrySet()) {
            if (entry.getValue().getInventory().equals(clickedInventory)) {
                condenserState = entry.getValue();
                break;
            }
        }

        if (condenserState != null) {
            int slot = event.getRawSlot();
            CondenserManager.CondenserType type = condenserState.getType();

            if (slot < clickedInventory.getSize()) {
                if (condenserManager.isNonInteractive(type, slot)) {
                    event.setCancelled(true);
                }

                if (!condenserManager.isTargetSlot(type, slot) && event.getCursor() != null) {}
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory closedInventory = event.getInventory();
        
        Location condenserLocation = null;
        CondenserManager.CondenserState condenserState = null;
        
        for (Map.Entry<Location, CondenserManager.CondenserState> entry :
                condenserManager.getActiveCondensers().entrySet()) {
            if (entry.getValue().getInventory().equals(closedInventory)) {
                condenserLocation = entry.getKey();
                condenserState = entry.getValue();
                break;
            }
        }
        
        if (condenserLocation != null && condenserState != null) {
            String locationKey = condenserLocation.getWorld().getName() + "," 
                + condenserLocation.getBlockX() + "," 
                + condenserLocation.getBlockY() + "," 
                + condenserLocation.getBlockZ();
            
            ItemStack targetItem = null;
            Integer targetSlotIndex = condenserManager.getTargetSlotIndex(condenserState.getType());
            if (targetSlotIndex != null) {
                targetItem = closedInventory.getItem(targetSlotIndex);
            }
            
            plugin.getDatabaseManager().saveCondenserData(
                locationKey,
                condenserState.getOwner(),
                condenserState.getType().ordinal(),
                closedInventory.getContents(),
                targetItem,
                condenserState.getStoredEmc()
            );
        }
    }
}
