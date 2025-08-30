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

public class EnergyCondenser implements Listener {

    private final ProjectE plugin;
    private final NamespacedKey condenserKey;

    public EnergyCondenser(ProjectE plugin) {
        this.plugin = plugin;
        this.condenserKey = new NamespacedKey(plugin, "energy_condenser");
    }

    public ItemStack getCondenserItem() {
        ItemStack item = new ItemStack(Material.BARREL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.getLanguageManager().get("item.projecte.energy_condenser.name"));
        meta.setLore(Collections.singletonList(plugin.getLanguageManager().get("item.projecte.energy_condenser.lore1")));
        item.setItemMeta(meta);
        item = CustomModelDataUtil.setCustomModelData(item, 2);
        ItemMeta newMeta = item.getItemMeta();
        newMeta.getPersistentDataContainer().set(condenserKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(newMeta);
        return item;
    }

    public boolean isCondenser(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(condenserKey, PersistentDataType.BYTE);
    }
}