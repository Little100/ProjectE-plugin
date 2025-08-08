package org.Little_100.projecte.compatibility;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public interface VersionAdapter {
    Material getMaterial(String name);

    String getItemKey(ItemStack itemStack);

    long calculateRecipeEmc(Recipe recipe, String divisionStrategy);

    void loadInitialEmcValues();

    java.util.List<String> getRecipeDebugInfo(Recipe recipe, String divisionStrategy);

    java.util.Map<String, org.bukkit.NamespacedKey> registerTransmutationTableRecipes();

    boolean isModern();
}