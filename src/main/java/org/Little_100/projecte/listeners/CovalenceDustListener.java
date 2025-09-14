package org.Little_100.projecte.listeners;

import org.Little_100.projecte.CovalenceDust;
import org.Little_100.projecte.Debug;
import org.Little_100.projecte.ProjectE;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class CovalenceDustListener implements Listener {

    private final ProjectE plugin;

    public CovalenceDustListener(ProjectE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack[] contents = event.getInventory().getContents();
        if (contents.length < 2 || contents[0] == null || contents[1] == null) {
            return;
        }

        ItemStack item1 = contents[0];
        ItemStack item2 = contents[1];
        ItemStack anvilResult = event.getResult();

        String item1Key = plugin.getEmcManager().getItemKey(item1);
        String item2Key = plugin.getEmcManager().getItemKey(item2);

        if (!item1Key.contains("projecte_id") && !item2Key.contains("projecte_id")) {
            java.util.Map<String, String> placeholders = new java.util.HashMap<>();
            placeholders.put("item", item1.getType().toString());
            Debug.log("debug.anvil.item_not_plugin_item", placeholders);
            placeholders.put("item", item2.getType().toString());
            Debug.log("debug.anvil.item_not_plugin_item", placeholders);
            return;
        }

        java.util.Map<String, String> placeholders = new java.util.HashMap<>();
        placeholders.put("item1", item1.getType().toString());
        placeholders.put("item2", item2.getType().toString());
        placeholders.put("result", anvilResult != null ? anvilResult.getType().toString() : "null");
        Debug.log("debug.anvil.combining_items", placeholders);

        ItemStack tool = contents[0];
        ItemStack dust = contents[1];

        if (!isRepairable(tool.getType())) {
            return;
        }

        CovalenceDust covalenceDust = plugin.getCovalenceDust();
        int repairAmount = 0;

        if (covalenceDust.isLowCovalenceDust(dust) && isLowTier(tool.getType())) {
            repairAmount = 250;
        } else if (covalenceDust.isMediumCovalenceDust(dust) && isMediumTier(tool.getType())) {
            repairAmount = 500;
        } else if (covalenceDust.isHighCovalenceDust(dust) && isHighTier(tool.getType())) {
            repairAmount = tool.getType().getMaxDurability();
        }

        if (repairAmount > 0) {
            ItemStack result = tool.clone();
            ItemMeta meta = result.getItemMeta();
            if (meta instanceof Damageable) {
                Damageable damageable = (Damageable) meta;
                damageable.setDamage(Math.max(0, damageable.getDamage() - repairAmount));
                result.setItemMeta(meta);
                event.setResult(result);
                event.getInventory().setRepairCost(0); // 牢版本
                try {
                    event.getInventory()
                            .getClass()
                            .getMethod("setRepairCostAmount", int.class)
                            .invoke(event.getInventory(), 0);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        ItemStack[] matrix = event.getInventory().getMatrix();
        if (matrix.length == 0) {
            return;
        }

        ItemStack tool = null;
        ItemStack dust = null;

        int dustCount = 0;
        for (ItemStack item : matrix) {
            if (item != null && item.getType() != Material.AIR) {
                if (isRepairable(item.getType()) && tool == null) {
                    tool = item;
                } else if (plugin.getCovalenceDust().isLowCovalenceDust(item)
                        || plugin.getCovalenceDust().isMediumCovalenceDust(item)
                        || plugin.getCovalenceDust().isHighCovalenceDust(item)) {
                    dust = item;
                    dustCount++;
                }
            }
        }

        if (tool == null || dust == null) {
            return;
        }

        if (dustCount != 1) {
            return;
        }

        CovalenceDust covalenceDust = plugin.getCovalenceDust();
        int repairAmount = 0;

        if (covalenceDust.isLowCovalenceDust(dust) && isLowTier(tool.getType())) {
            repairAmount = 250;
        } else if (covalenceDust.isMediumCovalenceDust(dust) && isMediumTier(tool.getType())) {
            repairAmount = 500;
        } else if (covalenceDust.isHighCovalenceDust(dust) && isHighTier(tool.getType())) {
            repairAmount = tool.getType().getMaxDurability();
        }

        if (repairAmount > 0) {
            ItemStack result = tool.clone();
            ItemMeta meta = result.getItemMeta();
            if (meta instanceof Damageable) {
                Damageable damageable = (Damageable) meta;
                damageable.setDamage(Math.max(0, damageable.getDamage() - repairAmount));
                result.setItemMeta(meta);
                event.getInventory().setResult(result);
            }
        }
    }

    private boolean isRepairable(Material material) {
        return material.getMaxDurability() > 0;
    }

    private boolean isLowTier(Material material) {
        String name = material.name();
        return name.startsWith("WOODEN_") || name.startsWith("STONE_") || name.startsWith("CHAINMAIL_");
    } // 低级小破粉可以修复的工具

    private boolean isMediumTier(Material material) {
        String name = material.name();
        return name.startsWith("IRON_") || name.startsWith("GOLDEN_");
    } // 中级小破粉可以修复的工具

    private boolean isHighTier(Material material) {
        String name = material.name();
        return name.startsWith("DIAMOND_") || name.startsWith("NETHERITE_");
    } // 高级小破粉可以修复的工具
}
