package org.Little_100.projecte.devices;

import java.util.Collections;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.util.CustomModelDataUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class AlchemicalChest implements Listener {

    private final ProjectE plugin;
    private final NamespacedKey chestKey;

    public AlchemicalChest(ProjectE plugin) {
        this.plugin = plugin;
        this.chestKey = new NamespacedKey(plugin, "alchemical_chest");
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
        newMeta.getPersistentDataContainer().set(chestKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(newMeta);
        return item;
    }

    public boolean isChest(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(chestKey, PersistentDataType.BYTE);
    }
}
