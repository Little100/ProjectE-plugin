package org.Little_100.projecte;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.HashMap;
import java.util.Map;

// 管理贤者之石相关的合成配方
public class RecipeManager {

    private final ProjectE plugin;
    private final Map<String, NamespacedKey> recipeKeys = new HashMap<>();

    public RecipeManager(ProjectE plugin) {
        this.plugin = plugin;
    }

    // 注册所有配方，包括贤者之石本身和矿物转换
    public void registerAllRecipes() {
        // 注册贤者之石配方
        registerPhilosopherStoneRecipe();

        // 注册升级配方
        registerUpgradeRecipes();

        // 注册降级配方
        registerDowngradeRecipes();

        // 注册转换桌配方
        registerTransmutationTableRecipes();

        // 注册矿物冶炼配方
        registerOreTransmutationRecipes();
    }

    // 注册转换桌的配方
    private void registerTransmutationTableRecipes() {
        Map<String, NamespacedKey> newKeys = plugin.getVersionAdapter().registerTransmutationTableRecipes();
        recipeKeys.putAll(newKeys);
    }
    
    // 注册矿物升级配方
    private void registerUpgradeRecipes() {
        ItemStack philosopherStone = plugin.getPhilosopherStone();
        // 煤炭 -> 铜锭
        registerUpgradeRecipe("coal_to_copper", Material.COAL, Material.COPPER_INGOT, 4, 1, philosopherStone);

        // 铜锭 -> 铁锭
        registerUpgradeRecipe("copper_to_iron", Material.COPPER_INGOT, Material.IRON_INGOT, 4, 1, philosopherStone);

        // 铁锭 -> 金锭
        registerUpgradeRecipe("iron_to_gold", Material.IRON_INGOT, Material.GOLD_INGOT, 4, 1, philosopherStone);

        // 金锭 -> 钻石
        registerUpgradeRecipe("gold_to_diamond", Material.GOLD_INGOT, Material.DIAMOND, 4, 1, philosopherStone);

        // 钻石块 -> 下界合金锭 (特殊配方：8个钻石块)
        registerSpecialUpgradeRecipe("diamond_to_netherite", Material.DIAMOND_BLOCK, Material.NETHERITE_INGOT, 8, 1, philosopherStone);
    }
    
    // 注册矿物降级配方
    private void registerDowngradeRecipes() {
        ItemStack philosopherStone = plugin.getPhilosopherStone();
        // 铜锭 -> 煤炭
        registerDowngradeRecipe("copper_to_coal", Material.COPPER_INGOT, Material.COAL, 1, 4, philosopherStone);

        // 铁锭 -> 铜锭
        registerDowngradeRecipe("iron_to_copper", Material.IRON_INGOT, Material.COPPER_INGOT, 1, 4, philosopherStone);

        // 金锭 -> 铁锭
        registerDowngradeRecipe("gold_to_iron", Material.GOLD_INGOT, Material.IRON_INGOT, 1, 4, philosopherStone);

        // 钻石 -> 金锭
        registerDowngradeRecipe("diamond_to_gold", Material.DIAMOND, Material.GOLD_INGOT, 1, 4, philosopherStone);

        // 下界合金锭 -> 钻石块
        registerDowngradeRecipe("netherite_to_diamond", Material.NETHERITE_INGOT, Material.DIAMOND_BLOCK, 1, 8, philosopherStone);
    }
    
    // 注册标准的升级配方
    private void registerUpgradeRecipe(String id, Material input, Material output, int inputAmount, int outputAmount, ItemStack catalyst) {
        NamespacedKey key = new NamespacedKey(plugin, "upgrade_" + id);
        ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(output, outputAmount));

        // 添加材料
        recipe.addIngredient(inputAmount, input);

        // 添加贤者之石作为转换物品
        recipe.addIngredient(catalyst.getType());

        // 注册配方
        Bukkit.addRecipe(recipe);
        recipeKeys.put("upgrade_" + id, key);
    }
    
    // 注册特殊的升级配方（钻石块到下界合金）
    private void registerSpecialUpgradeRecipe(String id, Material input, Material output, int inputAmount, int outputAmount, ItemStack catalyst) {
        NamespacedKey key = new NamespacedKey(plugin, "special_" + id);
        ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(output, outputAmount));

        // 添加材料
        recipe.addIngredient(inputAmount, input);

        // 添加贤者之石作为转换物品
        recipe.addIngredient(catalyst.getType());

        // 注册配方
        Bukkit.addRecipe(recipe);
        recipeKeys.put("special_" + id, key);
    }
    
    // 注册标准的降级配方
    private void registerDowngradeRecipe(String id, Material input, Material output, int inputAmount, int outputAmount, ItemStack catalyst) {
        NamespacedKey key = new NamespacedKey(plugin, "downgrade_" + id);
        ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(output, outputAmount));

        // 添加材料
        recipe.addIngredient(inputAmount, input);

        // 添加贤者之石作为转换物品
        recipe.addIngredient(catalyst.getType());

        // 注册配方
        Bukkit.addRecipe(recipe);
        recipeKeys.put("downgrade_" + id, key);
    }

    // 注册贤者之石本身的合成配方
    private void registerPhilosopherStoneRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "philosopher_stone");
        ItemStack philosopherStone = plugin.getPhilosopherStone();

        ShapedRecipe recipe = new ShapedRecipe(key, philosopherStone);
        recipe.shape("RGR", "GDG", "RGR");
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('G', Material.GLOWSTONE_DUST);
        recipe.setIngredient('D', Material.DIAMOND);

        Bukkit.addRecipe(recipe);
        recipeKeys.put("philosopher_stone", key);
    }
    
    // 获取所有配方的键
    public Map<String, NamespacedKey> getRecipeKeys() {
        return recipeKeys;
    }
    
    // 卸载所有配方
    public void unregisterAllRecipes() {
        for (NamespacedKey key : recipeKeys.values()) {
            Bukkit.removeRecipe(key);
        }
        recipeKeys.clear();
    }

    // 注册矿物冶炼配方 (例如: 7个铁矿石(支持混合 深层 普通 粗矿全部可以冶炼) + 煤炭/木炭 + 贤者之石 -> 7个铁锭)
    private void registerOreTransmutationRecipes() {
        // 定义转换物品
        org.bukkit.inventory.RecipeChoice catalyst = new org.bukkit.inventory.RecipeChoice.ExactChoice(plugin.getPhilosopherStone());
        org.bukkit.inventory.RecipeChoice fuel = new org.bukkit.inventory.RecipeChoice.MaterialChoice(Material.COAL, Material.CHARCOAL);

        // 铁
        registerOreTransmutationRecipe("iron",
                new org.bukkit.inventory.RecipeChoice.MaterialChoice(Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE, Material.RAW_IRON),
                new ItemStack(Material.IRON_INGOT, 7),
                catalyst, fuel);

        // 金
        registerOreTransmutationRecipe("gold",
                new org.bukkit.inventory.RecipeChoice.MaterialChoice(Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.RAW_GOLD),
                new ItemStack(Material.GOLD_INGOT, 7),
                catalyst, fuel);

        // 铜
        registerOreTransmutationRecipe("copper",
                new org.bukkit.inventory.RecipeChoice.MaterialChoice(Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE, Material.RAW_COPPER),
                new ItemStack(Material.COPPER_INGOT, 7),
                catalyst, fuel);

        // 钻石 (产物是钻石本身)
        registerOreTransmutationRecipe("diamond",
                new org.bukkit.inventory.RecipeChoice.MaterialChoice(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE),
                new ItemStack(Material.DIAMOND, 7),
                catalyst, fuel);

        // 下界合金 (输入远古残骸, 输出下界合金碎片)
        registerOreTransmutationRecipe("netherite",
                new org.bukkit.inventory.RecipeChoice.MaterialChoice(Material.ANCIENT_DEBRIS),
                new ItemStack(Material.NETHERITE_SCRAP, 7),
                catalyst, fuel);
    }

    private void registerOreTransmutationRecipe(String id, org.bukkit.inventory.RecipeChoice oreChoice, ItemStack result, org.bukkit.inventory.RecipeChoice catalyst, org.bukkit.inventory.RecipeChoice fuel) {
        NamespacedKey key = new NamespacedKey(plugin, "transmute_" + id);
        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        for (int i = 0; i < 7; i++) {
            recipe.addIngredient(oreChoice);
        }
        recipe.addIngredient(fuel);
        recipe.addIngredient(catalyst);

        Bukkit.addRecipe(recipe);
        recipeKeys.put("transmute_" + id, key);
    }
}