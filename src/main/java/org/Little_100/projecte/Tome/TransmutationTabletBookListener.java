package org.Little_100.projecte.Tome;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.TransmutationTable.TransmutationGUI;
import org.Little_100.projecte.util.CustomModelDataUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

public class TransmutationTabletBookListener implements Listener {

    private final NamespacedKey projecteIdKey;
    
    public TransmutationTabletBookListener() {
        this.projecteIdKey = new NamespacedKey(ProjectE.getInstance(), "projecte_id");
    }

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
                    ProjectE.getInstance().getLogger().warning("打开转换工具GUI时失败: " + e.getMessage());
                    e.printStackTrace();
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
        if (meta.getPersistentDataContainer().has(projecteIdKey, PersistentDataType.STRING)) {
            String id = meta.getPersistentDataContainer().get(projecteIdKey, PersistentDataType.STRING);
            return "transmutation_tablet_book".equals(id);
        }
        return false;
    }
}