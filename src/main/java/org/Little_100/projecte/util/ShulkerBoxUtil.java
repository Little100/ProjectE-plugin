package org.Little_100.projecte.util;

import org.Little_100.projecte.ProjectE;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ShulkerBoxUtil {

    /**
     * 检查潜影盒内的所有物品是否都有EMC值。
     * @param shulkerBoxItem 潜影盒物品
     * @return 如果所有物品都有EMC值，则返回null；否则返回第一个没有EMC值的物品。
     */
    public static ItemStack getFirstItemWithoutEmc(ItemStack shulkerBoxItem) {
        if (shulkerBoxItem == null || !(shulkerBoxItem.getItemMeta() instanceof BlockStateMeta)) {
            return null;
        }

        BlockStateMeta bsm = (BlockStateMeta) shulkerBoxItem.getItemMeta();
        if (!(bsm.getBlockState() instanceof ShulkerBox)) {
            return null;
        }

        ShulkerBox shulkerBox = (ShulkerBox) bsm.getBlockState();
        for (ItemStack item : shulkerBox.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                long emc = ProjectE.getInstance().getEmcManager().getEmc(item);
                if (emc <= 0) {
                    return item; // 找到第一个没有EMC的物品
                }
            }
        }

        return null; // 所有物品都有EMC
    }

    /**
     * 计算潜影盒内所有物品的总EMC值。
     * @param shulkerBoxItem 潜影盒物品
     * @return 内部物品的总EMC值
     */
    public static long getTotalEmcOfContents(ItemStack shulkerBoxItem) {
        if (shulkerBoxItem == null || !(shulkerBoxItem.getItemMeta() instanceof BlockStateMeta)) {
            return 0;
        }

        BlockStateMeta bsm = (BlockStateMeta) shulkerBoxItem.getItemMeta();
        if (!(bsm.getBlockState() instanceof ShulkerBox)) {
            return 0;
        }

        long totalEmc = 0;
        ShulkerBox shulkerBox = (ShulkerBox) bsm.getBlockState();
        for (ItemStack item : shulkerBox.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                totalEmc += ProjectE.getInstance().getEmcManager().getEmc(item) * item.getAmount();
            }
        }
        return totalEmc;
    }
}
