package org.Little_100.projecte;

import org.Little_100.projecte.TransmutationTable.TransmutationGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.data.type.Slab;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.CraftingInventory;

public class PhilosopherStoneListener implements Listener {
    
    private final ProjectE plugin;
    
    public PhilosopherStoneListener(ProjectE plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        // 检查玩家是否手持贤者之石并执行其特定操作
        if (plugin.isPhilosopherStone(heldItem)) {
            // 潜行+右键打开工作台
            if (player.isSneaking() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                event.setCancelled(true);
                player.openWorkbench(null, true);
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                return;
            }

        }

        // 如果贤者之石的操作没有被触发，再检查是否右键单击了石化橡木台阶
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.PETRIFIED_OAK_SLAB) {
            if (plugin.getConfig().getBoolean("TransmutationTable.enabled", true)) {
                if (!player.hasPermission("philosophersstone.interact.transmutationtable")) {
                    player.sendMessage(ChatColor.RED + "你没有权限使用转换桌。");
                    return;
                }
                if (event.getClickedBlock().getBlockData() instanceof Slab) {
                    Slab slab = (Slab) event.getClickedBlock().getBlockData();
                    if (slab.getType() == Slab.Type.BOTTOM) {
                        if (!player.isSneaking()) {
                            event.setCancelled(true);
                            new TransmutationGUI(player).open();
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        Recipe recipe = event.getRecipe();
        
        // 检查是否是贤者之石配方
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
            if (shapedRecipe.getKey().equals(plugin.getPhilosopherStoneKey())) {
                // 确保结果是正确的贤者之石
                inventory.setResult(plugin.getPhilosopherStone());
            }
        }
    }
    
    // 确保贤者之石不被消耗的事件处理
    @EventHandler
    public void onCraftItem(org.bukkit.event.inventory.CraftItemEvent event) {
        if (event.isCancelled() || !(event.getWhoClicked() instanceof Player)) {
            return;
        }

        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();
        ItemStack philosopherStone = null;
        int stoneSlot = -1;

        for (int i = 0; i < matrix.length; i++) {
            if (plugin.isPhilosopherStone(matrix[i])) {
                philosopherStone = matrix[i].clone();
                stoneSlot = i;
                break;
            }
        }

        if (philosopherStone == null) {
            return; // 没有找到贤者之石
        }

        Player player = (Player) event.getWhoClicked();
        NamespacedKey recipeKey = null;
        if (event.getRecipe() instanceof ShapedRecipe) {
            recipeKey = ((ShapedRecipe) event.getRecipe()).getKey();
        } else if (event.getRecipe() instanceof ShapelessRecipe) {
            recipeKey = ((ShapelessRecipe) event.getRecipe()).getKey();
        }

        if (recipeKey != null && recipeKey.equals(plugin.getPhilosopherStoneKey())) {
            if (hasPhilosopherStone(player)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "你已经拥有一个贤者之石了！");
            }
            return;
        }

        if (event.isShiftClick()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "使用贤者之石合成时不支持Shift点击！");
            return;
        }
        
        final int finalStoneSlot = stoneSlot + 1;
        final ItemStack finalPhilosopherStone = philosopherStone;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            inventory.setItem(finalStoneSlot, finalPhilosopherStone);
            player.updateInventory();
        }, 1L);
    }
    
    private boolean hasPhilosopherStone(org.bukkit.entity.HumanEntity player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (plugin.isPhilosopherStone(item)) {
                return true;
            }
        }
        return false;
    }
    
}