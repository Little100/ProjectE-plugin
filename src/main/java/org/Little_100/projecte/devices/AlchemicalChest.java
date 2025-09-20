package org.Little_100.projecte.devices;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.util.CustomModelDataUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;

public class AlchemicalChest implements Listener {
    public static final NamespacedKey KEY = new NamespacedKey(ProjectE.getInstance(), "alchemical_chest");

    private final ProjectE plugin;

    public AlchemicalChest(ProjectE plugin) {
        this.plugin = plugin;
    }

    public ItemStack getChestItem() {
        ItemStack item = new ItemStack(Material.BARREL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.getLanguageManager().get("item.projecte.alchemical_chest.name"));
        meta.setLore(
                Collections.singletonList(plugin.getLanguageManager().get("item.projecte.alchemical_chest.lore1")));
        item.setItemMeta(meta);
        item = CustomModelDataUtil.setCustomModelData(item, 1);
        ItemMeta newMeta = item.getItemMeta();
        newMeta.getPersistentDataContainer().set(KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(newMeta);
        return item;
    }

    public boolean isChest(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(KEY, PersistentDataType.BYTE);
    }
}
