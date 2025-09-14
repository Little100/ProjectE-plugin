package org.Little_100.projecte.util;

import org.Little_100.projecte.ProjectE;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemUtils {
    private ItemUtils() {}

    public static boolean isProjectEItem(ItemStack item, String id) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String foundId =
                container.get(new NamespacedKey(ProjectE.getInstance(), "projecte_id"), PersistentDataType.STRING);
        return id.equals(foundId);
    }
}
