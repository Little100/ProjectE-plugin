package org.Little_100.projecte.devices;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;

public class CondenserListener implements Listener {

    private final CondenserManager condenserManager;

    public CondenserListener(ProjectE plugin) {
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
}
