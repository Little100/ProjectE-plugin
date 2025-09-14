package org.Little_100.projecte.listeners;

import java.util.HashMap;
import org.Little_100.projecte.util.Constants;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class CovalenceDustCraftListener implements Listener {

    @EventHandler
    public void onCraftItem(InventoryClickEvent event) {
        // 只处理工作台中的结果槽点击
        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;
        if (!(event.getInventory() instanceof CraftingInventory)) return;

        CraftingInventory inv = (CraftingInventory) event.getInventory();
        ItemStack result = inv.getResult();

        // 检查是否为共价粉配方
        if (!isCovalenceDust(result)) {
            return;
        }

        // 取消默认的合成行为，我们自己处理
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        if (event.isShiftClick()) {
            handleShiftClick(player, inv, result);
        } else {
            handleNormalClick(player, inv, result);
        }
    }

    private void handleShiftClick(Player player, CraftingInventory inv, ItemStack result) {
        // 计算最大可合成数量
        int maxCrafts = calculateMaxCrafts(inv);
        if (maxCrafts <= 0) return;

        // 复制结果物品并设置正确的数量
        ItemStack resultStack = result.clone();
        resultStack.setAmount(result.getAmount() * maxCrafts);

        // 将合成的物品添加到玩家背包
        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(resultStack);

        // 计算实际成功合成的数量（考虑背包满的情况）
        int craftedAmount = resultStack.getAmount();
        if (!leftovers.isEmpty()) {
            craftedAmount -=
                    leftovers.values().stream().mapToInt(ItemStack::getAmount).sum();
        }
        int numCrafted = craftedAmount / result.getAmount();

        // 如果成功合成了至少一个，则消耗原料
        if (numCrafted > 0) {
            consumeIngredients(inv, numCrafted);
        }
    }

    private void handleNormalClick(Player player, CraftingInventory inv, ItemStack result) {
        ItemStack cursorItem = player.getItemOnCursor();

        // 如果鼠标上没有物品，直接放置
        if (cursorItem.getType() == Material.AIR) {
            consumeIngredients(inv, 1);
            player.setItemOnCursor(result);
        }

        // 如果鼠标上有同类物品，并且可以堆叠
        else if (cursorItem.isSimilar(result)) {
            int canAdd = result.getMaxStackSize() - cursorItem.getAmount();
            if (canAdd >= result.getAmount()) {
                consumeIngredients(inv, 1);
                cursorItem.setAmount(cursorItem.getAmount() + result.getAmount());
                player.setItemOnCursor(cursorItem); // 更新鼠标上的物品
            }
        }
    }

    private int calculateMaxCrafts(CraftingInventory inv) {
        // 对于标准配方，最大合成数等于最小原料堆叠数
        int maxCrafts = Integer.MAX_VALUE;
        for (ItemStack item : inv.getMatrix()) {
            if (item != null && item.getType() != Material.AIR) {
                maxCrafts = Math.min(maxCrafts, item.getAmount());
            }
        }
        return maxCrafts == Integer.MAX_VALUE ? 0 : maxCrafts;
    }

    private void consumeIngredients(CraftingInventory inv, int numCrafts) {
        ItemStack[] matrix = inv.getMatrix();
        for (int i = 0; i < matrix.length; i++) {
            ItemStack item = matrix[i];
            if (item != null && item.getType() != Material.AIR) {
                // 对于每个原料格，消耗 numCrafts 个
                item.setAmount(item.getAmount() - numCrafts);
                if (item.getAmount() <= 0) {
                    matrix[i] = null;
                }
            }
        }
        // 更新合成矩阵
        inv.setMatrix(matrix);
    }

    private boolean isCovalenceDust(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        String id = item.getItemMeta().getPersistentDataContainer().get(Constants.ID_KEY, PersistentDataType.STRING);
        return "low_covalence_dust".equals(id)
                || "medium_covalence_dust".equals(id)
                || "high_covalence_dust".equals(id);
    }
}
