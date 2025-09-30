package org.Little_100.projecte.gui;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PdcItemDebugGUI {

    private final ProjectE plugin;
    private final Player player;
    private final List<ItemStack> pdcItems;
    private int page;

    public PdcItemDebugGUI(ProjectE plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.pdcItems = new ArrayList<>();
        this.page = 0;
        
        scanPdcItems();
    }

    private void scanPdcItems() {
        Set<String> addedKeys = new HashSet<>();
        
        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
        while (recipeIterator.hasNext()) {
            try {
                Recipe recipe = recipeIterator.next();
                if (recipe.getResult() != null && !recipe.getResult().getType().isAir()) {
                    ItemStack result = recipe.getResult();
                    if (plugin.getEmcManager().isPdcItem(result)) {
                        String itemKey = plugin.getEmcManager().getItemKey(result);
                        if (!addedKeys.contains(itemKey)) {
                            addedKeys.add(itemKey);
                            pdcItems.add(result.clone());
                        }
                    }
                }
            } catch (Exception e) {
                // 忽略损坏的配方
            }
        }
        
        pdcItems.sort((a, b) -> {
            String keyA = plugin.getEmcManager().getItemKey(a);
            String keyB = plugin.getEmcManager().getItemKey(b);
            return keyA.compareTo(keyB);
        });
    }

    public void open() {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "PDC物品调试 - 页 " + (page + 1));

        int startIndex = page * 45;
        int endIndex = Math.min(startIndex + 45, pdcItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            ItemStack item = pdcItems.get(i).clone();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String itemKey = plugin.getEmcManager().getItemKey(item);
                long emc = plugin.getDatabaseManager().getEmc(itemKey);
                
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Key: " + ChatColor.YELLOW + itemKey);
                lore.add(ChatColor.GRAY + "EMC: " + ChatColor.GREEN + (emc > 0 ? emc : ChatColor.RED + "未计算"));
                lore.add("");
                lore.add(ChatColor.GOLD + "点击查看详细信息");
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            gui.setItem(i - startIndex, item);
        }

        if (page > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta meta = prevPage.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "上一页");
            prevPage.setItemMeta(meta);
            gui.setItem(48, prevPage);
        }

        if (endIndex < pdcItems.size()) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta meta = nextPage.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "下一页");
            nextPage.setItemMeta(meta);
            gui.setItem(50, nextPage);
        }

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta meta = close.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "关闭");
        close.setItemMeta(meta);
        gui.setItem(49, close);

        player.openInventory(gui);
    }

    public void nextPage() {
        if ((page + 1) * 45 < pdcItems.size()) {
            page++;
            open();
        }
    }

    public void previousPage() {
        if (page > 0) {
            page--;
            open();
        }
    }

    public void showItemDebug(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return;
        }

        String itemKey = plugin.getEmcManager().getItemKey(item);
        long emc = plugin.getDatabaseManager().getEmc(itemKey);

        // 发送给玩家
        player.sendMessage(ChatColor.GOLD + "========== PDC物品调试信息 ==========");
        player.sendMessage(ChatColor.YELLOW + "物品Key: " + ChatColor.WHITE + itemKey);
        player.sendMessage(ChatColor.YELLOW + "当前EMC: " + ChatColor.WHITE + (emc > 0 ? emc : ChatColor.RED + "未计算"));
        
        // 输出到控制台
        plugin.getLogger().info("========== PDC Item Debug ==========");
        plugin.getLogger().info("Item Key: " + itemKey);
        plugin.getLogger().info("Current EMC: " + emc);
        plugin.getLogger().info("Item Type: " + item.getType());
        plugin.getLogger().info("Has ItemMeta: " + item.hasItemMeta());
        
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            plugin.getLogger().info("Has DisplayName: " + meta.hasDisplayName());
            plugin.getLogger().info("Has CustomModelData: " + meta.hasCustomModelData());
            plugin.getLogger().info("PDC Keys: " + meta.getPersistentDataContainer().getKeys());
        }

        // 查找所有相关配方
        player.sendMessage(ChatColor.YELLOW + "相关配方:");
        plugin.getLogger().info("Related Recipes:");
        
        int recipeCount = 0;
        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
        while (recipeIterator.hasNext()) {
            try {
                Recipe recipe = recipeIterator.next();
                if (recipe.getResult() == null) continue;
                
                String resultKey = plugin.getEmcManager().getItemKey(recipe.getResult());
                if (resultKey.equals(itemKey)) {
                    recipeCount++;
                    showRecipeDebug(recipe, recipeCount);
                }
            } catch (Exception e) {
                // 忽略损坏的配方
            }
        }

        if (recipeCount == 0) {
            player.sendMessage(ChatColor.RED + "  未找到任何配方！");
            plugin.getLogger().warning("  No recipes found!");
        }

        player.sendMessage(ChatColor.GOLD + "===================================");
        plugin.getLogger().info("====================================");
    }

    private void showRecipeDebug(Recipe recipe, int index) {
        player.sendMessage(ChatColor.AQUA + "配方 #" + index + " (" + recipe.getClass().getSimpleName() + ")");
        plugin.getLogger().info("Recipe #" + index + " (" + recipe.getClass().getSimpleName() + ")");

        // 计算配方EMC
        String divisionStrategy = plugin.getConfig().getString("TransmutationTable.EMC.divisionStrategy", "floor");
        long calculatedEmc = plugin.getVersionAdapter().calculateRecipeEmc(recipe, divisionStrategy);
        
        player.sendMessage(ChatColor.YELLOW + "  计算出的EMC: " + ChatColor.WHITE + 
            (calculatedEmc > 0 ? calculatedEmc : ChatColor.RED + "0 (无法计算)"));
        plugin.getLogger().info("  Calculated EMC: " + calculatedEmc);

        // 显示原料
        player.sendMessage(ChatColor.YELLOW + "  原料:");
        plugin.getLogger().info("  Ingredients:");

        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shaped = (ShapedRecipe) recipe;
            for (Map.Entry<Character, ItemStack> entry : shaped.getIngredientMap().entrySet()) {
                if (entry.getValue() != null && !entry.getValue().getType().isAir()) {
                    showIngredientDebug(entry.getValue());
                }
            }
        } else if (recipe instanceof ShapelessRecipe) {
            ShapelessRecipe shapeless = (ShapelessRecipe) recipe;
            for (ItemStack ingredient : shapeless.getIngredientList()) {
                if (ingredient != null && !ingredient.getType().isAir()) {
                    showIngredientDebug(ingredient);
                }
            }
        }
    }

    private void showIngredientDebug(ItemStack ingredient) {
        String ingKey = plugin.getEmcManager().getItemKey(ingredient);
        String baseKey = plugin.getVersionAdapter().getItemKey(ingredient);
        long ingEmc = plugin.getDatabaseManager().getEmc(ingKey);
        boolean isPdc = plugin.getEmcManager().isPdcItem(ingredient);
        
        String message = ChatColor.GRAY + "    - " + ingredient.getType() + " x" + ingredient.getAmount() + 
            ChatColor.YELLOW + " [" + (isPdc ? "PDC" : "原版") + "]";
        
        player.sendMessage(message);
        player.sendMessage(ChatColor.GRAY + "      Key: " + ChatColor.WHITE + ingKey);
        if (!ingKey.equals(baseKey)) {
            player.sendMessage(ChatColor.GRAY + "      Base Key: " + ChatColor.WHITE + baseKey);
        }
        player.sendMessage(ChatColor.GRAY + "      EMC: " + ChatColor.WHITE + 
            (ingEmc > 0 ? ingEmc : ChatColor.RED + "0 (未设置)"));
        
        plugin.getLogger().info("    - " + ingredient.getType() + " x" + ingredient.getAmount() + 
            " [" + (isPdc ? "PDC" : "Vanilla") + "]");
        plugin.getLogger().info("      Key: " + ingKey);
        if (!ingKey.equals(baseKey)) {
            plugin.getLogger().info("      Base Key: " + baseKey);
        }
        plugin.getLogger().info("      EMC: " + ingEmc);
    }
}
