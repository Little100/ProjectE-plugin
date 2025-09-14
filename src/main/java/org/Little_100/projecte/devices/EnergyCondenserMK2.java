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

public class EnergyCondenserMK2 implements Listener {
    public static final NamespacedKey KEY = new NamespacedKey(ProjectE.getInstance(), "energy_condenser_mk2");;

    private final ProjectE plugin;

    public EnergyCondenserMK2(ProjectE plugin) {
        this.plugin = plugin;
    }

    public ItemStack getCondenserMK2Item() {
        ItemStack item = new ItemStack(Material.BARREL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.getLanguageManager().get("item.projecte.energy_condenser_mk2.name"));
        meta.setLore(
                Collections.singletonList(plugin.getLanguageManager().get("item.projecte.energy_condenser_mk2.lore1")));
        item.setItemMeta(meta);
        item = CustomModelDataUtil.setCustomModelData(item, 3);
        ItemMeta newMeta = item.getItemMeta();
        newMeta.getPersistentDataContainer().set(KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(newMeta);
        return item;
    }

    public boolean isCondenserMK2(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(KEY, PersistentDataType.BYTE);
    }
}
