package org.Little_100.projecte.alchemicalbag;

import org.Little_100.projecte.ProjectE;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryManager implements Listener {

    private final ProjectE plugin;
    private final AlchemicalBagManager alchemicalBagManager;
    private final Map<UUID, Inventory> openInventories = new HashMap<>();
    private final Map<UUID, String> openBagColors = new HashMap<>();

    public InventoryManager(ProjectE plugin, AlchemicalBagManager alchemicalBagManager) {
        this.plugin = plugin;
        this.alchemicalBagManager = alchemicalBagManager;
    }

    public void openBagInventory(UUID playerUUID, String bagColor, Inventory inventory) {
        openInventories.put(playerUUID, inventory);
        openBagColors.put(playerUUID, bagColor);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Inventory closedInventory = event.getInventory();

        if (openInventories.containsKey(playerUUID) && openInventories.get(playerUUID) == closedInventory) {
            String closedBagColor = openBagColors.remove(playerUUID);
            openInventories.remove(playerUUID);

            if (closedBagColor != null) {
                plugin.getDatabaseManager().saveBagInventory(playerUUID, closedBagColor, closedInventory.getContents());
                plugin.getLogger().info("Player " + player.getName() + " closed Alchemical Bag with color "
                        + closedBagColor + ", contents saved.");
            } else {
                plugin.getLogger().warning(
                        "Player " + player.getName() + " closed an Alchemical Bag, but its color could not be found!");
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        UUID playerUUID = player.getUniqueId();
        Inventory clickedInventory = event.getInventory();

        if (openInventories.containsKey(playerUUID) && openInventories.get(playerUUID) == clickedInventory) {
            String currentBagColor = openBagColors.get(playerUUID);
            if (currentBagColor == null) {
                event.setCancelled(true);
                plugin.getLogger().warning("Player " + player.getName()
                        + " clicked in an Alchemical Bag, but its color could not be found!");
                return;
            }

            ItemStack cursorItem = event.getCursor();
            ItemStack currentItem = event.getCurrentItem();

            if (isForbiddenBag(cursorItem, currentBagColor)
                    || (event.isShiftClick() && isForbiddenBag(currentItem, currentBagColor))) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot put an Alchemical Bag inside another!");
            }
        }
    }

    private boolean isForbiddenBag(ItemStack item, String targetColorIdentifier) {
        if (item == null || item.getType() != Material.LEATHER_HORSE_ARMOR || !item.hasItemMeta()) {
            return false;
        }
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta == null)
            return false;
        return true;
    }
}