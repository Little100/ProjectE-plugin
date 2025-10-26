package org.Little_100.projecte.managers;

import org.Little_100.projecte.Debug;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.compatibility.version.VersionAdapter;
import org.Little_100.projecte.devices.*;
import org.Little_100.projecte.storage.DatabaseManager;
import org.Little_100.projecte.util.Constants;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;

public class EmcManager {

    private final ProjectE plugin;
    private final DatabaseManager databaseManager;
    private final VersionAdapter versionAdapter;
    private final String recipeConflictStrategy;
    private final String divisionStrategy;
    private final Set<String> currentlyCalculating = new HashSet<>();

    public EmcManager(ProjectE plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.versionAdapter = plugin.getVersionAdapter();
        this.recipeConflictStrategy = plugin.getConfig()
                .getString("gui.EMC.recipeConflictStrategy", "lowest")
                .toLowerCase();
        this.divisionStrategy = plugin.getConfig()
                .getString("gui.EMC.divisionStrategy", "floor")
                .toLowerCase();

        // 确保文件存在
        File customEmcFile = new File(plugin.getDataFolder(), "custommoditememc.yml");
        if (!customEmcFile.exists()) {
            plugin.getLogger().info("custommoditememc.yml not found, creating default file.");
            plugin.saveResource("custommoditememc.yml", false);
        }
    }

    public void calculateAndStoreEmcValues(boolean forceRecalculate) {
        if (!forceRecalculate && databaseManager.hasEmcValues()) {
            plugin.getLogger().info("EMC values already exist in the database. Skipping calculation.");
            return;
        }
        if (forceRecalculate) {
            databaseManager.clearEmcValues();
        }
        plugin.getLogger().info("Start calculating EMC values...");
        versionAdapter.loadInitialEmcValues();

        for (int i = 0; i < 10; i++) {
            plugin.getLogger().info("EMC calculation iteration " + (i + 1) + "...");
            boolean changed = false;
            Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
            while (recipeIterator.hasNext()) {
                try {
                    Recipe recipe = recipeIterator.next();
                    if (calculateEmcForRecipe(recipe)) {
                        changed = true;
                    }
                } catch (Exception e) {
                    // 捕获并忽略损坏的配方
                }
            }
            if (!changed) {
                plugin.getLogger().info("EMC values stabilized, calculation ended early.");
                break;
            }
        }

        // 如果是第一次计算，存储到数据库

        plugin.getLogger().info("EMC value calculation completed.");
    }

    private boolean calculateEmcForRecipe(Recipe recipe) {
        ItemStack result = recipe.getResult();
        if (result.getType().isAir()) {
            return false;
        }


        NamespacedKey key = null;
        if (recipe instanceof ShapedRecipe) {
            key = ((ShapedRecipe) recipe).getKey();
        } else if (recipe instanceof ShapelessRecipe) {
            key = ((ShapelessRecipe) recipe).getKey();
        } else if (recipe instanceof CookingRecipe) {
            key = ((CookingRecipe<?>) recipe).getKey();
        }

        if (key != null && key.getNamespace().equalsIgnoreCase("projecte")) {
            return false;
        }

        String resultKey = getItemKey(result);
        long existingEmc = getEmc(resultKey);
        long newEmc = versionAdapter.calculateRecipeEmc(recipe, divisionStrategy);

        if (newEmc <= 0) {
            return false;
        }

        if (existingEmc <= 0) {
            databaseManager.setEmc(resultKey, newEmc);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("item", resultKey);
            placeholders.put("emc", String.valueOf(newEmc));
            Debug.log("debug.emc.item_emc_info", placeholders);
            return true;
        } else {
            boolean updated = false;
            if ("lowest".equals(recipeConflictStrategy) && newEmc < existingEmc) {
                databaseManager.setEmc(resultKey, newEmc);
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("item", resultKey);
                placeholders.put("emc", String.valueOf(newEmc));
                Debug.log("debug.emc.item_emc_info", placeholders);
                updated = true;
            } else if ("highest".equals(recipeConflictStrategy) && newEmc > existingEmc) {
                databaseManager.setEmc(resultKey, newEmc);
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("item", resultKey);
                placeholders.put("emc", String.valueOf(newEmc));
                Debug.log("debug.emc.item_emc_info", placeholders);
                updated = true;
            }
            return updated;
        }
    }

    public long getEmc(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return 0;
        }

        // 检查物品是否有非附魔的NBT组件（PDC、CustomModelData等）
        if (!hasOnlyEnchantments(item)) {
            // 这是一个自定义物品，使用完整key查询/计算EMC
            String fullKey = getItemKey(item);
            long customEmc = getEmc(fullKey);  // 这会触发递归计算
            if (customEmc > 0) {
                return applyDurabilityModifier(item, customEmc);
            }
            // 如果自定义物品没有EMC值，返回0（不使用底座材料的EMC）
            return 0;
        }

        // 对于纯默认或仅附魔物品，使用基础材料的EMC
        String baseKey = versionAdapter.getItemKey(item);
        long baseEmc = getEmc(baseKey);
        
        if (baseEmc <= 0) {
            return 0;
        }

        // 应用耐久修正
        return applyDurabilityModifier(item, baseEmc);
    }

    public long getEmc(String itemKey) {
        long emc = databaseManager.getEmc(itemKey);
        if (emc > 0) {
            return emc;
        }

        if (currentlyCalculating.contains(itemKey)) {
            return 0;
        }

        currentlyCalculating.add(itemKey);

        long lowestEmc = -1;

        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
        while (recipeIterator.hasNext()) {
            try {
                Recipe recipe = recipeIterator.next();
                ItemStack result = recipe.getResult();

                NamespacedKey key = null;
                if (recipe instanceof ShapedRecipe) {
                    key = ((ShapedRecipe) recipe).getKey();
                } else if (recipe instanceof ShapelessRecipe) {
                    key = ((ShapelessRecipe) recipe).getKey();
                } else if (recipe instanceof CookingRecipe) {
                    key = ((CookingRecipe<?>) recipe).getKey();
                }

                if (key != null && key.getNamespace().equalsIgnoreCase("projecte")) {
                    continue;
                }

                if (getItemKey(result).equals(itemKey)) {
                    long calculatedEmc = versionAdapter.calculateRecipeEmc(recipe, divisionStrategy);
                    if (calculatedEmc > 0) {
                        if (lowestEmc == -1 || calculatedEmc < lowestEmc) {
                            lowestEmc = calculatedEmc;
                        }
                    }
                }
            } catch (Exception e) {
                // 捕获并忽略损坏的配方
            }
        }

        currentlyCalculating.remove(itemKey);

        if (lowestEmc > 0) {
            databaseManager.setEmc(itemKey, lowestEmc);
            return lowestEmc;
        }

        return 0;
    }

    public String getItemKey(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return "minecraft:air";
        }
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                PersistentDataContainer container = meta.getPersistentDataContainer();
                
                // 首先检查ProjectE自己的ID_KEY
                if (container.has(Constants.ID_KEY, PersistentDataType.STRING)) {
                    String projecteId = container.get(Constants.ID_KEY, PersistentDataType.STRING);
                    if ("transmutation_tablet_book".equals(projecteId)) {
                        // Check for CustomModelData to be more specific
                        if (meta.hasCustomModelData() && meta.getCustomModelData() == 1) {
                            return "projecte:" + projecteId;
                        }
                    } else {
                        return "projecte:" + projecteId;
                    }
                }

                if (container.has(Constants.KLEIN_STAR_KEY, PersistentDataType.INTEGER)) {
                    int level = container.get(Constants.KLEIN_STAR_KEY, PersistentDataType.INTEGER);
                    if (level > 0) {
                        String levelName = getLevelName(level);
                        if (levelName != null) {
                            return "projecte:klein_star_" + levelName;
                        }
                    }
                }

                if (container.has(DarkMatterFurnace.KEY, PersistentDataType.BYTE)) {
                    return "projecte:dark_matter_furnace";
                }
                if (container.has(RedMatterFurnace.KEY, PersistentDataType.BYTE)) {
                    return "projecte:red_matter_furnace";
                }
                if (container.has(AlchemicalChest.KEY, PersistentDataType.BYTE)) {
                    return "projecte:alchemical_chest";
                }
                if (container.has(EnergyCondenser.KEY, PersistentDataType.BYTE)) {
                    return "projecte:energy_condenser";
                }
                if (container.has(EnergyCondenserMK2.KEY, PersistentDataType.BYTE)) {
                    return "projecte:energy_condenser_mk2";
                }
                // 检查能量收集器
                if (container.has(EnergyCollector.KEY_MK1, PersistentDataType.BYTE)) {
                    return "projecte:energy_collector_mk1";
                }
                if (container.has(EnergyCollector.KEY_MK2, PersistentDataType.BYTE)) {
                    return "projecte:energy_collector_mk2";
                }
                if (container.has(EnergyCollector.KEY_MK3, PersistentDataType.BYTE)) {
                    return "projecte:energy_collector_mk3";
                }
                
                // 检测其他插件的PDC
                // Detect PDC from other plugins
                Set<NamespacedKey> keys = container.getKeys();
                if (!keys.isEmpty()) {
                    // 尝试找到一个通用的ID key
                    // 优先查找常见的命名模式，如 "xxx:id", "xxx:item_id" 等
                    for (NamespacedKey key : keys) {
                        String keyName = key.getKey().toLowerCase();
                        if (keyName.equals("id") || keyName.equals("item_id") || keyName.equals("custom_id")) {
                            // 尝试以STRING类型获取
                            if (container.has(key, PersistentDataType.STRING)) {
                                String customId = container.get(key, PersistentDataType.STRING);
                                if (customId != null && !customId.isEmpty()) {
                                    return key.getNamespace() + ":" + customId;
                                }
                            }
                        }
                    }
                    
                    // 如果没有找到标准的ID key，使用第一个非ProjectE的key
                    for (NamespacedKey key : keys) {
                        if (!key.getNamespace().equals("projecte")) {
                            // 尝试获取该key的值
                            if (container.has(key, PersistentDataType.STRING)) {
                                String value = container.get(key, PersistentDataType.STRING);
                                if (value != null && !value.isEmpty()) {
                                    return key.toString().replace(":", "_") + "_" + value;
                                }
                            }
                        }
                    }
                }
            }
        }
        return versionAdapter.getItemKey(item);
    }

    public String getEffectiveItemKey(ItemStack item) {
        // 根据物品状态返回合适的key
        if (hasOnlyEnchantments(item)) {
            // 仅附魔物品返回基础key
            return versionAdapter.getItemKey(item);
        }
        // 有PDC或其他组件返回完整key
        return getItemKey(item);
    }

    public String getSpecificItemKey(ItemStack item) {
        return getItemKey(item);
    }

    public boolean isPdcItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return !container.getKeys().isEmpty();
    }

    /**
     * 检查物品是否只有附魔（没有PDC、CustomModelData等其他组件）
     * Check if the item has only enchantments (no PDC, CustomModelData, etc.)
     */
    private boolean hasOnlyEnchantments(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return true; // 纯默认物品
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return true;
        }
        
        // 检查是否有PDC
        if (!meta.getPersistentDataContainer().getKeys().isEmpty()) {
            return false;
        }
        
        // 检查是否有CustomModelData
        if (meta.hasCustomModelData()) {
            return false;
        }
        
        // 检查是否有自定义名称
        if (meta.hasDisplayName()) {
            return false;
        }
        
        // 检查是否有Lore
        if (meta.hasLore()) {
            return false;
        }
        
        // 如果只有附魔（或什么都没有），返回true
        return true;
    }

    /**
     * 根据物品耐久度应用EMC修正
     * Apply EMC modifier based on item durability
     */
    private long applyDurabilityModifier(ItemStack item, long baseEmc) {
        if (item == null || !item.hasItemMeta()) {
            return baseEmc;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return baseEmc;
        }
        
        // 检查物品是否有耐久度
        if (meta instanceof org.bukkit.inventory.meta.Damageable) {
            org.bukkit.inventory.meta.Damageable damageable = (org.bukkit.inventory.meta.Damageable) meta;
            
            // 获取最大耐久度
            int maxDurability = item.getType().getMaxDurability();
            if (maxDurability > 0 && damageable.hasDamage()) {
                int damage = damageable.getDamage();
                int currentDurability = maxDurability - damage;
                
                // 计算耐久百分比
                double durabilityPercent = (double) currentDurability / maxDurability;
                
                // 应用耐久修正
                return (long) (baseEmc * durabilityPercent);
            }
        }
        
        return baseEmc;
    }

    private String getLevelName(int level) {
        switch (level) {
            case 1:
                return "ein";
            case 2:
                return "zwei";
            case 3:
                return "drei";
            case 4:
                return "vier";
            case 5:
                return "sphere";
            case 6:
                return "omega";
            default:
                return null;
        }
    }

    public long getBaseEmc(ItemStack item) {
        String baseKey = versionAdapter.getItemKey(item);
        return getEmc(baseKey);
    }

    public String getPdcId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey idKey = Constants.ID_KEY;
        return container.get(idKey, PersistentDataType.STRING);
    }

    public void registerEmc(String itemKey, long emcValue) {
        databaseManager.setEmc(itemKey, emcValue);
    }

    public void setEmcValue(ItemStack item, long emc) {
        if (item == null) return;
        String key = getItemKey(item);
        databaseManager.setEmc(key, emc);
    }

    /**
     * 重新计算原版物品的EMC值（不包括PDC物品）
     * Recalculate EMC values for default items (excluding PDC items)
     */
    public void recalculateDefaultEmcValues() {
        plugin.getLogger().info("Start calculating default items EMC values...");
        
        // 重新加载基础EMC值
        versionAdapter.loadInitialEmcValues();

        for (int i = 0; i < 10; i++) {
            plugin.getLogger().info("Default EMC calculation iteration " + (i + 1) + "...");
            boolean changed = false;
            Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
            while (recipeIterator.hasNext()) {
                try {
                    Recipe recipe = recipeIterator.next();
                    
                    // 跳过PDC物品
                    if (recipe.getResult() != null && isPdcItem(recipe.getResult())) {
                        continue;
                    }
                    
                    if (calculateEmcForRecipe(recipe)) {
                        changed = true;
                    }
                } catch (Exception e) {
                    // 捕获并忽略损坏的配方
                }
            }
            if (!changed) {
                plugin.getLogger().info("Default EMC values stabilized, calculation ended early.");
                break;
            }
        }

        plugin.getLogger().info("Default items EMC value calculation completed.");
    }

    /**
     * 重新计算PDC物品的EMC值
     * Recalculate EMC values for PDC items
     * @return 计算的唯一物品数量
     */
    public int recalculatePdcEmcValues() {
        plugin.getLogger().info("Start calculating PDC items EMC values...");
        
        boolean debug = plugin.getConfig().getBoolean("debug");
        int pdcRecipesFound = 0;
        Set<String> calculatedItems = new HashSet<>();

        // 先统计有多少PDC配方
        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
        while (recipeIterator.hasNext()) {
            try {
                Recipe recipe = recipeIterator.next();
                if (recipe.getResult() != null && !recipe.getResult().getType().isAir() 
                    && isPdcItem(recipe.getResult())) {
                    pdcRecipesFound++;
                }
            } catch (Exception e) {
                // 忽略损坏的配方
            }
        }

        if (pdcRecipesFound == 0) {
            plugin.getLogger().info("No PDC recipes found. Skipping PDC EMC calculation.");
            return 0;
        }

        plugin.getLogger().info("Found " + pdcRecipesFound + " PDC recipes. Starting EMC calculation...");

        // 进行额外的迭代计算来处理PDC物品
        for (int i = 0; i < 5; i++) {
            plugin.getLogger().info("PDC EMC calculation iteration " + (i + 1) + "...");
            boolean changed = false;
            
            currentlyCalculating.clear();
            
            recipeIterator = Bukkit.recipeIterator();
            while (recipeIterator.hasNext()) {
                try {
                    Recipe recipe = recipeIterator.next();
                    if (recipe.getResult() == null || recipe.getResult().getType().isAir()) {
                        continue;
                    }

                    // 只处理PDC物品
                    if (isPdcItem(recipe.getResult())) {
                        String itemKey = getItemKey(recipe.getResult());
                        long oldEmc = databaseManager.getEmc(itemKey);
                        
                        // 计算配方的EMC
                        long recipeEmc = versionAdapter.calculateRecipeEmc(recipe, divisionStrategy);
                        
                        if (debug && recipeEmc == 0 && oldEmc == 0) {
                            // 调试：显示为什么无法计算EMC
                            plugin.getLogger().warning("[PDC EMC Debug] Cannot calculate EMC for: " + itemKey);
                            plugin.getLogger().warning("  Recipe type: " + recipe.getClass().getSimpleName());
                            
                            // 显示原料信息
                            if (recipe instanceof ShapedRecipe) {
                                plugin.getLogger().warning("  Ingredients:");
                                ShapedRecipe shaped = (ShapedRecipe) recipe;
                                for (ItemStack ingredient : shaped.getIngredientMap().values()) {
                                    if (ingredient != null && !ingredient.getType().isAir()) {
                                        String ingKey = getItemKey(ingredient);
                                        long ingEmc = databaseManager.getEmc(ingKey);
                                        plugin.getLogger().warning("    - " + ingKey + ": " + ingEmc + " EMC");
                                    }
                                }
                            } else if (recipe instanceof ShapelessRecipe) {
                                plugin.getLogger().warning("  Ingredients:");
                                ShapelessRecipe shapeless = (ShapelessRecipe) recipe;
                                for (ItemStack ingredient : shapeless.getIngredientList()) {
                                    if (ingredient != null && !ingredient.getType().isAir()) {
                                        String ingKey = getItemKey(ingredient);
                                        long ingEmc = databaseManager.getEmc(ingKey);
                                        plugin.getLogger().warning("    - " + ingKey + ": " + ingEmc + " EMC");
                                    }
                                }
                            }
                        }
                        
                        if (recipeEmc > 0) {
                            // 根据策略更新EMC
                            boolean shouldUpdate = false;
                            
                            if (oldEmc <= 0) {
                                // 还没有EMC值，直接设置
                                shouldUpdate = true;
                            } else if ("lowest".equals(recipeConflictStrategy) && recipeEmc < oldEmc) {
                                // 策略是最低值，且新值更低
                                shouldUpdate = true;
                            } else if ("highest".equals(recipeConflictStrategy) && recipeEmc > oldEmc) {
                                // 策略是最高值，且新值更高
                                shouldUpdate = true;
                            }
                            
                            if (shouldUpdate) {
                                databaseManager.setEmc(itemKey, recipeEmc);
                                changed = true;
                                calculatedItems.add(itemKey);
                                if (debug) {
                                    plugin.getLogger().info("[PDC EMC Calculated] " + itemKey + " = " + recipeEmc + " EMC");
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // 忽略损坏的配方
                }
            }
            
            if (!changed) {
                plugin.getLogger().info("PDC EMC values stabilized, calculation ended early.");
                break;
            }
        }

        plugin.getLogger().info("PDC items EMC calculation completed. " + calculatedItems.size() + " unique items calculated.");
        
        // 报告未计算的PDC物品
        int uncalculatedCount = 0;
        recipeIterator = Bukkit.recipeIterator();
        while (recipeIterator.hasNext()) {
            try {
                Recipe recipe = recipeIterator.next();
                if (recipe.getResult() != null && !recipe.getResult().getType().isAir() 
                    && isPdcItem(recipe.getResult())) {
                    String itemKey = getItemKey(recipe.getResult());
                    long emc = databaseManager.getEmc(itemKey);
                    if (emc <= 0) {
                        uncalculatedCount++;
                        if (debug) {
                            plugin.getLogger().warning("[PDC EMC] Item without EMC: " + itemKey);
                        }
                    }
                }
            } catch (Exception e) {
                // 忽略
            }
        }
        
        if (uncalculatedCount > 0) {
            plugin.getLogger().warning("Warning: " + uncalculatedCount + " PDC items still don't have EMC values.");
            plugin.getLogger().warning("This may be due to missing ingredient EMC or circular dependencies.");
            if (!debug) {
                plugin.getLogger().warning("Enable debug mode in config.yml to see which items are affected.");
            }
        }
        
        return calculatedItems.size();
    }
}
