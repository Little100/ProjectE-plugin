package org.Little_100.projecte;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.HashMap;

public class CraftingListener implements Listener {

    private final ProjectE plugin;

    public CraftingListener(ProjectE plugin) {
        this.plugin = plugin;
        Bukkit.getLogger().info("[ProjectE] CraftingListener initialized - Final Version");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Recipe recipe = event.getRecipe();
        if (recipe == null) {
            return;
        }

        CraftingInventory inventory = event.getInventory();
        
        boolean hasPhilosopherStone = false;
        for (ItemStack item : inventory.getMatrix()) {
            if (item != null && plugin.isPhilosopherStone(item)) {
                hasPhilosopherStone = true;
                break;
            }
        }
        
        if (!hasPhilosopherStone) {
            return;
        }
        
        event.setCancelled(true);
        
        ItemStack result = recipe.getResult().clone();
        plugin.getLogger().info("贤者之石合成: " + result.getType() + " x" + result.getAmount());
        
        if (event.isShiftClick()) {
            int maxCrafts = calculateMaxCrafts(inventory);
            plugin.getLogger().info("批量合成，最大次数: " + maxCrafts);
            
            if (maxCrafts <= 0) {
                plugin.getLogger().info("无法进行批量合成: 材料不足");
                return;
            }
            
            int totalAmount = result.getAmount() * maxCrafts;
            result.setAmount(totalAmount);
            plugin.getLogger().info("合成总量: " + totalAmount);
            
            consumeIngredients(inventory, maxCrafts);
        } else {
            plugin.getLogger().info("单次合成: " + result.getType());
            
            consumeIngredients(inventory, 1);
        }
        
        plugin.getLogger().info("将物品添加到玩家背包: " + result.getType() + " x" + result.getAmount());
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(result);
        
        if (!leftover.isEmpty()) {
            plugin.getLogger().info("背包已满，部分物品掉落在地上");
            for (ItemStack item : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
        
        player.updateInventory();
    }
    
    private void consumeIngredients(CraftingInventory inventory, int times) {
        if (times <= 0) {
            plugin.getLogger().warning("尝试消耗0或负数材料，操作被取消");
            return;
        }
        
        ItemStack[] matrix = inventory.getMatrix();
        
        plugin.getLogger().info("消耗材料，次数: " + times);
        
        StringBuilder before = new StringBuilder("消耗前材料: ");
        for (ItemStack item : matrix) {
            if (item != null) {
                before.append(item.getType()).append("x").append(item.getAmount()).append(", ");
            } else {
                before.append("空, ");
            }
        }
        plugin.getLogger().info(before.toString());
        
        for (int i = 0; i < matrix.length; i++) {
            ItemStack item = matrix[i];
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            
            // 保留贤者之石
            if (plugin.isPhilosopherStone(item)) {
                plugin.getLogger().info("保留贤者之石在位置 " + i);
                continue;
            }
            
            int newAmount = item.getAmount() - times;
            plugin.getLogger().info("材料 " + item.getType() + " 在位置 " + i + ": " + item.getAmount() + " -> " + (newAmount > 0 ? newAmount : "消耗完"));
            
            if (newAmount <= 0) {
                matrix[i] = null;
            } else {
                item.setAmount(newAmount);
            }
        }
        
        StringBuilder after = new StringBuilder("消耗后材料: ");
        for (ItemStack item : matrix) {
            if (item != null) {
                after.append(item.getType()).append("x").append(item.getAmount()).append(", ");
            } else {
                after.append("空, ");
            }
        }
        plugin.getLogger().info(after.toString());
        
        try {
            inventory.setMatrix(matrix);
            plugin.getLogger().info("合成格已更新");
        } catch (Exception e) {
            plugin.getLogger().severe("更新合成格失败: " + e.getMessage());
            e.printStackTrace();
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