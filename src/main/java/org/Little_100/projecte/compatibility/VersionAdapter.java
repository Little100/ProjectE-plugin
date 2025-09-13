package org.Little_100.projecte.compatibility;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.List;
import java.util.Map;

public interface VersionAdapter {
    Material getMaterial(String name);

    String getItemKey(ItemStack itemStack);

    long calculateRecipeEmc(Recipe recipe, String divisionStrategy);

    void loadInitialEmcValues();

    List<String> getRecipeDebugInfo(Recipe recipe, String divisionStrategy);

    Map<String, org.bukkit.NamespacedKey> registerTransmutationTableRecipes();

    void openSign(org.bukkit.entity.Player player, org.bukkit.block.Sign sign);
}