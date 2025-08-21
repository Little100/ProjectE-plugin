package org.Little_100.projecte;

import org.Little_100.projecte.Tools.KleinStar.KleinStarManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public class CraftingListener implements Listener {

    public CraftingListener(ProjectE plugin) {
        // The logic has been moved to RecipeManager.
        // This listener might be used for other purposes in the future.
    }

    // No-op event handler, can be removed if class is not used for anything else.
    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        // All crafting logic is now handled by RecipeManager at the source.
    }
}