package org.Little_100.projecte.compatibility;

import org.Little_100.projecte.EmcManager;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.storage.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.*;

import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LegacyAdapter implements VersionAdapter {

    private EmcManager emcManager;
    private final DatabaseManager databaseManager;

    public LegacyAdapter() {
        this.databaseManager = ProjectE.getInstance().getDatabaseManager();
    }

    private EmcManager getEmcManager() {
        if (this.emcManager == null) {
            this.emcManager = ProjectE.getInstance().getEmcManager();
        }
        return this.emcManager;
    }

    @Override
    public Material getMaterial(String name) {
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String getItemKey(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return "minecraft:air";
        }
        return "minecraft:" + itemStack.getType().name().toLowerCase();
    }

    @Override
    public long calculateRecipeEmc(Recipe recipe, String divisionStrategy) {
        if (recipe == null) return 0;

        long totalEmc = 0;
        boolean isCooking = false;

        if (recipe instanceof ShapedRecipe) {
            for (ItemStack ingredient : ((ShapedRecipe) recipe).getIngredientMap().values()) {
                if (ingredient == null) continue;
                long ingredientEmc = getIngredientEmc(ingredient);
                if (ingredientEmc == 0) return 0;
                totalEmc += ingredientEmc;
            }
        } else if (recipe instanceof ShapelessRecipe) {
            for (ItemStack ingredient : ((ShapelessRecipe) recipe).getIngredientList()) {
                if (ingredient == null) continue;
                long ingredientEmc = getIngredientEmc(ingredient);
                if (ingredientEmc == 0) return 0;
                totalEmc += ingredientEmc;
            }
        } else if (recipe instanceof FurnaceRecipe) {
            isCooking = true;
            FurnaceRecipe furnaceRecipe = (FurnaceRecipe) recipe;
            ItemStack input = furnaceRecipe.getInput();
            if (input != null) {
                totalEmc = getIngredientEmc(input);
            }
        }

        if (totalEmc <= 0) {
            return 0;
        }

        int resultAmount = recipe.getResult().getAmount();
        if (resultAmount <= 0) {
            return 0;
        }

        long recipeEmc;
        if ("ceil".equals(divisionStrategy)) {
            recipeEmc = (long) Math.ceil((double) totalEmc / resultAmount);
        } else {
            recipeEmc = totalEmc / resultAmount;
        }

        if (isCooking && recipeEmc > 0) {
            recipeEmc += 1;
        }

        return recipeEmc;
    }

    private long getIngredientEmc(ItemStack ingredient) {
        if (ingredient == null) return 0;
        return getEmcManager().getEmc(getItemKey(ingredient));
    }

    @Override
    public void loadInitialEmcValues() {
        org.bukkit.configuration.file.FileConfiguration config = ProjectE.getInstance().getConfig();
        org.bukkit.configuration.ConfigurationSection emcSection = config.getConfigurationSection("TransmutationTable.EMC.ImportantItems");

        if (emcSection == null) {
            ProjectE.getInstance().getLogger().warning("EMC section 'TransmutationTable.EMC.ImportantItems' not found in config.yml");
            return;
        }

        List<Map<?, ?>> items = emcSection.getMapList("default");
        if (items == null || items.isEmpty()) {
            ProjectE.getInstance().getLogger().warning("'default' EMC list is missing or empty in config.yml");
            return;
        }

        for (Map<?, ?> itemMap : items) {
            for (Map.Entry<?, ?> entry : itemMap.entrySet()) {
                if (entry.getKey() instanceof String && entry.getValue() instanceof Number) {
                    String itemKey = (String) entry.getKey();
                    long emc = ((Number) entry.getValue()).longValue();

                    if (emc > 0) {
                        if (getMaterial(itemKey) != null) {
                            databaseManager.setEmc("minecraft:" + itemKey.toLowerCase(), emc);
                        } else {
                            ProjectE.getInstance().getLogger().warning("Item '" + itemKey + "' from 'default' EMC list not found in this Minecraft version. Skipping.");
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<String> getRecipeDebugInfo(Recipe recipe, String divisionStrategy) {
        List<String> debugInfo = new ArrayList<>();
        if (recipe == null) {
            debugInfo.add(" - 配方为空");
            return debugInfo;
        }

        debugInfo.add(" - 配方类型: " + recipe.getClass().getSimpleName());
        long calculatedEmc = calculateRecipeEmc(recipe, divisionStrategy);
        debugInfo.add(" - 计算出的EMC: " + calculatedEmc);

        if (recipe instanceof ShapedRecipe) {
            debugInfo.add(" - 成分:");
            for (Map.Entry<Character, ItemStack> entry : ((ShapedRecipe) recipe).getIngredientMap().entrySet()) {
                if (entry.getValue() != null) {
                    String key = getItemKey(entry.getValue());
                    long emc = getIngredientEmc(entry.getValue());
                    debugInfo.add("   - " + key + ": " + emc + " EMC");
                }
            }
        } else if (recipe instanceof ShapelessRecipe) {
            debugInfo.add(" - 成分:");
            for (ItemStack ingredient : ((ShapelessRecipe) recipe).getIngredientList()) {
                if (ingredient != null) {
                    String key = getItemKey(ingredient);
                    long emc = getIngredientEmc(ingredient);
                    debugInfo.add("   - " + key + ": " + emc + " EMC");
                }
            }
        } else if (recipe instanceof FurnaceRecipe) {
            debugInfo.add(" - 成分:");
            ItemStack input = ((FurnaceRecipe) recipe).getInput();
            if (input != null) {
                String key = getItemKey(input);
                long emc = getIngredientEmc(input);
                debugInfo.add("   - " + key + ": " + emc + " EMC");
            }
        }
        return debugInfo;
    }
    @Override
    public Map<String, NamespacedKey> registerTransmutationTableRecipes() {
        Map<String, NamespacedKey> newKeys = new HashMap<>();
        ProjectE plugin = ProjectE.getInstance();
        ItemStack transmutationTable = new ItemStack(Material.PETRIFIED_OAK_SLAB);

        List<Material> stones = Arrays.asList(Material.STONE, Material.COBBLESTONE, Material.ANDESITE, Material.DIORITE, Material.GRANITE);

        for (Material stone : stones) {
            String key1_id = "transmutation_table_1_" + stone.name().toLowerCase();
            NamespacedKey key1 = new NamespacedKey(plugin, key1_id);
            ShapedRecipe recipe1 = new ShapedRecipe(key1, transmutationTable);
            recipe1.shape("OSO", "SPS", "OSO");
            recipe1.setIngredient('O', Material.OBSIDIAN);
            recipe1.setIngredient('S', stone);
            recipe1.setIngredient('P', plugin.getPhilosopherStone().getType());
            Bukkit.addRecipe(recipe1);
            newKeys.put(key1_id, key1);

            String key2_id = "transmutation_table_2_" + stone.name().toLowerCase();
            NamespacedKey key2 = new NamespacedKey(plugin, key2_id);
            ShapedRecipe recipe2 = new ShapedRecipe(key2, transmutationTable);
            recipe2.shape("SOS", "OPO", "SOS");
            recipe2.setIngredient('O', Material.OBSIDIAN);
            recipe2.setIngredient('S', stone);
            recipe2.setIngredient('P', plugin.getPhilosopherStone().getType());
            Bukkit.addRecipe(recipe2);
            newKeys.put(key2_id, key2);
        }
        return newKeys;
    }
}