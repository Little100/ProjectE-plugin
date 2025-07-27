package org.Little_100.projecte;

import org.Little_100.projecte.compatibility.VersionAdapter;
import org.Little_100.projecte.storage.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

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
        plugin.getLogger().info("开始计算EMC值...");
        versionAdapter.loadInitialEmcValues();

        for (int i = 0; i < 10; i++) {
            plugin.getLogger().info("EMC计算迭代第 " + (i + 1) + " 轮...");
            boolean changed = false;
            Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
            while (recipeIterator.hasNext()) {
                Recipe recipe = recipeIterator.next();
                if (calculateEmcForRecipe(recipe)) {
                    changed = true;
                }
            }
            if (!changed) {
                plugin.getLogger().info("EMC值已稳定，提前结束计算。");
                break;
            }
        }
        plugin.getLogger().info("EMC值计算完成。");
    }

    private boolean calculateEmcForRecipe(Recipe recipe) {
        ItemStack result = recipe.getResult();
        if (result == null || result.getType().isAir()) {
            return false;
        }

        String resultKey = versionAdapter.getItemKey(result);
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
            Recipe recipe = recipeIterator.next();
            ItemStack result = recipe.getResult();

            if (result != null && versionAdapter.getItemKey(result).equals(itemKey)) {
                long calculatedEmc = versionAdapter.calculateRecipeEmc(recipe, divisionStrategy);
                if (calculatedEmc > 0) {
                    if (lowestEmc == -1 || calculatedEmc < lowestEmc) {
                        lowestEmc = calculatedEmc;
                    }
                }
            }
        }

        currentlyCalculating.remove(itemKey);

        if (lowestEmc > 0) {
            databaseManager.setEmc(itemKey, lowestEmc);
            return lowestEmc;
        }

        return 0;
    }
}