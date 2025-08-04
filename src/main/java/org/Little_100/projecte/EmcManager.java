package org.Little_100.projecte;

import org.Little_100.projecte.compatibility.VersionAdapter;
import org.Little_100.projecte.storage.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
        this.recipeConflictStrategy = plugin.getConfig().getString("TransmutationTable.EMC.recipeConflictStrategy", "lowest").toLowerCase();
        this.divisionStrategy = plugin.getConfig().getString("TransmutationTable.EMC.divisionStrategy", "floor").toLowerCase();
    }

    public void calculateAndStoreEmcValues() {
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
        plugin.getLogger().info("EMC value calculation completed.");
    }

    private boolean calculateEmcForRecipe(Recipe recipe) {
        ItemStack result = recipe.getResult();
        if (result == null || result.getType().isAir()) {
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
        long existingEmc = databaseManager.getEmc(resultKey);
        long newEmc = versionAdapter.calculateRecipeEmc(recipe, divisionStrategy);

        if (newEmc <= 0) {
            return false;
        }

        if (existingEmc <= 0) {
            databaseManager.setEmc(resultKey, newEmc);
            return true;
        } else {
            boolean updated = false;
            if ("lowest".equals(recipeConflictStrategy) && newEmc < existingEmc) {
                databaseManager.setEmc(resultKey, newEmc);
                updated = true;
            } else if ("highest".equals(recipeConflictStrategy) && newEmc > existingEmc) {
                databaseManager.setEmc(resultKey, newEmc);
                updated = true;
            }
            return updated;
        }
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

                if (result != null && getItemKey(result).equals(itemKey)) {
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
                    return versionAdapter.getItemKey(item) + "{projecte_id:\"" + projecteId + "\"}";
                }
            }
        }
        return versionAdapter.getItemKey(item);
    }
}