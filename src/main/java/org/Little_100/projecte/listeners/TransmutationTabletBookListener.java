package org.Little_100.projecte.listeners;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.gui.TransmutationGUI;
import org.Little_100.projecte.util.Constants;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.logging.Level;

public class TransmutationTabletBookListener implements Listener {

    public TransmutationTabletBookListener() {}

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            ItemStack item = event.getItem();

            if (isTransmutationTabletBook(item)) {
                try {
                    new TransmutationGUI(player).open();
                    event.setCancelled(true);
                } catch (Exception e) {
                    ProjectE.getInstance().getLogger().log(Level.WARNING, "打开转换工具GUI时失败", e);
                }
            }
        }
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);

        if (isTransmutationTabletBookSafe(first) || isTransmutationTabletBookSafe(second)) {
            event.setResult(null);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.GRINDSTONE) {
            ItemStack first = event.getInventory().getItem(0);
            ItemStack second = event.getInventory().getItem(1);

            if (isTransmutationTabletBookSafe(first) || isTransmutationTabletBookSafe(second)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isTransmutationTabletBookSafe(ItemStack item) {
        try {
            return isTransmutationTabletBook(item);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTransmutationTabletBook(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        if (meta.getPersistentDataContainer().has(Constants.ID_KEY, PersistentDataType.STRING)) {
            String id = meta.getPersistentDataContainer().get(Constants.ID_KEY, PersistentDataType.STRING);
            return "transmutation_tablet_book".equals(id);
        }
        return false;
    }
}
