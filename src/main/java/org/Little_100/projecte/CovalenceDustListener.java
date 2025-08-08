package org.Little_100.projecte;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

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
                    event.getInventory().getClass().getMethod("setRepairCostAmount", int.class).invoke(event.getInventory(), 0);
                } catch (Exception ignored) {}
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
                } else if (plugin.getCovalenceDust().isLowCovalenceDust(item) || plugin.getCovalenceDust().isMediumCovalenceDust(item) || plugin.getCovalenceDust().isHighCovalenceDust(item)) {
                    dust = item;
                    dustCount++;
                }
            }
        }

        if (tool == null || dust == null || dustCount != 1) {
            return;
        }

        if (tool == null || dust == null) {
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
    } //中级小破粉可以修复的工具

    private boolean isHighTier(Material material) {
        String name = material.name();
        return name.startsWith("DIAMOND_") || name.startsWith("NETHERITE_");
    } //高级小破粉可以修复的工具
}