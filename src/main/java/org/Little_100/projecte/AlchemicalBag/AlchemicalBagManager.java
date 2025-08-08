package org.Little_100.projecte.AlchemicalBag;

import org.Little_100.projecte.LanguageManager;
import org.Little_100.projecte.ProjectE;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.HashMap;
import java.util.Map;

public class AlchemicalBagManager implements Listener {

    private final ProjectE plugin;
    private final InventoryManager inventoryManager;
    private final LanguageManager languageManager;

    public AlchemicalBagManager(ProjectE plugin) {
        this.plugin = plugin;
        this.inventoryManager = new InventoryManager(plugin, this);
        this.languageManager = plugin.getLanguageManager();
    }

    public void register() {
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerInteractListener(plugin, this, inventoryManager),
                plugin);
        Bukkit.getServer().getPluginManager().registerEvents(inventoryManager, plugin);
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        removeVanillaRecipes();
        // 从recipe.yml中读取合成表
    }

    public void unregister() {
        // 取消注册事件监听器
    }

    private void removeVanillaRecipes() {
        try {
            Bukkit.removeRecipe(NamespacedKey.minecraft("leather_horse_armor"));
            plugin.getLogger().info("Successfully removed vanilla leather horse armor recipe.");
        } catch (Exception e) {
            plugin.getLogger().warning("Could not remove vanilla leather horse armor recipe: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe == null || !(recipe instanceof ShapelessRecipe)) {
            return;
        }

        NamespacedKey key = ((ShapelessRecipe) recipe).getKey();
        if (!key.equals(new NamespacedKey(plugin, "alchemicalbag_dyeing"))) {
            return;
        }

        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();

        ItemStack bag = null;
        ItemStack dye = null;

        for (ItemStack item : matrix) {
            if (item != null && !item.getType().isAir()) {
                if (item.getType() == Material.LEATHER_HORSE_ARMOR) {
                    bag = item;
                } else if (item.getType().name().endsWith("_DYE")) {
                    dye = item;
                }
            }
        }

        if (bag == null || dye == null) {
            inventory.setResult(null);
            return;
        }

        LeatherArmorMeta bagMeta = (LeatherArmorMeta) bag.getItemMeta();
        if (bagMeta == null) {
            inventory.setResult(null);
            return;
        }

        DyeColor dyeColor = getDyeColor(dye.getType());
        if (dyeColor == null) {
            inventory.setResult(null);
            return;
        }

        ItemStack result = bag.clone();
        LeatherArmorMeta resultMeta = (LeatherArmorMeta) result.getItemMeta();
        resultMeta.setColor(dyeColor.getColor());
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("color", PlayerInteractListener.getChatColor(dyeColor.name()).toString());
        placeholders.put("color_name", dyeColor.name());
        resultMeta.setDisplayName(languageManager.get("clientside.alchemical_bag.colored_name", placeholders));
        result.setItemMeta(resultMeta);

        inventory.setResult(result);
    }

    private DyeColor getDyeColor(Material material) {
        try {
            return DyeColor.valueOf(material.name().replace("_DYE", ""));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}