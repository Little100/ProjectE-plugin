package org.Little_100.projecte;

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
import java.util.ArrayList;
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
        // registerSpecialFuelRecipes(); // FuelManager is disabled
        registerOreTransmutationRecipes();
    }

    private void loadRecipesFromYaml() {
        File recipeFile = new File(plugin.getDataFolder(), "recipe.yml");
        if (!recipeFile.exists()) {
            plugin.getLogger().info("recipe.yml not found, creating a default one.");
            plugin.saveResource("recipe.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(recipeFile);
        ConfigurationSection recipesSection = config.getConfigurationSection("recipes");
        if (recipesSection == null) {
            List<Map<?, ?>> recipeList = config.getMapList("recipes");
            if (recipeList.isEmpty()) {
                plugin.getLogger().warning("Could not find 'recipes' section or list in recipe.yml.");
                return;
            }
            recipesSection = new YamlConfiguration();
            for (Map<?, ?> map : recipeList) {
                if (map.containsKey("id")) {
                    recipesSection.set(map.get("id").toString(), map);
                }
            }
        }


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
                    ConfigurationSection recipeConfig = new YamlConfiguration().createSection("recipe", (Map<String, Object>) recipeMap);
                    if (recipeConfig.getBoolean("enabled", true)) {
                        parseRecipe(id + "_" + i, recipeConfig);
                        i++;
                    }
                }
            }
        }
    }

    private void parseRecipe(String id, ConfigurationSection config) {
        String type = config.getString("type", "shaped").toLowerCase();
        if (type.equals("shaped")) {
            parseShapedRecipe(id, config);
        } else if (type.equals("shapeless")) {
            parseShapelessRecipe(id, config);
        }
    }

    private void parseShapedRecipe(String id, ConfigurationSection config) {
        NamespacedKey key = new NamespacedKey(plugin, id);
        ItemStack result = createResultStack(config.getConfigurationSection("result"));
        if (result == null) {
            plugin.getLogger().warning("Invalid result for recipe: " + id);
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
                    plugin.getLogger().warning("Invalid ingredient '" + ingredientValue + "' in recipe: " + id);
                }
            }
        }
        Bukkit.addRecipe(recipe);
        recipeKeys.put(id, key);
    }

    private void parseShapelessRecipe(String id, ConfigurationSection config) {
        NamespacedKey key = new NamespacedKey(plugin, id);
        ItemStack result = createResultStack(config.getConfigurationSection("result"));
        if (result == null) {
            plugin.getLogger().warning("Invalid result for recipe: " + id);
            return;
        }

        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        List<String> ingredients = config.getStringList("ingredients");
        for (String ingredientValue : ingredients) {
            RecipeChoice choice = getChoice(ingredientValue);
            if (choice != null) {
                recipe.addIngredient(choice);
            } else {
                plugin.getLogger().warning("Invalid ingredient '" + ingredientValue + "' in recipe: " + id);
            }
        }
        Bukkit.addRecipe(recipe);
        recipeKeys.put(id, key);
    }

    private RecipeChoice getChoice(String ingredient) {
        if (ingredient.equalsIgnoreCase("projecte:philosopher_stone")) {
            return new RecipeChoice.ExactChoice(plugin.getPhilosopherStone());
        } else if (ingredient.equalsIgnoreCase("any_wool")) {
            return new RecipeChoice.MaterialChoice(
                Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL,
                Material.LIGHT_BLUE_WOOL, Material.YELLOW_WOOL, Material.LIME_WOOL,
                Material.PINK_WOOL, Material.GRAY_WOOL, Material.LIGHT_GRAY_WOOL,
                Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL,
                Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL, Material.BLACK_WOOL
            );
        } else if (ingredient.equalsIgnoreCase("any_dye")) {
            return new RecipeChoice.MaterialChoice(Material.RED_DYE, Material.GREEN_DYE, Material.BLUE_DYE, Material.WHITE_DYE, Material.BLACK_DYE, Material.YELLOW_DYE, Material.PURPLE_DYE, Material.ORANGE_DYE);
        } else if (ingredient.equalsIgnoreCase("projecte:alchemical_bag")) {
            return new RecipeChoice.MaterialChoice(Material.LEATHER_HORSE_ARMOR);
        }
        else {
            Material mat = Material.matchMaterial(ingredient);
            if (mat != null) {
                return new RecipeChoice.MaterialChoice(mat);
            }
        }
        return null;
    }

    private ItemStack createResultStack(ConfigurationSection config) {
        if (config == null) return null;
        String materialName = config.getString("material");
        Material material = plugin.getVersionAdapter().getMaterial(materialName);
        if (material == null) {
            material = Material.matchMaterial(materialName);
        }
        if (material == null) return null;

        ItemStack item = new ItemStack(material, config.getInt("amount", 1));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (config.contains("display_name")) {
                meta.setDisplayName(config.getString("display_name"));
            }
            if (config.contains("lore")) {
                meta.setLore(config.getStringList("lore"));
            }
            if (config.contains("custom_model_data")) {
                meta.setCustomModelData(config.getInt("custom_model_data"));
            }
            if (config.getBoolean("unbreakable")) {
                meta.setUnbreakable(true);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerUpgradeRecipes() {
        ItemStack philosopherStone = plugin.getPhilosopherStone();
        registerUpgradeRecipe("copper_to_iron", Material.COPPER_INGOT, Material.IRON_INGOT, 4, 1, philosopherStone);
        registerUpgradeRecipe("iron_to_gold", Material.IRON_INGOT, Material.GOLD_INGOT, 4, 1, philosopherStone);
        registerUpgradeRecipe("gold_to_diamond", Material.GOLD_INGOT, Material.DIAMOND, 4, 1, philosopherStone);
        registerSpecialUpgradeRecipe("diamond_to_netherite", Material.DIAMOND_BLOCK, Material.NETHERITE_INGOT, 8, 1, philosopherStone);
    }

    private void registerDowngradeRecipes() {
        ItemStack philosopherStone = plugin.getPhilosopherStone();
        registerDowngradeRecipe("copper_to_coal", Material.COPPER_INGOT, Material.COAL, 1, 4, philosopherStone);
        registerDowngradeRecipe("iron_to_copper", Material.IRON_INGOT, Material.COPPER_INGOT, 1, 4, philosopherStone);
        registerDowngradeRecipe("gold_to_iron", Material.GOLD_INGOT, Material.IRON_INGOT, 1, 4, philosopherStone);
        registerDowngradeRecipe("diamond_to_gold", Material.DIAMOND, Material.GOLD_INGOT, 1, 4, philosopherStone);
        registerDowngradeRecipe("netherite_to_diamond", Material.NETHERITE_INGOT, Material.DIAMOND_BLOCK, 1, 8, philosopherStone);
    }

    private void registerUpgradeRecipe(String id, Material input, Material output, int inputAmount, int outputAmount, ItemStack catalyst) {
        NamespacedKey key = new NamespacedKey(plugin, "upgrade_" + id);
        ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(output, outputAmount));
        recipe.addIngredient(inputAmount, input);
        recipe.addIngredient(catalyst.getType());
        Bukkit.addRecipe(recipe);
        recipeKeys.put("upgrade_" + id, key);
    }

    private void registerSpecialUpgradeRecipe(String id, Material input, Material output, int inputAmount, int outputAmount, ItemStack catalyst) {
        NamespacedKey key = new NamespacedKey(plugin, "special_" + id);
        ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(output, outputAmount));
        recipe.addIngredient(inputAmount, input);
        recipe.addIngredient(catalyst.getType());
        Bukkit.addRecipe(recipe);
        recipeKeys.put("special_" + id, key);
    }

    private void registerDowngradeRecipe(String id, Material input, Material output, int inputAmount, int outputAmount, ItemStack catalyst) {
        NamespacedKey key = new NamespacedKey(plugin, "downgrade_" + id);
        ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(output, outputAmount));
        recipe.addIngredient(inputAmount, input);
        recipe.addIngredient(catalyst.getType());
        Bukkit.addRecipe(recipe);
        recipeKeys.put("downgrade_" + id, key);
    }

    public Map<String, NamespacedKey> getRecipeKeys() {
        return recipeKeys;
    }

    public void unregisterAllRecipes() {
        for (NamespacedKey key : recipeKeys.values()) {
            Bukkit.removeRecipe(key);
        }
        recipeKeys.clear();
    }

    private void registerOreTransmutationRecipes() {
        RecipeChoice catalyst = new RecipeChoice.ExactChoice(plugin.getPhilosopherStone());
        RecipeChoice fuel = new RecipeChoice.MaterialChoice(Material.COAL, Material.CHARCOAL);

        registerOreTransmutationRecipe("iron", new RecipeChoice.MaterialChoice(Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE, Material.RAW_IRON), new ItemStack(Material.IRON_INGOT, 7), catalyst, fuel);
        registerOreTransmutationRecipe("gold", new RecipeChoice.MaterialChoice(Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.RAW_GOLD), new ItemStack(Material.GOLD_INGOT, 7), catalyst, fuel);
        registerOreTransmutationRecipe("copper", new RecipeChoice.MaterialChoice(Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE, Material.RAW_COPPER), new ItemStack(Material.COPPER_INGOT, 7), catalyst, fuel);
        registerOreTransmutationRecipe("diamond", new RecipeChoice.MaterialChoice(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE), new ItemStack(Material.DIAMOND, 7), catalyst, fuel);
        registerOreTransmutationRecipe("netherite", new RecipeChoice.MaterialChoice(Material.ANCIENT_DEBRIS), new ItemStack(Material.NETHERITE_SCRAP, 7), catalyst, fuel);
    }

    private void registerOreTransmutationRecipe(String id, RecipeChoice oreChoice, ItemStack result, RecipeChoice catalyst, RecipeChoice fuel) {
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