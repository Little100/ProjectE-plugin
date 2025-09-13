package org.Little_100.projecte.tools;

import java.util.List;
import org.Little_100.projecte.ProjectE;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class RepairTalisman {
    private final ProjectE plugin;
    private final NamespacedKey key;
    private ItemStack repairTalisman;

    public RepairTalisman(ProjectE plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "repair_talisman");
        createRepairTalisman();
    }

    private void createRepairTalisman() {
        repairTalisman = new ItemStack(Material.PAPER);
        // 使用setCustomModelDataBoth方法同时设置字符串和整数值
        repairTalisman = org.Little_100.projecte.util.CustomModelDataUtil.setCustomModelDataBoth(
                repairTalisman, "repair_talisman", 1);

        ItemMeta meta = repairTalisman.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getLanguageManager().get("item.repair_talisman.name"));
            meta.setLore(List.of(plugin.getLanguageManager().get("item.repair_talisman.lore1")));

            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            container.set(new NamespacedKey(plugin, "projecte_id"), PersistentDataType.STRING, "repair_talisman");

            repairTalisman.setItemMeta(meta);
        }
    }

    public ItemStack getRepairTalisman() {
        return repairTalisman.clone();
    }

    public boolean isRepairTalisman(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String foundId = container.get(new NamespacedKey(plugin, "projecte_id"), PersistentDataType.STRING);
        return "repair_talisman".equals(foundId);
    }

    public void setEmcValue() {
        var emcManager = plugin.getEmcManager();
        java.io.File configFile = new java.io.File(plugin.getDataFolder(), "custommoditememc.yml");
        if (!configFile.exists()) {
            plugin.getLogger().warning("custommoditememc.yml not found, repair talisman EMC value will not be loaded.");
            return;
        }
        org.bukkit.configuration.file.YamlConfiguration config =
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(configFile);

        String itemKey = emcManager.getItemKey(getRepairTalisman());
        long emc = config.getLong("repair_talisman", 490);
        emcManager.registerEmc(itemKey, emc);
    }
}
