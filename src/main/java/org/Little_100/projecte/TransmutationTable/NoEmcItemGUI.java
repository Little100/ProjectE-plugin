package org.Little_100.projecte.TransmutationTable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class NoEmcItemGUI implements InventoryHolder {
    private final Inventory inventory;
    private final List<ItemStack> items;
    private final int page;

    public NoEmcItemGUI(List<ItemStack> items, int page) {
        this.items = items;
        this.page = page;
        int totalPages = (int) Math.ceil((double) items.size() / 45);
        this.inventory = Bukkit.createInventory(this, 54, "No EMC Items (Page " + (page + 1) + "/" + totalPages + ")");
        loadItems();
    }

    private void loadItems() {
        inventory.clear();
        int startIndex = page * 45;
        int endIndex = Math.min(startIndex + 45, items.size());

        for (int i = startIndex; i < endIndex; i++) {
            inventory.addItem(items.get(i));
        }

        if (page > 0) {
            inventory.setItem(45, createNavItem("Previous Page", Material.ARROW));
        }
        if (endIndex < items.size()) {
            inventory.setItem(53, createNavItem("Next Page", Material.ARROW));
        }
    }

    private ItemStack createNavItem(String name, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + name);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void openInventory(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public int getPage() {
        return page;
    }

    public List<ItemStack> getItems() {
        return items;
    }
}