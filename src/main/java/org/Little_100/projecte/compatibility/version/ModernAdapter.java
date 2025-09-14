package org.Little_100.projecte.compatibility.version;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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

public class ModernAdapter implements VersionAdapter {

    private EmcManager emcManager;
    private final DatabaseManager databaseManager;

    ModernAdapter() {
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
        return Material.matchMaterial(name.toUpperCase());
    }

    @Override
    public String getItemKey(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return "minecraft:air";
        }
        return itemStack.getType().getKey().toString();
    }

    @Override
    public long calculateRecipeEmc(Recipe recipe, String divisionStrategy) {
        if (recipe == null || recipe instanceof StonecuttingRecipe || recipe instanceof SmithingTrimRecipe) {
            return 0;
        }

        long totalEmc = 0;
        boolean isCooking = false;

        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
            for (RecipeChoice choice : shapedRecipe.getChoiceMap().values()) {
                if (choice == null) continue;
                long ingredientEmc = getChoiceEmc(choice);
                if (ingredientEmc == 0) return 0;
                totalEmc += ingredientEmc;
            }
        } else if (recipe instanceof ShapelessRecipe) {
            ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
            for (RecipeChoice choice : shapelessRecipe.getChoiceList()) {
                if (choice == null) continue;
                long ingredientEmc = getChoiceEmc(choice);
                if (ingredientEmc == 0) return 0;
                totalEmc += ingredientEmc;
            }
        } else if (recipe instanceof SmithingTransformRecipe) {
            SmithingTransformRecipe smithingRecipe = (SmithingTransformRecipe) recipe;
            long baseEmc = getChoiceEmc(smithingRecipe.getBase());
            long additionEmc = getChoiceEmc(smithingRecipe.getAddition());
            long templateEmc = getChoiceEmc(smithingRecipe.getTemplate());
            if (baseEmc == 0 || additionEmc == 0 || templateEmc == 0) return 0;
            totalEmc = baseEmc + additionEmc + templateEmc;

        } else if (recipe instanceof CookingRecipe) {
            isCooking = true;
            CookingRecipe<?> cookingRecipe = (CookingRecipe<?>) recipe;
            totalEmc = getChoiceEmc(cookingRecipe.getInputChoice());
        } else {
            return 0;
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

    private long getChoiceEmc(RecipeChoice choice) {
        if (choice == null) return 0;

        long lowestEmc = -1;

        List<ItemStack> itemChoices = new ArrayList<>();
        if (choice instanceof RecipeChoice.MaterialChoice) {
            for (Material mat : ((RecipeChoice.MaterialChoice) choice).getChoices()) {
                itemChoices.add(new ItemStack(mat));
            }
        } else if (choice instanceof RecipeChoice.ExactChoice) {
            itemChoices.addAll(((RecipeChoice.ExactChoice) choice).getChoices());
        } else {
            return 0;
        }

        for (ItemStack item : itemChoices) {
            long emc = getIngredientEmc(item);
            if (emc > 0) {
                if (lowestEmc == -1 || emc < lowestEmc) {
                    lowestEmc = emc;
                }
            }
        }

        return lowestEmc > 0 ? lowestEmc : 0;
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
        ProjectE.getInstance()
                .getLogger()
                .info("Loading " + items.size() + " EMC entries from config's default list...");

        for (Map<?, ?> itemMap : items) {
            if (itemMap == null) {
                ProjectE.getInstance().getLogger().warning("Found a null entry in config's EMC list. Skipping.");
                continue;
            }
            for (Map.Entry<?, ?> entry : itemMap.entrySet()) {
                try {
                    String configKey = entry.getKey().toString();

                    if (!(entry.getValue() instanceof Number)) {
                        ProjectE.getInstance()
                                .getLogger()
                                .warning("Invalid EMC value for '" + configKey + "': not a number. Skipping.");
                        continue;
                    }

                    Material material = getMaterial(configKey);
                    if (material == null) {
                        continue;
                    }
                    String correctItemKey = material.getKey().toString();
                    long emc = ((Number) entry.getValue()).longValue();

                    if (emc > 0) {
                        databaseManager.setEmc(correctItemKey, emc);
                    }
                } catch (Exception e) {
                    ProjectE.getInstance()
                            .getLogger()
                            .log(Level.SEVERE, "Failed to process an EMC entry: " + entry.toString(), e);
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
            for (Map.Entry<Character, ItemStack> entry :
                    ((ShapedRecipe) recipe).getIngredientMap().entrySet()) {
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
        } else if (recipe instanceof SmithingTransformRecipe) {
            debugInfo.add(" - 成分:");
            SmithingTransformRecipe smithingRecipe = (SmithingTransformRecipe) recipe;
            ItemStack base = smithingRecipe.getBase().getItemStack();
            ItemStack addition = smithingRecipe.getAddition().getItemStack();
            debugInfo.add("   - 基础: " + getItemKey(base) + ": " + getIngredientEmc(base) + " EMC");
            debugInfo.add("   - 添加物: " + getItemKey(addition) + ": " + getIngredientEmc(addition) + " EMC");
        } else if (recipe instanceof CookingRecipe) {
            debugInfo.add(" - 成分:");
            try {
                Method getInputMethod = recipe.getClass().getMethod("getInput");
                Object inputObject = getInputMethod.invoke(recipe);
                if (inputObject instanceof ItemStack) {
                    ItemStack input = (ItemStack) inputObject;
                    debugInfo.add("   - " + getItemKey(input) + ": " + getIngredientEmc(input) + " EMC");
                } else if (inputObject instanceof RecipeChoice.ExactChoice) {
                    ItemStack input = ((RecipeChoice.ExactChoice) inputObject).getItemStack();
                    debugInfo.add("   - " + getItemKey(input) + ": " + getIngredientEmc(input) + " EMC");
                } else if (inputObject instanceof RecipeChoice.MaterialChoice) {
                    debugInfo.add("   - (MaterialChoice, e.g., a tag)");
                }
            } catch (Exception e) {
                debugInfo.add("   - (无法获取成分)");
            }
        }
        return debugInfo;
    }

    @Override
    public Map<String, NamespacedKey> registerTransmutationTableRecipes() {
        Map<String, NamespacedKey> newKeys = new HashMap<>();
        ProjectE plugin = ProjectE.getInstance();
        ItemStack transmutationTable = new ItemStack(Material.PETRIFIED_OAK_SLAB);

        RecipeChoice stoneChoice = new RecipeChoice.MaterialChoice(
                Material.STONE, Material.COBBLESTONE, Material.ANDESITE, Material.DIORITE, Material.GRANITE);

        NamespacedKey key1 = new NamespacedKey(plugin, "transmutation_table_1");
        ShapedRecipe recipe1 = new ShapedRecipe(key1, transmutationTable);
        recipe1.shape("OSO", "SPS", "OSO");
        recipe1.setIngredient('O', Material.OBSIDIAN);
        recipe1.setIngredient('S', stoneChoice);
        recipe1.setIngredient('P', new RecipeChoice.ExactChoice(plugin.getPhilosopherStone()));
        Bukkit.addRecipe(recipe1);
        newKeys.put("transmutation_table_1", key1);

        NamespacedKey key2 = new NamespacedKey(plugin, "transmutation_table_2");
        ShapedRecipe recipe2 = new ShapedRecipe(key2, transmutationTable);
        recipe2.shape("SOS", "OPO", "SOS");
        recipe2.setIngredient('O', Material.OBSIDIAN);
        recipe2.setIngredient('S', stoneChoice);
        recipe2.setIngredient('P', new RecipeChoice.ExactChoice(plugin.getPhilosopherStone()));
        Bukkit.addRecipe(recipe2);
        newKeys.put("transmutation_table_2", key2);

        return newKeys;
    }

    @Override
    public void openSign(Player player, Sign sign) {
        player.openSign(sign);
    }
}
