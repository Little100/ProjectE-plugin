package org.Little_100.projecte.compatibility.version;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.managers.EmcManager;
import org.Little_100.projecte.storage.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;

import java.util.*;

public class LegacyAdapter implements VersionAdapter {

    private EmcManager emcManager;
    private final DatabaseManager databaseManager;

    LegacyAdapter() {
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
            for (ItemStack ingredient :
                    ((ShapedRecipe) recipe).getIngredientMap().values()) {
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
            totalEmc = getIngredientEmc(input);
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
        FileConfiguration config = ProjectE.getInstance().getConfig();
        ConfigurationSection emcSection = config.getConfigurationSection("TransmutationTable.EMC.ImportantItems");

        if (emcSection == null) {
            ProjectE.getInstance()
                    .getLogger()
                    .warning("EMC section 'TransmutationTable.EMC.ImportantItems' not found in config.yml");
            return;
        }

        List<Map<?, ?>> items = emcSection.getMapList("default");
        if (items.isEmpty()) {
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
                            ProjectE.getInstance()
                                    .getLogger()
                                    .warning(
                                            "Item '" + itemKey
                                                    + "' from 'default' EMC list not found in this Minecraft version. Skipping.");
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
            debugInfo.add(" - Recipe is null");
            return debugInfo;
        }

        debugInfo.add(" - Recipe type: " + recipe.getClass().getSimpleName());
        long calculatedEmc = calculateRecipeEmc(recipe, divisionStrategy);
        debugInfo.add(" - Calculated EMC: " + calculatedEmc);

        if (recipe instanceof ShapedRecipe) {
            debugInfo.add(" - Ingredients:");
            for (Map.Entry<Character, ItemStack> entry :
                    ((ShapedRecipe) recipe).getIngredientMap().entrySet()) {
                if (entry.getValue() != null) {
                    String key = getItemKey(entry.getValue());
                    long emc = getIngredientEmc(entry.getValue());
                    debugInfo.add("   - " + key + ": " + emc + " EMC");
                }
            }
        } else if (recipe instanceof ShapelessRecipe) {
            debugInfo.add(" - Ingredients:");
            for (ItemStack ingredient : ((ShapelessRecipe) recipe).getIngredientList()) {
                if (ingredient != null) {
                    String key = getItemKey(ingredient);
                    long emc = getIngredientEmc(ingredient);
                    debugInfo.add("   - " + key + ": " + emc + " EMC");
                }
            }
        } else if (recipe instanceof FurnaceRecipe) {
            debugInfo.add(" - Ingredient:");
            ItemStack input = ((FurnaceRecipe) recipe).getInput();
            String key = getItemKey(input);
            long emc = getIngredientEmc(input);
            debugInfo.add("   - " + key + ": " + emc + " EMC");
        }
        return debugInfo;
    }

    @Override
    public Map<String, NamespacedKey> registerTransmutationTableRecipes() {
        Map<String, NamespacedKey> newKeys = new HashMap<>();
        ProjectE plugin = ProjectE.getInstance();
        ItemStack transmutationTable = new ItemStack(Material.PETRIFIED_OAK_SLAB);

        List<Material> stones = Arrays.asList(
                Material.STONE, Material.COBBLESTONE, Material.ANDESITE, Material.DIORITE, Material.GRANITE);

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

    @Override
    public void openSign(Player player, Sign sign) {
        player.sendMessage("Please right-click the sign to search.");
    }
}
