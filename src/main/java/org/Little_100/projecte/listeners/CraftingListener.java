package org.Little_100.projecte.listeners;

import org.Little_100.projecte.Debug;
import org.Little_100.projecte.ProjectE;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.logging.Level;

public class CraftingListener implements Listener {

    private final ProjectE plugin;

    public CraftingListener(ProjectE plugin) {
        this.plugin = plugin;
        Bukkit.getLogger().info("[ProjectE] CraftingListener initialized - Final Version");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCraftItem(InventoryClickEvent event) {
        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;
        if (!(event.getInventory() instanceof CraftingInventory)) return;
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        CraftingInventory inventory = (CraftingInventory) event.getInventory();
        
        // 检查是否有贤者之石
        boolean hasPhilosopherStone = false;
        for (ItemStack item : inventory.getMatrix()) {
            if (plugin.isPhilosopherStone(item)) {
                hasPhilosopherStone = true;
                break;
            }
        }

        if (!hasPhilosopherStone) {
            return;
        }

        ItemStack result = inventory.getResult();
        if (result == null || result.getType() == Material.AIR) {
            return;
        }

        result = result.clone();
        Debug.log("贤者之石合成: " + result.getType() + " x" + result.getAmount());
        
        event.setCancelled(true);

        if (event.isShiftClick()) {
            handleShiftClick(player, inventory, result);
        } else {
            handleNormalClick(player, inventory, result);
        }
    }

    private void handleShiftClick(Player player, CraftingInventory inventory, ItemStack result) {
        int maxCrafts = calculateMaxCrafts(inventory);
        Debug.log("批量合成，最大次数: " + maxCrafts);

        if (maxCrafts <= 0) {
            Debug.log("无法进行批量合成: 材料不足");
            return;
        }

        // 复制结果物品并设置正确的数量
        ItemStack resultStack = result.clone();
        resultStack.setAmount(result.getAmount() * maxCrafts);
        Debug.log("合成总量: " + resultStack.getAmount());

        // 将合成的物品添加到玩家背包
        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(resultStack);

        // 计算实际成功合成的数量
        int craftedAmount = resultStack.getAmount();
        if (!leftovers.isEmpty()) {
            Debug.log("背包已满，部分物品掉落在地上");
            for (ItemStack item : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            craftedAmount -= leftovers.values().stream().mapToInt(ItemStack::getAmount).sum();
        }
        
        int numCrafted = craftedAmount / result.getAmount();
        Debug.log("实际合成次数: " + numCrafted);

        // 根据实际成功合成的数量消耗原料
        if (numCrafted > 0) {
            consumeIngredients(inventory, numCrafted);
        }
    }

    private void handleNormalClick(Player player, CraftingInventory inventory, ItemStack result) {
        Debug.log("单次合成: " + result.getType() + " x" + result.getAmount());

        ItemStack cursorItem = player.getItemOnCursor();

        // 如果鼠标上没有物品，直接放置到鼠标上
        if (cursorItem.getType() == Material.AIR) {
            Debug.log("鼠标为空，将合成结果放置到鼠标上");
            consumeIngredients(inventory, 1);
            player.setItemOnCursor(result);
        }
        // 如果鼠标上有同类物品，并且可以堆叠
        else if (cursorItem.isSimilar(result)) {
            int canAdd = result.getMaxStackSize() - cursorItem.getAmount();
            if (canAdd >= result.getAmount()) {
                Debug.log("鼠标上有同类物品，堆叠到鼠标上");
                consumeIngredients(inventory, 1);
                cursorItem.setAmount(cursorItem.getAmount() + result.getAmount());
                player.setItemOnCursor(cursorItem); // 更新鼠标上的物品
            } else {
                Debug.log("鼠标上的物品无法完全堆叠，不进行合成");
                // 无法完全堆叠，不消耗材料，不进行合成
            }
        } else {
            Debug.log("鼠标上有不同类型的物品，无法进行合成");
            // 鼠标上有不同类型的物品，无法进行合成
        }
    }

    private void consumeIngredients(CraftingInventory inventory, int times) {
        if (times <= 0) {
            plugin.getLogger().warning("尝试消耗0或负数材料，操作被取消");
            return;
        }

        ItemStack[] matrix = inventory.getMatrix();

        Debug.log("消耗材料，次数: " + times);

        StringBuilder before = new StringBuilder("消耗前材料: ");
        for (ItemStack item : matrix) {
            if (item != null) {
                before.append(item.getType())
                        .append("x")
                        .append(item.getAmount())
                        .append(", ");
            } else {
                before.append("空, ");
            }
        }
        Debug.log(before.toString());

        for (int i = 0; i < matrix.length; i++) {
            ItemStack item = matrix[i];
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            // 保留贤者之石
            if (plugin.isPhilosopherStone(item)) {
                Debug.log("保留贤者之石在位置 " + i);
                continue;
            }

            int newAmount = item.getAmount() - times;
            plugin.getLogger()
                    .info("材料 " + item.getType() + " 在位置 " + i + ": " + item.getAmount() + " -> "
                            + (newAmount > 0 ? newAmount : "消耗完"));

            if (newAmount <= 0) {
                matrix[i] = null;
            } else {
                item.setAmount(newAmount);
            }
        }

        StringBuilder after = new StringBuilder("消耗后材料: ");
        for (ItemStack item : matrix) {
            if (item != null) {
                after.append(item.getType())
                        .append("x")
                        .append(item.getAmount())
                        .append(", ");
            } else {
                after.append("空, ");
            }
        }
        Debug.log(after.toString());

        try {
            inventory.setMatrix(matrix);
            Debug.log("合成格已更新");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "更新合成格失败", e);
        }
    }

    private int calculateMaxCrafts(CraftingInventory inventory) {
        int maxCrafts = Integer.MAX_VALUE;

        for (ItemStack item : inventory.getMatrix()) {
            if (item == null || item.getType() == Material.AIR || plugin.isPhilosopherStone(item)) {
                continue;
            }

            maxCrafts = Math.min(maxCrafts, item.getAmount());
        }

        return maxCrafts == Integer.MAX_VALUE ? 0 : maxCrafts;
    }
}
