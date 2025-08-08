package org.Little_100.projecte;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.Little_100.projecte.util.ReflectionUtil;
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
import java.util.stream.Collectors;

public class RecipeManager {

    private final ProjectE plugin;
    private final Map<String, NamespacedKey> recipeKeys = new HashMap<>();

    public RecipeManager(ProjectE plugin) {
        this.plugin = plugin;
    }

    public void registerAllRecipes() {
        loadRecipesFromYaml();
        registerUpgradeRecipes();
        registerDowngradeRecipes();
        registerOreTransmutationRecipes();
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
            List<Map<?, ?>> recipeList = config.getMapList("recipes");
            if (recipeList.isEmpty()) {
                plugin.getLogger().warning("在 recipe.yml 中未找到 'recipes' 区段或列表。");
                return;
            }
            recipesSection = new YamlConfiguration();
            for (Map<?, ?> map : recipeList) {
                if (map.containsKey("id")) {
                    recipesSection.set(map.get("id").toString(), map);
                }
            }
        }

        // 遍历所有配方ID，解析配方
        for (String id : recipesSection.getKeys(false)) {
            if (recipesSection.isConfigurationSection(id)) {
                ConfigurationSection recipeConfig = recipesSection.getConfigurationSection(id);
                if (recipeConfig != null && recipeConfig.getBoolean("enabled", true)) {
                    parseRecipe(id, recipeConfig);
                }
            } else if (recipesSection.isList(id)) {
                List<Map<?, ?>> recipeList = recipesSection.getMapList(id);
                int i = 0;
                for (Map<?, ?> recipeMap : recipeList) {
                    ConfigurationSection recipeConfig = new YamlConfiguration().createSection("recipe",
                            (Map<String, Object>) recipeMap);
                    if (recipeConfig.getBoolean("enabled", true)) {
                        parseRecipe(id + "_" + i, recipeConfig);
                        i++;
                    }
                }
            }
        }
    }

    // 解析单个配方
    private void parseRecipe(String id, ConfigurationSection config) {
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
        ItemStack result = createResultStack(config.getConfigurationSection("result"));
        if (result == null) {
            plugin.getLogger().warning("配方 " + id + " 的结果无效。");
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
                } else {
                    plugin.getLogger().warning("配方 " + id + " 中的原料 '" + ingredientValue + "' 无效。");
                }
            }
        }
        Bukkit.addRecipe(recipe);
        recipeKeys.put(id, key);
    }

    // 解析无序配方
    private void parseShapelessRecipe(String id, ConfigurationSection config) {
        NamespacedKey key = new NamespacedKey(plugin, id);
        ItemStack result;
        if ("low_covalence_dust".equals(id)) {
            result = plugin.getCovalenceDust().getLowCovalenceDust().clone();
            result.setAmount(config.getConfigurationSection("result").getInt("amount", 1));
        } else if ("medium_covalence_dust".equals(id)) {
            result = plugin.getCovalenceDust().getMediumCovalenceDust().clone();
            result.setAmount(config.getConfigurationSection("result").getInt("amount", 1));
        } else if ("high_covalence_dust".equals(id)) {
            result = plugin.getCovalenceDust().getHighCovalenceDust().clone();
            result.setAmount(config.getConfigurationSection("result").getInt("amount", 1));
        } else {
            result = createResultStack(config.getConfigurationSection("result"));
        }
        if (result == null) {
            plugin.getLogger().warning("配方 " + id + " 的结果无效。");
            return;
        }

        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        List<String> ingredients = config.getStringList("ingredients");
        for (String ingredientValue : ingredients) {
            RecipeChoice choice = getChoice(ingredientValue);
            if (choice != null) {
                recipe.addIngredient(choice);
            } else {
                plugin.getLogger().warning("配方 " + id + " 中的原料 '" + ingredientValue + "' 无效。");
            }
        }
        Bukkit.addRecipe(recipe);
        recipeKeys.put(id, key);
    }

    // 获取原料选择对象
    private RecipeChoice getChoice(String ingredient) {
        if (ingredient.equalsIgnoreCase("projecte:philosopher_stone")) {
            return new RecipeChoice.ExactChoice(plugin.getPhilosopherStone());
        } else if (ingredient.equalsIgnoreCase("any_wool")) {
            return new RecipeChoice.MaterialChoice(
                    Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL,
                    Material.LIGHT_BLUE_WOOL, Material.YELLOW_WOOL, Material.LIME_WOOL,
                    Material.PINK_WOOL, Material.GRAY_WOOL, Material.LIGHT_GRAY_WOOL,
                    Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL,
                    Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL, Material.BLACK_WOOL);
        } else if (ingredient.equalsIgnoreCase("any_dye")) {
            return new RecipeChoice.MaterialChoice(Material.RED_DYE, Material.GREEN_DYE, Material.BLUE_DYE,
                    Material.WHITE_DYE, Material.BLACK_DYE, Material.YELLOW_DYE, Material.PURPLE_DYE,
                    Material.ORANGE_DYE);
        } else if (ingredient.equalsIgnoreCase("projecte:alchemical_bag")) {
            return new RecipeChoice.MaterialChoice(Material.LEATHER_HORSE_ARMOR);
        } else if (ingredient.equalsIgnoreCase("projecte:alchemical_coal")) {
            return new RecipeChoice.ExactChoice(plugin.getFuelManager().getAlchemicalCoal());
        } else if (ingredient.equalsIgnoreCase("projecte:mobius_fuel")) {
            return new RecipeChoice.ExactChoice(plugin.getFuelManager().getMobiusFuel());
        } else if (ingredient.equalsIgnoreCase("projecte:aeternalis_fuel")) {
            return new RecipeChoice.ExactChoice(plugin.getFuelManager().getAeternalisFuel());
        } else if (ingredient.equalsIgnoreCase("projecte:alchemical_coal_block")) {
            return new RecipeChoice.ExactChoice(plugin.getFuelManager().getAlchemicalCoalBlock());
        } else if (ingredient.equalsIgnoreCase("projecte:mobius_fuel_block")) {
            return new RecipeChoice.ExactChoice(plugin.getFuelManager().getMobiusFuelBlock());
        } else if (ingredient.equalsIgnoreCase("projecte:aeternalis_fuel_block")) {
            return new RecipeChoice.ExactChoice(plugin.getFuelManager().getAeternalisFuelBlock());
        } else if (ingredient.equalsIgnoreCase("projecte:dark_matter")) {
            return new RecipeChoice.ExactChoice(plugin.getFuelManager().getDarkMatter());
        } else if (ingredient.equalsIgnoreCase("projecte:red_matter")) {
            return new RecipeChoice.ExactChoice(plugin.getFuelManager().getRedMatter());
        } else if (ingredient.equalsIgnoreCase("projecte:dark_matter_block")) {
            return new RecipeChoice.ExactChoice(plugin.getFuelManager().getDarkMatterBlock());
        } else if (ingredient.equalsIgnoreCase("projecte:red_matter_block")) {
            return new RecipeChoice.ExactChoice(plugin.getFuelManager().getRedMatterBlock());
        } else if (ingredient.equalsIgnoreCase("projecte:low_covalence_dust")) {
            return new RecipeChoice.ExactChoice(plugin.getCovalenceDust().getLowCovalenceDust());
        } else if (ingredient.equalsIgnoreCase("projecte:medium_covalence_dust")) {
            return new RecipeChoice.ExactChoice(plugin.getCovalenceDust().getMediumCovalenceDust());
        } else if (ingredient.equalsIgnoreCase("projecte:high_covalence_dust")) {
            return new RecipeChoice.ExactChoice(plugin.getCovalenceDust().getHighCovalenceDust());
        } else {
            Material mat = Material.matchMaterial(ingredient);
            if (mat != null) {
                return new RecipeChoice.MaterialChoice(mat);
            }
        }
        return null;
    }

    private void registerSpecialFuelRecipes() {
    }

    private ItemStack createResultStack(ConfigurationSection config) {
        if (config == null)
            return null;

        if (config.contains("projecte_id")) {
            String projecteId = config.getString("projecte_id");
            String materialName = config.getString("material", "GLOWSTONE_DUST");
            ItemStack item = plugin.getItemStackFromKey(materialName + "{projecte_id:\"" + projecteId + "\"}");
            if (item != null) {
                item.setAmount(config.getInt("amount", 1));
                return item;
            }
        }

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
            if (config.contains("lore")) {
                List<String> loreKeys = config.getStringList("lore");
                List<String> translatedLore = loreKeys.stream()
                        .map(key -> plugin.getLanguageManager().get(key))
                        .collect(Collectors.toList());
                meta.setLore(translatedLore);
            }
            if (config.contains("custom_model_data")) {
                item = org.Little_100.projecte.util.CustomModelDataUtil.setCustomModelData(item,
                        config.getString("custom_model_data"));
                meta = item.getItemMeta();
                if (config.contains("display_name")) {
                    meta.setDisplayName(plugin.getLanguageManager().get(config.getString("display_name")));
                }
                if (config.contains("lore")) {
                    List<String> loreKeys2 = config.getStringList("lore");
                    List<String> translatedLore2 = loreKeys2.stream()
                            .map(key -> plugin.getLanguageManager().get(key))
                            .collect(Collectors.toList());
                    meta.setLore(translatedLore2);
                }
            }
            if (config.getBoolean("unbreakable")) {
                meta.setUnbreakable(true);
            }
            item.setItemMeta(meta);
        }
        return item;
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

}