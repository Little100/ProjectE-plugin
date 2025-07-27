package org.Little_100.projecte.AlchemicalBag;

import org.Little_100.projecte.LanguageManager;
import org.Little_100.projecte.ProjectE;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AlchemicalBagManager implements Listener {

    private final ProjectE plugin;
    private final List<NamespacedKey> registeredRecipeKeys = new ArrayList<>();
    private final InventoryManager inventoryManager;
    private final LanguageManager languageManager;

    public AlchemicalBagManager(ProjectE plugin) {
        this.plugin = plugin;
        this.inventoryManager = new InventoryManager(plugin, this);
        this.languageManager = plugin.getLanguageManager();
    }

    public void register() {
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerInteractListener(plugin, this, inventoryManager), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(inventoryManager, plugin);
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        removeVanillaRecipes();
        registerConfiguredRecipes();
    }

    public void unregister() {
        unregisterRecipes();
    }

    private void removeVanillaRecipes() {
        try {
            Bukkit.removeRecipe(NamespacedKey.minecraft("leather_horse_armor"));
            plugin.getLogger().info("Successfully removed vanilla leather horse armor recipe.");
        } catch (Exception e) {
            plugin.getLogger().warning("Could not remove vanilla leather horse armor recipe: " + e.getMessage());
        }
    }

    public void registerConfiguredRecipes() {
        registerBaseBagRecipe();
        registerDyeingRecipe();
    }

    private void registerBaseBagRecipe() {
        ConfigurationSection recipeConfig = plugin.getConfig().getConfigurationSection("AlchemicalBag.recipe");
        if (recipeConfig == null) {
            plugin.getLogger().warning("In config.yml, 'AlchemicalBag.recipe' section not found. Cannot register base Alchemical Bag recipe.");
            return;
        }

        List<String> shape = recipeConfig.getStringList("shape");
        ConfigurationSection ingredients = recipeConfig.getConfigurationSection("ingredients");

        if (shape.size() != 3 || ingredients == null) {
            plugin.getLogger().warning("Invalid Alchemical Bag recipe definition in config.yml. It must have a 3-line shape and an ingredients section.");
            return;
        }

        ItemStack resultBag = createColoredBagItem("DEFAULT");
        NamespacedKey key = new NamespacedKey(plugin, "alchemicalbag_base");
        ShapedRecipe recipe = new ShapedRecipe(key, resultBag);
        recipe.shape(shape.get(0), shape.get(1), shape.get(2));

        String fullShape = String.join("", shape);

        for (String ingredientKey : ingredients.getKeys(false)) {
            char keyChar = ingredientKey.charAt(0);

            if (fullShape.indexOf(keyChar) == -1) {
                plugin.getLogger().warning("Ingredient '" + keyChar + "' is defined in the Alchemical Bag recipe ingredients but not used in the shape. It will be ignored.");
                continue;
            }

            String materialName = ingredients.getString(ingredientKey);
            if (materialName.equalsIgnoreCase("any_wool")) {
                recipe.setIngredient(keyChar, new RecipeChoice.MaterialChoice(
                    Arrays.stream(Material.values())
                          .filter(m -> m.name().endsWith("_WOOL"))
                          .collect(Collectors.toList())
                ));
            } else {
                Material material = Material.matchMaterial(materialName);
                if (material != null) {
                    recipe.setIngredient(keyChar, material);
                } else {
                    plugin.getLogger().warning("Invalid material '" + materialName + "' in Alchemical Bag recipe. Skipping ingredient '" + keyChar + "'.");
                }
            }
        }

        if (Bukkit.addRecipe(recipe)) {
            registeredRecipeKeys.add(key);
            plugin.getLogger().info("Successfully registered the base Alchemical Bag recipe.");
        } else {
            plugin.getLogger().warning("Failed to register the base Alchemical Bag recipe.");
        }
    }

    private void registerDyeingRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "alchemicalbag_dyeing");

        ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(Material.LEATHER_HORSE_ARMOR));

        recipe.addIngredient(new RecipeChoice.MaterialChoice(Material.LEATHER_HORSE_ARMOR));
        recipe.addIngredient(new RecipeChoice.MaterialChoice(
            Arrays.stream(Material.values())
                  .filter(m -> m.name().endsWith("_DYE"))
                  .collect(Collectors.toList())
        ));

        if (Bukkit.addRecipe(recipe)) {
            registeredRecipeKeys.add(key);
            plugin.getLogger().info("Successfully registered the dynamic Alchemical Bag dyeing recipe.");
        } else {
            plugin.getLogger().warning("Failed to register the Alchemical Bag dyeing recipe.");
        }
    }

    public void unregisterRecipes() {
        plugin.getLogger().info("Unregistering Alchemical Bag custom recipes...");
        int count = 0;
        for (NamespacedKey key : registeredRecipeKeys) {
            if (Bukkit.removeRecipe(key)) {
                count++;
            }
        }
        plugin.getLogger().info("Successfully unregistered " + count + " Alchemical Bag recipes.");
        registeredRecipeKeys.clear();
    }

    public ItemStack createColoredBagItem(String colorIdentifier) {
        ItemStack bag = new ItemStack(Material.LEATHER_HORSE_ARMOR);
        LeatherArmorMeta meta = (LeatherArmorMeta) bag.getItemMeta();
        if (meta == null) return null;

        Color bukkitColor;
        String displayName;

        if (colorIdentifier.equals("DEFAULT")) {
            bukkitColor = Bukkit.getServer().getItemFactory().getDefaultLeatherColor();
            displayName = languageManager.get("clientside.alchemical_bag.default_name");
        } else {
            try {
                DyeColor dye = DyeColor.valueOf(colorIdentifier.toUpperCase());
                bukkitColor = dye.getColor();
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("color", PlayerInteractListener.getChatColor(colorIdentifier).toString());
                placeholders.put("color_name", colorIdentifier);
                displayName = languageManager.get("clientside.alchemical_bag.colored_name", placeholders);

            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Could not parse '" + colorIdentifier + "' as a valid color! Cannot create result item.");
                return null;
            }
        }

        meta.setColor(bukkitColor);
        meta.setDisplayName(displayName);
        bag.setItemMeta(meta);
        return bag;
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe == null || !(recipe instanceof ShapelessRecipe)) {
            return;
        }

        NamespacedKey key = ((ShapelessRecipe) recipe).getKey();
        if (!key.equals(new NamespacedKey(plugin, "alchemicalbag_dyeing"))) {
            return;
        }

        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();

        ItemStack bag = null;
        ItemStack dye = null;

        for (ItemStack item : matrix) {
            if (item != null && !item.getType().isAir()) {
                if (item.getType() == Material.LEATHER_HORSE_ARMOR) {
                    bag = item;
                } else if (item.getType().name().endsWith("_DYE")) {
                    dye = item;
                }
            }
        }

        if (bag == null || dye == null) {
            inventory.setResult(null);
            return;
        }

        LeatherArmorMeta bagMeta = (LeatherArmorMeta) bag.getItemMeta();
        if (bagMeta == null) {
            inventory.setResult(null);
            return;
        }

        DyeColor dyeColor = getDyeColor(dye.getType());
        if (dyeColor == null) {
            inventory.setResult(null);
            return;
        }

        ItemStack result = bag.clone();
        LeatherArmorMeta resultMeta = (LeatherArmorMeta) result.getItemMeta();
        resultMeta.setColor(dyeColor.getColor());
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("color", PlayerInteractListener.getChatColor(dyeColor.name()).toString());
        placeholders.put("color_name", dyeColor.name());
        resultMeta.setDisplayName(languageManager.get("clientside.alchemical_bag.colored_name", placeholders));
        result.setItemMeta(resultMeta);

        inventory.setResult(result);
    }

    private DyeColor getDyeColor(Material material) {
        try {
            return DyeColor.valueOf(material.name().replace("_DYE", ""));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}