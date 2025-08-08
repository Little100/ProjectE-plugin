package org.Little_100.projecte.Tools;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class Repair_Talisman {
    private final ProjectE plugin;
    private final NamespacedKey key;
    private ItemStack repairTalisman;

    public Repair_Talisman(ProjectE plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "repair_talisman");
        createRepairTalisman();
    }

    private void createRepairTalisman() {
        repairTalisman = new ItemStack(Material.PAPER);
        repairTalisman = org.Little_100.projecte.util.CustomModelDataUtil.setCustomModelData(repairTalisman, 1);

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
        PersistentDataContainer container = meta.getPersistentDataContainer();
        boolean hasKey = container.has(key, PersistentDataType.BYTE);
        boolean hasId = container.has(new NamespacedKey(plugin, "projecte_id"), PersistentDataType.STRING);
        String foundId = container.get(new NamespacedKey(plugin, "projecte_id"), PersistentDataType.STRING);

        return hasKey && hasId && "repair_talisman".equals(foundId);
    }

    public void setEmcValue() {
        var db = plugin.getDatabaseManager();
        db.setEmc(plugin.getEmcManager().getItemKey(getRepairTalisman()), 0); // Assuming it has no EMC value or it's calculated from recipe
    }
}
