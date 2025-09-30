package org.Little_100.projecte.listeners;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.Recipe;

import java.util.Iterator;

public class ServerLoadListener implements Listener {

    private final ProjectE plugin;

    public ServerLoadListener(ProjectE plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerLoad(ServerLoadEvent event) {
        if (event.getType() != ServerLoadEvent.LoadType.STARTUP) {
            return;
        }

        plugin.getLogger().info("Server fully loaded. Recalculating EMC for PDC items...");
        
        // 延迟1秒执行确保所有插件都已完全加载
        plugin.getSchedulerAdapter().runTaskLater(() -> {
            try {
                // 重新计算所有配方的EMC，这次所有插件的PDC物品都应该已经注册
                recalculatePdcItemsEmc();
                plugin.getLogger().info("PDC items EMC recalculation completed.");
            } catch (Exception e) {
                plugin.getLogger().severe("Error during PDC items EMC recalculation: " + e.getMessage());
                e.printStackTrace();
            }
        }, 20L); // 1秒后执行
    }

    private void recalculatePdcItemsEmc() {
        int pdcRecipesFound = 0;
        boolean debug = plugin.getConfig().getBoolean("debug");

        // 先统计有多少PDC配方
        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
        while (recipeIterator.hasNext()) {
            try {
                Recipe recipe = recipeIterator.next();
                if (recipe.getResult() == null || recipe.getResult().getType().isAir()) {
                    continue;
                }

                // 检查结果物品是否有PDC
                if (plugin.getEmcManager().isPdcItem(recipe.getResult())) {
                    pdcRecipesFound++;
                    if (debug) {
                        String itemKey = plugin.getEmcManager().getItemKey(recipe.getResult());
                        plugin.getLogger().info("[PDC Recipe Found] " + itemKey);
                    }
                }
            } catch (Exception e) {
                // 忽略损坏的配方
            }
        }

        if (pdcRecipesFound > 0) {
            plugin.getLogger().info("Found " + pdcRecipesFound + " PDC recipes. Starting EMC calculation...");
            
            java.util.Set<String> calculatedItems = new java.util.HashSet<>();
            
            // 进行额外的迭代计算来处理PDC物品
            for (int i = 0; i < 5; i++) {
                plugin.getLogger().info("PDC EMC calculation iteration " + (i + 1) + "...");
                boolean changed = false;
                
                recipeIterator = Bukkit.recipeIterator();
                while (recipeIterator.hasNext()) {
                    try {
                        Recipe recipe = recipeIterator.next();
                        if (recipe.getResult() == null || recipe.getResult().getType().isAir()) {
                            continue;
                        }

                        // 只处理PDC物品
                        if (plugin.getEmcManager().isPdcItem(recipe.getResult())) {
                            String itemKey = plugin.getEmcManager().getItemKey(recipe.getResult());
                            long oldEmc = plugin.getDatabaseManager().getEmc(itemKey);
                            
                            // 计算配方的EMC
                            long recipeEmc = plugin.getVersionAdapter().calculateRecipeEmc(recipe, 
                                plugin.getConfig().getString("TransmutationTable.EMC.divisionStrategy", "floor"));
                            
                            if (debug && recipeEmc == 0 && oldEmc == 0) {
                                // 调试：显示为什么无法计算EMC
                                plugin.getLogger().warning("[PDC EMC Debug] Cannot calculate EMC for: " + itemKey);
                                plugin.getLogger().warning("  Recipe type: " + recipe.getClass().getSimpleName());
                                
                                // 显示原料信息
                                if (recipe instanceof org.bukkit.inventory.ShapedRecipe) {
                                    plugin.getLogger().warning("  Ingredients:");
                                    org.bukkit.inventory.ShapedRecipe shaped = (org.bukkit.inventory.ShapedRecipe) recipe;
                                    for (org.bukkit.inventory.ItemStack ingredient : shaped.getIngredientMap().values()) {
                                        if (ingredient != null && !ingredient.getType().isAir()) {
                                            String ingKey = plugin.getEmcManager().getItemKey(ingredient);
                                            long ingEmc = plugin.getDatabaseManager().getEmc(ingKey);
                                            plugin.getLogger().warning("    - " + ingKey + ": " + ingEmc + " EMC");
                                        }
                                    }
                                } else if (recipe instanceof org.bukkit.inventory.ShapelessRecipe) {
                                    plugin.getLogger().warning("  Ingredients:");
                                    org.bukkit.inventory.ShapelessRecipe shapeless = (org.bukkit.inventory.ShapelessRecipe) recipe;
                                    for (org.bukkit.inventory.ItemStack ingredient : shapeless.getIngredientList()) {
                                        if (ingredient != null && !ingredient.getType().isAir()) {
                                            String ingKey = plugin.getEmcManager().getItemKey(ingredient);
                                            long ingEmc = plugin.getDatabaseManager().getEmc(ingKey);
                                            plugin.getLogger().warning("    - " + ingKey + ": " + ingEmc + " EMC");
                                        }
                                    }
                                }
                            }
                            
                            if (recipeEmc > 0) {
                                // 根据策略更新EMC
                                String strategy = plugin.getConfig().getString("TransmutationTable.EMC.recipeConflictStrategy", "lowest");
                                boolean shouldUpdate = false;
                                
                                if (oldEmc <= 0) {
                                    // 还没有EMC值，直接设置
                                    shouldUpdate = true;
                                } else if ("lowest".equals(strategy) && recipeEmc < oldEmc) {
                                    // 策略是最低值，且新值更低
                                    shouldUpdate = true;
                                } else if ("highest".equals(strategy) && recipeEmc > oldEmc) {
                                    // 策略是最高值，且新值更高
                                    shouldUpdate = true;
                                }
                                
                                if (shouldUpdate) {
                                    plugin.getDatabaseManager().setEmc(itemKey, recipeEmc);
                                    changed = true;
                                    calculatedItems.add(itemKey);
                                    if (debug) {
                                        plugin.getLogger().info("[PDC EMC Calculated] " + itemKey + " = " + recipeEmc + " EMC");
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        // 忽略损坏的配方
                    }
                }
                
                if (!changed) {
                    plugin.getLogger().info("PDC EMC values stabilized, calculation ended early.");
                    break;
                }
            }

            plugin.getLogger().info("PDC items EMC calculation completed. " + calculatedItems.size() + " unique items calculated.");
            
            // 未计算的PDC物品
            int uncalculatedCount = 0;
            recipeIterator = Bukkit.recipeIterator();
            while (recipeIterator.hasNext()) {
                try {
                    Recipe recipe = recipeIterator.next();
                    if (recipe.getResult() != null && !recipe.getResult().getType().isAir() 
                        && plugin.getEmcManager().isPdcItem(recipe.getResult())) {
                        String itemKey = plugin.getEmcManager().getItemKey(recipe.getResult());
                        long emc = plugin.getDatabaseManager().getEmc(itemKey);
                        if (emc <= 0) {
                            uncalculatedCount++;
                            if (debug) {
                                plugin.getLogger().warning("[PDC EMC] Item without EMC: " + itemKey);
                            }
                        }
                    }
                } catch (Exception e) {
                    // 忽略
                }
            }
            
            if (uncalculatedCount > 0) {
                plugin.getLogger().warning("Warning: " + uncalculatedCount + " PDC items still don't have EMC values.");
                plugin.getLogger().warning("This may be due to missing ingredient EMC or circular dependencies.");
                if (!debug) {
                    plugin.getLogger().warning("Enable debug mode in config.yml to see which items are affected.");
                }
            }
        } else {
            plugin.getLogger().info("No PDC recipes found. Skipping PDC EMC calculation.");
        }
    }
}
