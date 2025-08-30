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

public class RedMatterFurnace implements Listener {

    private final ProjectE plugin;
    private final NamespacedKey furnaceKey;

    public RedMatterFurnace(ProjectE plugin) {
        this.plugin = plugin;
        this.furnaceKey = new NamespacedKey(plugin, "red_matter_furnace");
    }

    public ItemStack getFurnaceItem() {
        ItemStack item = new ItemStack(Material.FURNACE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.getLanguageManager().get("item.projecte.red_matter_furnace.name"));
        meta.setLore(Collections.singletonList(plugin.getLanguageManager().get("item.projecte.red_matter_furnace.lore1")));
        item.setItemMeta(meta);
        item = CustomModelDataUtil.setCustomModelData(item, 2);
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

// 这是彩/蛋 爱你 Minecraft :)
// 也爱你 看着屏幕的家伙