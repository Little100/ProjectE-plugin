package org.Little_100.projecte.managers;

import org.Little_100.projecte.alchemicalbag.AlchemicalBagManager;
import org.Little_100.projecte.Debug;
import org.Little_100.projecte.ProjectE;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeManager {

    private final ProjectE plugin;
    private final Map<String, NamespacedKey> recipeKeys = new HashMap<>();;

    public RecipeManager(ProjectE plugin) {
        this.plugin = plugin;
    }

    public void registerAllRecipes() {
        loadRecipesFromYaml();
        loadOpItemRecipes();
        registerUpgradeRecipes();
        registerDowngradeRecipes();
        registerOreTransmutationRecipes();
        loadDeviceRecipes();
        registerSpecialFuelRecipes();
    }

    private void loadDeviceRecipes() {
        File deviceFile = new File(plugin.getDataFolder(), "devices.yml");
        if (!deviceFile.exists()) {
            plugin.saveResource("devices.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(deviceFile);
        ConfigurationSection devicesSection = config.getConfigurationSection("devices");
        if (devicesSection == null) {
            return;
        }

        for (String id : devicesSection.getKeys(false)) {
            ConfigurationSection recipeConfig = devicesSection.getConfigurationSection(id);
            if (recipeConfig != null && recipeConfig.getBoolean("enabled", true)) {
                parseRecipe(id, recipeConfig);
            }
        }
        plugin.getLogger().info("devices.yml loaded");
    }

    // 从 recipe.yml 加载所有配方
    private void loadRecipesFromYaml() {
        File recipeFile = new File(plugin.getDataFolder(), "recipe.yml");
        if (!recipeFile.exists()) {
            plugin.getLogger().info("未找到 recipe.yml，正在创建默认文件。");
            plugin.saveResource("recipe.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(recipeFile);
        ConfigurationSection recipesSection = config.getConfigurationSection("recipes");
        if (recipesSection == null) {
            return;
        }

        for (String id : recipesSection.getKeys(false)) {
            if (recipesSection.isList(id)) {
                List<Map<?, ?>> recipeList = recipesSection.getMapList(id);
                int i = 0;
                for (Map<?, ?> recipeMap : recipeList) {
                    ConfigurationSection recipeConfig = new YamlConfiguration().createSection("recipe", recipeMap);
                    if (recipeConfig.getBoolean("enabled", true)) {
                        parseRecipe(id + "_" + i, recipeConfig);
                        i++;
                    }
                }
            } else if (recipesSection.isConfigurationSection(id)) {
                ConfigurationSection recipeConfig = recipesSection.getConfigurationSection(id);
                if (recipeConfig != null && recipeConfig.getBoolean("enabled", true)) {
                    parseRecipe(id, recipeConfig);
                }
            }
        }
        plugin.getLogger().info("recipe.yml loaded");
    }

    private void loadOpItemRecipes() {
        File opItemFile = new File(plugin.getDataFolder(), "op_item.yml");
        if (!opItemFile.exists()) {
            plugin.saveResource("op_item.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(opItemFile);
        for (String id : config.getKeys(false)) {
            if (config.isConfigurationSection(id)) {
                ConfigurationSection recipeConfig = config.getConfigurationSection(id);
                // Check if it's a recipe by looking for a 'type' key
                if (recipeConfig != null && recipeConfig.isSet("type") && recipeConfig.getBoolean("enabled", true)) {
                    parseRecipe(id, recipeConfig);
                }
            }
        }
        plugin.getLogger().info("op_item.yml recipes loaded");
    }

    // 解析单个配方
    private void parseRecipe(String id, ConfigurationSection config) {
        if (id.contains("_pickaxe") || id.contains("_axe") || id.contains("_shovel") || id.contains("_hoe") || id.contains("_sword") || id.contains("_shears") || id.contains("_hammer")) {
            if (id.startsWith("dark_matter_") && !plugin.getConfig().getBoolean("tools.dark_matter_tools_enabled", true)) {
                return;
            }
            if (id.startsWith("red_matter_") && !plugin.getConfig().getBoolean("tools.red_matter_tools_enabled", true)) {
                return;
            }
        }
 
        java.util.Map<String, String> placeholders = new java.util.HashMap<>();
        placeholders.put("recipe", id);
        Debug.log("debug.recipe.loading_recipe", placeholders);

        String type = config.getString("type", "shaped").toLowerCase();
        if (type.equals("shaped")) {
            parseShapedRecipe(id, config);
        } else if (type.equals("shapeless")) {
            parseShapelessRecipe(id, config);
        }
    }

    // 解析有序配方
    private void parseShapedRecipe(String id, ConfigurationSection config) {
        NamespacedKey key = new NamespacedKey(plugin, id);
        ItemStack result;

        String originalId = id.split("_")[0];
        int kleinStarLevel = getKleinStarLevelFromId(originalId);

        if (kleinStarLevel != -1) {
            result = plugin.getKleinStarManager().getKleinStar(kleinStarLevel);
        } else {
            result = createResultStack(config.getConfigurationSection("result"));
        }

        if (result == null) {
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().warning("[Debug] Recipe " + id + " has a null result. Skipping.");
            }
            return;
        }

        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(config.getStringList("shape").toArray(new String[0]));

        ConfigurationSection ingredients = config.getConfigurationSection("ingredients");
        if (ingredients != null) {
            for (String ingredientKey : ingredients.getKeys(false)) {
                char keyChar = ingredientKey.charAt(0);
                String ingredientValue = ingredients.getString(ingredientKey);
                RecipeChoice choice = getChoice(ingredientValue);
                if (choice != null) {
                    recipe.setIngredient(keyChar, choice);
                    if (plugin.getConfig().getBoolean("debug")) {
                        plugin.getLogger().info("[Debug] Recipe " + id + ": ingredient " + keyChar + " = " + ingredientValue);
                    }
                } else {
                    if (plugin.getConfig().getBoolean("debug")) {
                        plugin.getLogger().warning("[Debug] Recipe " + id + ": ingredient " + keyChar + " (" + ingredientValue + ") is invalid. Skipping.");
                    }
                }
            }
        }
        Bukkit.addRecipe(recipe);
        recipeKeys.put(id, key);
        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().info("[Debug] Successfully registered shaped recipe: " + id);
        }
    }

    // 解析无序配方
    private void parseShapelessRecipe(String id, ConfigurationSection config) {
        NamespacedKey key = new NamespacedKey(plugin, id);
        ItemStack result;

        String originalId = id.split("_")[0];
        int kleinStarLevel = getKleinStarLevelFromId(originalId);

        if (kleinStarLevel != -1) {
            result = plugin.getKleinStarManager().getKleinStar(kleinStarLevel);
        } else {
            result = createResultStack(config.getConfigurationSection("result"));
        }

        if (result == null) {
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().warning("[Debug] Recipe " + id + " has a null result. Skipping.");
            }
            return;
        }

        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        List<String> ingredients = config.getStringList("ingredients");
        for (String ingredientValue : ingredients) {
            RecipeChoice choice = getChoice(ingredientValue);
            if (choice != null) {
                recipe.addIngredient(choice);
                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().info("[Debug] Recipe " + id + ": ingredient = " + ingredientValue);
                }
            } else {
                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().warning("[Debug] Recipe " + id + ": ingredient " + ingredientValue + " is invalid. Skipping.");
                }
            }
        }
        Bukkit.addRecipe(recipe);
        recipeKeys.put(id, key);
        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().info("[Debug] Successfully registered shapeless recipe: " + id);
        }
    }

    // 获取原料选择对象
    private RecipeChoice getChoice(String ingredient) {
        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().info("[Debug] Getting choice for ingredient: " + ingredient);
        }
        if (ingredient.startsWith("projecte:")) {
            String customItemId = ingredient.substring(9);
            ItemStack customItem;
            if (customItemId.equals("alchemical_bag")) {
                customItem = AlchemicalBagManager.getAlchemicalBag();
            } else {
                customItem = plugin.getItemStackFromKey(customItemId);
            }
            if (customItem != null) {
                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().info("[Debug] Matched custom item: " + customItemId);
                }

                return new RecipeChoice.ExactChoice(customItem);
            } else {
                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().warning("[Debug] Custom item not found: " + customItemId);
                }
                return null;
            }
        }

        if (ingredient.equalsIgnoreCase("any_wool")) {
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("[Debug] Matched special case: any_wool");
            }
            return new RecipeChoice.MaterialChoice(
                    Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL,
                    Material.LIGHT_BLUE_WOOL, Material.YELLOW_WOOL, Material.LIME_WOOL,
                    Material.PINK_WOOL, Material.GRAY_WOOL, Material.LIGHT_GRAY_WOOL,
                    Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL,
                    Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL, Material.BLACK_WOOL);
        }

        if (ingredient.equalsIgnoreCase("any_dye")) {
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("[Debug] Matched special case: any_dye");
            }
            return new RecipeChoice.MaterialChoice(Material.RED_DYE, Material.GREEN_DYE, Material.BLUE_DYE,
                    Material.WHITE_DYE, Material.BLACK_DYE, Material.YELLOW_DYE, Material.PURPLE_DYE,
                    Material.ORANGE_DYE);
        }

        try {
            String materialName = ingredient;
            if (materialName.startsWith("minecraft:")) {
                materialName = materialName.substring(10);
            }
            Material material = Material.valueOf(materialName.toUpperCase());
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("[Debug] Matched material: " + material.name());
            }
            return new RecipeChoice.MaterialChoice(material);
        } catch (IllegalArgumentException e) {
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().warning("[Debug] No choice found for ingredient: " + ingredient);
            }
            return null;
        }
    }

    private void registerSpecialFuelRecipes() {
        FuelManager fuelManager = plugin.getFuelManager();
        ItemStack philosopherStone = plugin.getPhilosopherStone();

        // 检查recipe.yml中的配方是否已经注册成功
        boolean alchemicalCoalExists = recipeKeys.containsKey("alchemical_coal");
        boolean mobiusFuelExists = recipeKeys.containsKey("mobius_fuel");
        boolean aeternalisFuelExists = recipeKeys.containsKey("aeternalis_fuel");
        boolean aeternalisToMobiusExists = recipeKeys.containsKey("aeternalis_to_mobius");
        boolean mobiusToAlchemicalExists = recipeKeys.containsKey("mobius_to_alchemical");
        boolean alchemicalToCoalExists = recipeKeys.containsKey("alchemical_to_coal");

        // 只有在recipe.yml中对应的配方未成功注册时才使用代码注册
        if (!alchemicalCoalExists) {
            registerFuelUpgradeRecipe("code_alchemical_coal", new ItemStack(Material.COAL, 4), fuelManager.getAlchemicalCoal(), philosopherStone);
            plugin.getLogger().info("Registered alchemical coal recipe from code");
        }
        
        if (!mobiusFuelExists) {
            registerFuelUpgradeRecipe("code_mobius_fuel", fuelManager.getAlchemicalCoal(), 4, fuelManager.getMobiusFuel(), 1, philosopherStone);
            plugin.getLogger().info("Registered mobius fuel recipe from code");
        }
        
        if (!aeternalisFuelExists) {
            registerFuelUpgradeRecipe("code_aeternalis_fuel", fuelManager.getMobiusFuel(), 4, fuelManager.getAeternalisFuel(), 1, philosopherStone);
            plugin.getLogger().info("Registered aeternalis fuel recipe from code");
        }

        if (!aeternalisToMobiusExists) {
            registerFuelDowngradeRecipe("code_aeternalis_to_mobius", fuelManager.getAeternalisFuel(), fuelManager.getMobiusFuel(), 4, philosopherStone);
            plugin.getLogger().info("Registered aeternalis to mobius recipe from code");
        }
        
        if (!mobiusToAlchemicalExists) {
            registerFuelDowngradeRecipe("code_mobius_to_alchemical", fuelManager.getMobiusFuel(), fuelManager.getAlchemicalCoal(), 4, philosopherStone);
            plugin.getLogger().info("Registered mobius to alchemical recipe from code");
        }
        
        if (!alchemicalToCoalExists) {
            registerFuelDowngradeRecipe("code_alchemical_to_coal", fuelManager.getAlchemicalCoal(), new ItemStack(Material.COAL, 4), philosopherStone);
            plugin.getLogger().info("Registered alchemical to coal recipe from code");
        }
    }

    private void registerFuelUpgradeRecipe(String id, ItemStack input, int inputAmount, ItemStack output, int outputAmount, ItemStack catalyst) {
        NamespacedKey key = new NamespacedKey(plugin, "upgrade_" + id);
        ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(output.getType(), outputAmount));
        ItemMeta meta = output.getItemMeta();
        if (meta != null) {
            recipe.getResult().setItemMeta(meta);
        }
        recipe.addIngredient(new RecipeChoice.ExactChoice(input));
        recipe.addIngredient(new RecipeChoice.ExactChoice(catalyst));
        Bukkit.addRecipe(recipe);
        recipeKeys.put("upgrade_" + id, key);
    }

    private void registerFuelUpgradeRecipe(String id, ItemStack input, ItemStack output, ItemStack catalyst) {
        NamespacedKey key = new NamespacedKey(plugin, "upgrade_" + id);
        ShapelessRecipe recipe = new ShapelessRecipe(key, output);
        recipe.addIngredient(new RecipeChoice.ExactChoice(input));
        recipe.addIngredient(new RecipeChoice.ExactChoice(catalyst));
        Bukkit.addRecipe(recipe);
        recipeKeys.put("upgrade_" + id, key);
    }

    private void registerFuelDowngradeRecipe(String id, ItemStack input, ItemStack output, int outputAmount, ItemStack catalyst) {
        NamespacedKey key = new NamespacedKey(plugin, "downgrade_" + id);
        ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(output.getType(), outputAmount));
        ItemMeta meta = output.getItemMeta();
        if (meta != null) {
            recipe.getResult().setItemMeta(meta);
        }
        recipe.addIngredient(new RecipeChoice.ExactChoice(input));
        recipe.addIngredient(new RecipeChoice.ExactChoice(catalyst));
        Bukkit.addRecipe(recipe);
        recipeKeys.put("downgrade_" + id, key);
    }

    private void registerFuelDowngradeRecipe(String id, ItemStack input, ItemStack output, ItemStack catalyst) {
        NamespacedKey key = new NamespacedKey(plugin, "downgrade_" + id);
        ShapelessRecipe recipe = new ShapelessRecipe(key, output);
        recipe.addIngredient(new RecipeChoice.ExactChoice(input));
        recipe.addIngredient(new RecipeChoice.ExactChoice(catalyst));
        Bukkit.addRecipe(recipe);
        recipeKeys.put("downgrade_" + id, key);
    }

    private ItemStack createResultStack(ConfigurationSection config) {
        if (config == null)
            return null;

        if (config.contains("projecte_id")) {
            String projecteId = config.getString("projecte_id");
            if (projecteId != null && projecteId.startsWith("projecte:")) {
                projecteId = projecteId.substring(9);
            }

            ItemStack item;
            
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("[Debug] Creating result with projecte_id: " + projecteId);
            }
            
            item = plugin.getItemStackFromKey(projecteId);
            if (item != null && plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("[Debug] Found item from getItemStackFromKey: " + projecteId);
            }


            if (item != null) {
                item.setAmount(config.getInt("amount", 1));
                File customEmcFile = new File(plugin.getDataFolder(), "custommoditememc.yml");
                if (customEmcFile.exists()) {
                    YamlConfiguration emcConfig = YamlConfiguration.loadConfiguration(customEmcFile);
                    if (emcConfig.contains(projecteId)) {
                        long emc = emcConfig.getLong(projecteId);
                        plugin.getEmcManager().setEmcValue(item, emc);
                    }
                }
                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().info("[Debug] Successfully created item for projecte_id: " + projecteId);
                }
                return item;
            } else {
                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().warning("[Debug] Failed to create item for projecte_id: " + projecteId);
                }
            }
        } else {
            String materialName = config.getString("material");
            Material material = plugin.getVersionAdapter().getMaterial(materialName);
            if (material == null) {
            material = Material.matchMaterial(materialName);
        }
        if (material == null)
            return null;

        ItemStack item = new ItemStack(material, config.getInt("amount", 1));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (config.contains("display_name")) {
                meta.setDisplayName(plugin.getLanguageManager().get(config.getString("display_name")));
            }
            if (config.contains("lore1")) {
                java.util.List<String> lore = new java.util.ArrayList<>();
                for (int i = 1; config.contains("lore" + i); i++) {
                    lore.add(plugin.getLanguageManager().get(config.getString("lore" + i)));
                }
                meta.setLore(lore);
            }
            if (config.contains("custom_model_data")) {
                item = org.Little_100.projecte.util.CustomModelDataUtil.setCustomModelData(item,
                        config.getString("custom_model_data"));
                meta = item.getItemMeta();
                if (config.contains("display_name")) {
                    meta.setDisplayName(plugin.getLanguageManager().get(config.getString("display_name")));
                }
                if (config.contains("lore1")) {
                    java.util.List<String> lore = new java.util.ArrayList<>();
                    for (int i = 1; config.contains("lore" + i); i++) {
                        lore.add(plugin.getLanguageManager().get(config.getString("lore" + i)));
                    }
                    meta.setLore(lore);
                }
            }
            if (config.getBoolean("unbreakable")) {
                meta.setUnbreakable(true);
            }
            item.setItemMeta(meta);
            }
            return item;
        }
        return null;
    }

    // 注册升级配方
    private void registerUpgradeRecipes() {
        ItemStack philosopherStone = plugin.getPhilosopherStone();
        registerUpgradeRecipe("copper_to_iron", Material.COPPER_INGOT, Material.IRON_INGOT, 4, 1, philosopherStone);
        registerUpgradeRecipe("iron_to_gold", Material.IRON_INGOT, Material.GOLD_INGOT, 4, 1, philosopherStone);
        registerUpgradeRecipe("gold_to_diamond", Material.GOLD_INGOT, Material.DIAMOND, 4, 1, philosopherStone);
        registerSpecialUpgradeRecipe("diamond_to_netherite", Material.DIAMOND_BLOCK, Material.NETHERITE_INGOT, 8, 1,
                philosopherStone);
    }

    // 注册降级配方
    private void registerDowngradeRecipes() {
        ItemStack philosopherStone = plugin.getPhilosopherStone();
        registerDowngradeRecipe("copper_to_coal", Material.COPPER_INGOT, Material.COAL, 1, 4, philosopherStone);
        registerDowngradeRecipe("iron_to_copper", Material.IRON_INGOT, Material.COPPER_INGOT, 1, 4, philosopherStone);
        registerDowngradeRecipe("gold_to_iron", Material.GOLD_INGOT, Material.IRON_INGOT, 1, 4, philosopherStone);
        registerDowngradeRecipe("diamond_to_gold", Material.DIAMOND, Material.GOLD_INGOT, 1, 4, philosopherStone);
        registerDowngradeRecipe("netherite_to_diamond", Material.NETHERITE_INGOT, Material.DIAMOND_BLOCK, 1, 8,
                philosopherStone);
    }

    // 注册单个升级配方
    private void registerUpgradeRecipe(String id, Material input, Material output, int inputAmount, int outputAmount,
            ItemStack catalyst) {
        NamespacedKey key = new NamespacedKey(plugin, "upgrade_" + id);
        ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(output, outputAmount));
        recipe.addIngredient(inputAmount, input);
        recipe.addIngredient(catalyst.getType());
        Bukkit.addRecipe(recipe);
        recipeKeys.put("upgrade_" + id, key);
    }

    // 注册特殊升级配方
    private void registerSpecialUpgradeRecipe(String id, Material input, Material output, int inputAmount,
            int outputAmount, ItemStack catalyst) {
        NamespacedKey key = new NamespacedKey(plugin, "special_" + id);
        ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(output, outputAmount));
        recipe.addIngredient(inputAmount, input);
        recipe.addIngredient(catalyst.getType());
        Bukkit.addRecipe(recipe);
        recipeKeys.put("special_" + id, key);
    }

    // 注册单个降级配方
    private void registerDowngradeRecipe(String id, Material input, Material output, int inputAmount, int outputAmount,
            ItemStack catalyst) {
        NamespacedKey key = new NamespacedKey(plugin, "downgrade_" + id);
        ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(output, outputAmount));
        recipe.addIngredient(inputAmount, input);
        recipe.addIngredient(catalyst.getType());
        Bukkit.addRecipe(recipe);
        recipeKeys.put("downgrade_" + id, key);
    }

    // 获取所有配方键
    public Map<String, NamespacedKey> getRecipeKeys() {
        return recipeKeys;
    }

    public java.util.Set<String> getRegisteredItemIds() {
        return recipeKeys.keySet();
    }

    // 注销所有配方
    public void unregisterAllRecipes() {
        for (NamespacedKey key : recipeKeys.values()) {
            Bukkit.removeRecipe(key);
        }
        recipeKeys.clear();
    }

    // 注册矿石转化配方
    private void registerOreTransmutationRecipes() {
        RecipeChoice catalyst = new RecipeChoice.ExactChoice(plugin.getPhilosopherStone());
        RecipeChoice fuel = new RecipeChoice.MaterialChoice(Material.COAL, Material.CHARCOAL);

        registerOreTransmutationRecipe("iron",
                new RecipeChoice.MaterialChoice(Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE, Material.RAW_IRON),
                new ItemStack(Material.IRON_INGOT, 7), catalyst, fuel);
        registerOreTransmutationRecipe("gold",
                new RecipeChoice.MaterialChoice(Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.RAW_GOLD),
                new ItemStack(Material.GOLD_INGOT, 7), catalyst, fuel);
        registerOreTransmutationRecipe("copper", new RecipeChoice.MaterialChoice(Material.COPPER_ORE,
                Material.DEEPSLATE_COPPER_ORE, Material.RAW_COPPER), new ItemStack(Material.COPPER_INGOT, 7), catalyst,
                fuel);
        registerOreTransmutationRecipe("diamond",
                new RecipeChoice.MaterialChoice(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE),
                new ItemStack(Material.DIAMOND, 7), catalyst, fuel);
        registerOreTransmutationRecipe("netherite", new RecipeChoice.MaterialChoice(Material.ANCIENT_DEBRIS),
                new ItemStack(Material.NETHERITE_SCRAP, 7), catalyst, fuel);
        RecipeChoice logChoice = new RecipeChoice.MaterialChoice(
                Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG,
                Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.MANGROVE_LOG, Material.CHERRY_LOG,
                Material.STRIPPED_OAK_LOG, Material.STRIPPED_SPRUCE_LOG, Material.STRIPPED_BIRCH_LOG,
                Material.STRIPPED_JUNGLE_LOG, Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_DARK_OAK_LOG,
                Material.STRIPPED_MANGROVE_LOG, Material.STRIPPED_CHERRY_LOG);
        registerOreTransmutationRecipe("wood_to_charcoal", logChoice, new ItemStack(Material.CHARCOAL, 7), catalyst,
                fuel);
    }

    // 注册单个矿石转化配方
    private void registerOreTransmutationRecipe(String id, RecipeChoice oreChoice, ItemStack result,
            RecipeChoice catalyst, RecipeChoice fuel) {
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

    private int getKleinStarLevelFromId(String id) {
        switch (id) {
            case "klein_star_ein": return 1;
            case "klein_star_zwei": return 2;
            case "klein_star_drei": return 3;
            case "klein_star_vier": return 4;
            case "klein_star_sphere": return 5;
            case "klein_star_omega": return 6;
            default: return -1;
        }
    }
}