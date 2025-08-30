package org.Little_100.projecte;

import org.Little_100.projecte.Armor.ArmorManager;
import org.Little_100.projecte.Tools.ToolManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public class CraftingListener implements Listener {

    private final ProjectE plugin;
    private final ToolManager toolManager;
    private final ArmorManager armorManager;

    public CraftingListener(ProjectE plugin) {
        this.plugin = plugin;
        this.toolManager = plugin.getToolManager();
        this.armorManager = plugin.getArmorManager();
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        ItemStack result = inventory.getResult();

        if (result == null || result.getType() == Material.AIR) {
            return;
        }

        boolean isRedMatterUpgrade = toolManager.isRedMatterTool(result) || armorManager.isRedMatterArmor(result);

        if (isRedMatterUpgrade) {
            boolean invalidIngredientFound = false;
            for (ItemStack item : inventory.getMatrix()) {
                if (item == null || item.getType() == Material.AIR) {
                    continue;
                }

                if (isToolMaterial(item.getType())) {
                    if (!toolManager.isDarkMatterTool(item)) {
                        invalidIngredientFound = true;
                        break;
                    }
                }

                if (isArmorMaterial(item.getType())) {
                    if (!armorManager.isDarkMatterArmor(item)) {
                        invalidIngredientFound = true;
                        break;
                    }
                }
            }

            if (invalidIngredientFound) {
                inventory.setResult(null);
            }
        }
    }

    private boolean isToolMaterial(Material material) {
        switch (material) {
            case DIAMOND_PICKAXE:
            case DIAMOND_AXE:
            case DIAMOND_SHOVEL:
            case DIAMOND_HOE:
            case DIAMOND_SWORD:
            case SHEARS:
                return true;
            default:
                return false;
        }
    }

    private boolean isArmorMaterial(Material material) {
        switch (material) {
            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
                return true;
            default:
                return false;
        }
    }
}