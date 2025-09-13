package org.Little_100.projecte.managers;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.Little_100.projecte.Debug;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.compatibility.VersionAdapter;
import org.Little_100.projecte.storage.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

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

        if (plugin.isPdcExcluded() && isPdcItem(result)) {
            return false;
        }

        if (plugin.isOnlyMcItems()) {
            if (recipe instanceof ShapedRecipe) {
                for (ItemStack ingredient :
                        ((ShapedRecipe) recipe).getIngredientMap().values()) {
                    if (isPdcItem(ingredient)) return false;
                }
            } else if (recipe instanceof ShapelessRecipe) {
                for (ItemStack ingredient : ((ShapelessRecipe) recipe).getIngredientList()) {
                    if (isPdcItem(ingredient)) return false;
                }
            } else if (recipe instanceof CookingRecipe) {
                if (isPdcItem(((CookingRecipe<?>) recipe).getInput())) return false;
            }
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
            java.util.Map<String, String> placeholders = new java.util.HashMap<>();
            placeholders.put("item", resultKey);
            placeholders.put("emc", String.valueOf(newEmc));
            Debug.log("debug.emc.item_emc_info", placeholders);
            return true;
        } else {
            boolean updated = false;
            if ("lowest".equals(recipeConflictStrategy) && newEmc < existingEmc) {
                databaseManager.setEmc(resultKey, newEmc);
                java.util.Map<String, String> placeholders = new java.util.HashMap<>();
                placeholders.put("item", resultKey);
                placeholders.put("emc", String.valueOf(newEmc));
                Debug.log("debug.emc.item_emc_info", placeholders);
                updated = true;
            } else if ("highest".equals(recipeConflictStrategy) && newEmc > existingEmc) {
                databaseManager.setEmc(resultKey, newEmc);
                java.util.Map<String, String> placeholders = new java.util.HashMap<>();
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

        if (plugin.isOnlyMcItems() && item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
            return databaseManager.getEmc(getItemKey(item));
        }

        if (plugin.isPdcExcluded() && isPdcItem(item)) {
            return databaseManager.getEmc(getItemKey(item));
        }

        return getEmc(getItemKey(item));
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
                NamespacedKey idKey = new NamespacedKey(plugin, "projecte_id");
                if (container.has(idKey, PersistentDataType.STRING)) {
                    String projecteId = container.get(idKey, PersistentDataType.STRING);
                    if ("transmutation_tablet_book".equals(projecteId)) {
                        // Check for CustomModelData to be more specific
                        if (meta.hasCustomModelData() && meta.getCustomModelData() == 1) {
                            return "projecte:" + projecteId;
                        }
                    } else {
                        return "projecte:" + projecteId;
                    }
                }

                NamespacedKey kleinStarKey = new NamespacedKey(plugin, "klein_star_level");
                if (container.has(kleinStarKey, PersistentDataType.INTEGER)) {
                    int level = container.get(kleinStarKey, PersistentDataType.INTEGER);
                    if (level > 0) {
                        String levelName = getLevelName(level);
                        if (levelName != null) {
                            return "projecte:klein_star_" + levelName;
                        }
                    }
                }

                NamespacedKey darkMatterFurnaceKey = new NamespacedKey(plugin, "dark_matter_furnace");
                NamespacedKey redMatterFurnaceKey = new NamespacedKey(plugin, "red_matter_furnace");
                NamespacedKey alchemicalChestKey = new NamespacedKey(plugin, "alchemical_chest");
                NamespacedKey energyCondenserKey = new NamespacedKey(plugin, "energy_condenser");
                NamespacedKey energyCondenserMK2Key = new NamespacedKey(plugin, "energy_condenser_mk2");

                if (container.has(darkMatterFurnaceKey, PersistentDataType.BYTE)) {
                    return "projecte:dark_matter_furnace";
                }
                if (container.has(redMatterFurnaceKey, PersistentDataType.BYTE)) {
                    return "projecte:red_matter_furnace";
                }
                if (container.has(alchemicalChestKey, PersistentDataType.BYTE)) {
                    return "projecte:alchemical_chest";
                }
                if (container.has(energyCondenserKey, PersistentDataType.BYTE)) {
                    return "projecte:energy_condenser";
                }
                if (container.has(energyCondenserMK2Key, PersistentDataType.BYTE)) {
                    return "projecte:energy_condenser_mk2";
                }
            }
        }
        return versionAdapter.getItemKey(item);
    }

    public String getEffectiveItemKey(ItemStack item) {
        if (plugin.isPdcExcluded()) {
            return versionAdapter.getItemKey(item);
        }
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
        NamespacedKey idKey = new NamespacedKey(plugin, "projecte_id");
        return container.has(idKey, PersistentDataType.STRING);
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
        NamespacedKey idKey = new NamespacedKey(plugin, "projecte_id");
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
}
