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

public class DarkMatterFurnace implements Listener {

    private final ProjectE plugin;
    private final NamespacedKey furnaceKey;

    public DarkMatterFurnace(ProjectE plugin) {
        this.plugin = plugin;
        this.furnaceKey = new NamespacedKey(plugin, "dark_matter_furnace");
    }

    public ItemStack getFurnaceItem() {
        ItemStack item = new ItemStack(Material.FURNACE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.getLanguageManager().get("item.projecte.dark_matter_furnace.name"));
        meta.setLore(
                Collections.singletonList(plugin.getLanguageManager().get("item.projecte.dark_matter_furnace.lore1")));
        item.setItemMeta(meta);
        item = CustomModelDataUtil.setCustomModelData(item, 1);
        ItemMeta newMeta = item.getItemMeta();
        newMeta.getPersistentDataContainer().set(furnaceKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(newMeta);
        return item;
    }

    public boolean isFurnace(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(furnaceKey, PersistentDataType.BYTE);
    }
}
