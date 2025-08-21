package org.Little_100.projecte.Tome;

import org.Little_100.projecte.TransmutationTable.TransmutationGUI;
import org.Little_100.projecte.util.CustomModelDataUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class TransmutationTabletBookListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (item != null && item.getType() == Material.ENCHANTED_BOOK && CustomModelDataUtil.getCustomModelDataInt(item) == 1) {
                new TransmutationGUI(player).open();
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);

        if (isTransmutationTabletBook(first) || isTransmutationTabletBook(second)) {
            event.setResult(null);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.GRINDSTONE) {
            ItemStack first = event.getInventory().getItem(0);
            ItemStack second = event.getInventory().getItem(1);

            if (isTransmutationTabletBook(first) || isTransmutationTabletBook(second)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isTransmutationTabletBook(ItemStack item) {
        return item != null && item.getType() == Material.ENCHANTED_BOOK && CustomModelDataUtil.getCustomModelDataInt(item) == 1;
    }
}