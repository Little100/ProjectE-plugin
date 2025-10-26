package org.Little_100.projecte.devices;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.util.CustomModelDataUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnergyCollector implements Listener {
    public static final NamespacedKey KEY_MK1 = new NamespacedKey(ProjectE.getInstance(), "energy_collector_mk1");
    public static final NamespacedKey KEY_MK2 = new NamespacedKey(ProjectE.getInstance(), "energy_collector_mk2");
    public static final NamespacedKey KEY_MK3 = new NamespacedKey(ProjectE.getInstance(), "energy_collector_mk3");

    private final ProjectE plugin;

    public static final int TYPE_MK1 = 1;
    public static final int TYPE_MK2 = 2;
    public static final int TYPE_MK3 = 3;

    public static final long EMC_RATE_MK1 = 4;
    public static final long EMC_RATE_MK2 = 10;
    public static final long EMC_RATE_MK3 = 40;

    public static final int INV_SIZE_MK1 = 4;
    public static final int INV_SIZE_MK2 = 8;
    public static final int INV_SIZE_MK3 = 12;

    public EnergyCollector(ProjectE plugin) {
        this.plugin = plugin;
    }

    public ItemStack getCollectorItem(int type) {
        ItemStack item = new ItemStack(Material.GLOWSTONE);
        ItemMeta meta = item.getItemMeta();

        String name;
        List<String> lore = new ArrayList<>();
        NamespacedKey key;
        int customModelData;

        switch (type) {
            case TYPE_MK1:
                name = plugin.getLanguageManager().get("item.projecte.energy_collector_mk1.name");
                lore.add(plugin.getLanguageManager().get("item.projecte.energy_collector_mk1.lore1"));
                lore.add(plugin.getLanguageManager().get("item.projecte.energy_collector_mk1.lore2"));
                key = KEY_MK1;
                customModelData = 1;
                break;
            case TYPE_MK2:
                name = plugin.getLanguageManager().get("item.projecte.energy_collector_mk2.name");
                lore.add(plugin.getLanguageManager().get("item.projecte.energy_collector_mk2.lore1"));
                lore.add(plugin.getLanguageManager().get("item.projecte.energy_collector_mk2.lore2"));
                key = KEY_MK2;
                customModelData = 2;
                break;
            case TYPE_MK3:
                name = plugin.getLanguageManager().get("item.projecte.energy_collector_mk3.name");
                lore.add(plugin.getLanguageManager().get("item.projecte.energy_collector_mk3.lore1"));
                lore.add(plugin.getLanguageManager().get("item.projecte.energy_collector_mk3.lore2"));
                key = KEY_MK3;
                customModelData = 3;
                break;
            default:
                return null;
        }

        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);

        item = CustomModelDataUtil.setCustomModelData(item, customModelData);

        ItemMeta newMeta = item.getItemMeta();
        newMeta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(newMeta);

        return item;
    }

    public boolean isCollector(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(KEY_MK1, PersistentDataType.BYTE)
                || meta.getPersistentDataContainer().has(KEY_MK2, PersistentDataType.BYTE)
                || meta.getPersistentDataContainer().has(KEY_MK3, PersistentDataType.BYTE);
    }

    public int getCollectorType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(KEY_MK1, PersistentDataType.BYTE)) {
            return TYPE_MK1;
        } else if (meta.getPersistentDataContainer().has(KEY_MK2, PersistentDataType.BYTE)) {
            return TYPE_MK2;
        } else if (meta.getPersistentDataContainer().has(KEY_MK3, PersistentDataType.BYTE)) {
            return TYPE_MK3;
        }
        return 0;
    }

    public static long getEmcRate(int type) {
        switch (type) {
            case TYPE_MK1:
                return EMC_RATE_MK1;
            case TYPE_MK2:
                return EMC_RATE_MK2;
            case TYPE_MK3:
                return EMC_RATE_MK3;
            default:
                return 0;
        }
    }

    public static int getInventorySize(int type) {
        switch (type) {
            case TYPE_MK1:
                return INV_SIZE_MK1;
            case TYPE_MK2:
                return INV_SIZE_MK2;
            case TYPE_MK3:
                return INV_SIZE_MK3;
            default:
                return 0;
        }
    }

    public static NamespacedKey getKey(int type) {
        switch (type) {
            case TYPE_MK1:
                return KEY_MK1;
            case TYPE_MK2:
                return KEY_MK2;
            case TYPE_MK3:
                return KEY_MK3;
            default:
                return null;
        }
    }
}
