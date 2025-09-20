package org.Little_100.projecte.accessories;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccessoryRecipeManager {

    private final ProjectE plugin;
    private final Map<String, NamespacedKey> recipeKeys = new HashMap<>();

    public AccessoryRecipeManager(ProjectE plugin) {
        this.plugin = plugin;
    }

    public void registerRecipes() {
        File recipeFile = new File(plugin.getDataFolder(), "accessories.yml");
        if (!recipeFile.exists()) {
            plugin.saveResource("accessories.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(recipeFile);
        ConfigurationSection recipesSection = config.getConfigurationSection("accessories");
        if (recipesSection == null) {
            plugin.getLogger()
                    .warning(
                            "Could not find 'accessories' section in accessories.yml. Please delete the old accessories.yml and restart the server.");
            return;
        }
        for (String id : recipesSection.getKeys(false)) {
            if (recipesSection.isConfigurationSection(id)) {
                ConfigurationSection recipeConfig = recipesSection.getConfigurationSection(id);
                if (recipeConfig != null && recipeConfig.getBoolean("enabled", true)) {
                    parseRecipe(id, recipeConfig);
                }
            }
        }
        plugin.getLogger().info("accessories.yml loaded");
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
            plugin.getLogger().warning("[Debug] Recipe " + id + " has a null result. Skipping.");
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
            plugin.getLogger().warning("[Debug] Recipe " + id + " has a null result. Skipping.");
            return;
        }

        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        List<String> ingredients = config.getStringList("ingredients");
        for (String ingredientValue : ingredients) {
            RecipeChoice choice = getChoice(ingredientValue);
            if (choice != null) {
                recipe.addIngredient(choice);
            }
        }
        Bukkit.addRecipe(recipe);
        recipeKeys.put(id, key);
    }

    private RecipeChoice getChoice(String ingredient) {
        if (ingredient.startsWith("projecte:")) {
            String customItemId = ingredient.substring(9);
            ItemStack customItem = plugin.getItemStackFromKey(customItemId);
            if (customItem != null) {
                return new RecipeChoice.ExactChoice(customItem);
            } else {
                return null;
            }
        }

        try {
            org.bukkit.Material material = org.bukkit.Material.valueOf(ingredient.toUpperCase());
            return new RecipeChoice.MaterialChoice(material);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private ItemStack createResultStack(ConfigurationSection config) {
        if (config == null) {
            return null;
        }

        if (config.contains("projecte_id")) {
            String projecteId = config.getString("projecte_id", "");
            switch (projecteId) {
                case "body_stone":
                    return BodyStone.createBodyStone();
                case "soul_stone":
                    return SoulStone.createSoulStone();
                case "life_stone":
                    return LifeStone.createLifeStone();
                case "mind_stone":
                    return MindStone.createMindStone();
            }
        }
        return null;
    }

    public void unregisterAllRecipes() {
        for (NamespacedKey key : recipeKeys.values()) {
            Bukkit.removeRecipe(key);
        }
        recipeKeys.clear();
    }
}
