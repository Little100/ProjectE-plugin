package org.Little_100.projecte.compatibility.version;

import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public interface VersionAdapter {
    static VersionAdapter getInstance() {
        return VersionMatcher.getAdapter();
    }

    Material getMaterial(String name);

    String getItemKey(ItemStack itemStack);

    long calculateRecipeEmc(Recipe recipe, String divisionStrategy);

    void loadInitialEmcValues();

    List<String> getRecipeDebugInfo(Recipe recipe, String divisionStrategy);

    Map<String, NamespacedKey> registerTransmutationTableRecipes();

    void openSign(Player player, Sign sign);
}
