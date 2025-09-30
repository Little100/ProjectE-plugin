package org.Little_100.projecte.listeners;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.util.InventoryViewHelper;
import org.Little_100.projecte.gui.ToolChargeGUI;
import org.Little_100.projecte.tools.ToolManager;
import org.Little_100.projecte.util.Constants;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ToolChargeGUIListener implements Listener {

    private final ProjectE plugin;

    public ToolChargeGUIListener(ProjectE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedInventory == null || clickedItem == null || !clickedItem.hasItemMeta()) return;

        String expectedTitle = plugin.getLanguageManager().get("clientside.tool_charge_gui.title");
        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().info("[DEBUG] GUI点击事件触发，标题: " + InventoryViewHelper.getTitle(event) + ", 期望: " + expectedTitle);
        }
        if (!InventoryViewHelper.getTitle(event).equals(expectedTitle)) {
            return;
        }

        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().info("[DEBUG] 确认是工具界面，取消事件");
        }
        event.setCancelled(true);

        ItemMeta clickedMeta = clickedItem.getItemMeta();
        if (clickedMeta == null) return;

        PersistentDataContainer clickedContainer = clickedMeta.getPersistentDataContainer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        ToolManager toolManager = plugin.getToolManager();

        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().info("[DEBUG] 点击的物品meta keys: " + clickedContainer.getKeys());
            plugin.getLogger().info("[DEBUG] 手持工具: " + (tool != null ? tool.getType() : "null"));
        }
        
        if (!toolManager.isProjectETool(tool)) {
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("[DEBUG] 手持物品不是ProjectE工具，跳过");
            }
            return;
        }

        ItemMeta toolMeta = tool.getItemMeta();
        if (toolMeta == null) return;
        PersistentDataContainer toolContainer = toolMeta.getPersistentDataContainer();

        if (clickedContainer.has(Constants.CHARGE_LEVEL_KEY, PersistentDataType.INTEGER)) {
            int newCharge = clickedContainer.getOrDefault(Constants.CHARGE_LEVEL_KEY, PersistentDataType.INTEGER, 0);
            int oldCharge = toolContainer.getOrDefault(Constants.CHARGE_KEY, PersistentDataType.INTEGER, 0);

            if (newCharge != oldCharge) {
                if (toolManager.isDarkMatterShovel(tool) && newCharge > 1) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }

                toolContainer.set(Constants.CHARGE_KEY, PersistentDataType.INTEGER, newCharge);
                tool.setItemMeta(toolMeta);
                updateDurability(tool, newCharge);
                toolManager.updateToolEfficiency(tool);

                if (toolManager.isRedMatterHammer(tool)) {
                    toolManager.updateHammerAttackDamage(tool);
                }

                if (toolManager.isDarkMatterSword(tool) || toolManager.isRedMatterSword(tool)) {
                    toolManager.updateSwordAttackDamage(tool);
                }

                if (toolManager.isRedMatterKatar(tool)) {
                    toolManager.updateKatarAttackDamage(tool);
                }

                if (newCharge > oldCharge) {
                    player.playSound(player.getLocation(), "projecte:custom.pecharge", 1.0f, 1.0f);
                } else {
                    player.playSound(player.getLocation(), "projecte:custom.peuncharge", 1.0f, 1.0f);
                }

                new ToolChargeGUI(plugin, player, tool).open();
                player.getInventory().setItemInMainHand(tool);
            }
        }

        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().info("[DEBUG] 检查是否有KATAR_MODE_KEY: " + clickedContainer.has(Constants.KATAR_MODE_KEY, PersistentDataType.INTEGER));
            plugin.getLogger().info("[DEBUG] 是否是拳剑: " + toolManager.isRedMatterKatar(tool));
        }
        
        if (clickedContainer.has(Constants.KATAR_MODE_KEY, PersistentDataType.INTEGER)) {
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("[DEBUG] 检测到拳剑模式切换点击");
            }
            int newMode = clickedContainer.get(Constants.KATAR_MODE_KEY, PersistentDataType.INTEGER);
            int oldMode = toolContainer.getOrDefault(Constants.KATAR_MODE_KEY, PersistentDataType.INTEGER, 0);
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("[DEBUG] 当前模式: " + oldMode + ", 新模式: " + newMode);
            }

            if (newMode != oldMode) {
                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().info("[DEBUG] 模式不同，开始切换");
                }
                toolContainer.set(Constants.KATAR_MODE_KEY, PersistentDataType.INTEGER, newMode);
                tool.setItemMeta(toolMeta);
                toolManager.updateLore(tool);
                toolManager.updateKatarAttackDamage(tool);

                player.getInventory().setItemInMainHand(tool);
                
                // 发送模式切换确认消息
                String modeName = (newMode == 0) 
                    ? plugin.getLanguageManager().get("clientside.red_matter_katar.mode_all")
                    : plugin.getLanguageManager().get("clientside.red_matter_katar.mode_hostile");
                player.sendMessage(plugin.getLanguageManager().get("clientside.red_matter_katar.mode_prefix") + " " + modeName);
                
                // 播放切换音效
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);

                new ToolChargeGUI(plugin, player, player.getInventory().getItemInMainHand()).open();
            }
        }
    }

    private void updateDurability(ItemStack tool, int newCharge) {
        ItemMeta meta = tool.getItemMeta();
        if (!(meta instanceof Damageable)) return;
        Damageable damageable = (Damageable) meta;
        ToolManager toolManager = plugin.getToolManager();

        int maxDurability = tool.getType().getMaxDurability();
        int newDamage = 0;

        if (toolManager.isRedMatterTool(tool)) {
            switch (newCharge) {
                case 0:
                    newDamage = maxDurability - 2;
                    break;
                case 1:
                    newDamage = maxDurability * 2 / 3;
                    break;
                case 2:
                    newDamage = maxDurability / 3;
                    break;
                case 3:
                    newDamage = maxDurability / 4;
                    break;
                case 4:
                    newDamage = 1;
                    break;
            }
        } else {
            switch (newCharge) {
                case 0:
                    newDamage = maxDurability - 2;
                    break;
                case 1:
                    newDamage = maxDurability / 2;
                    break;
                case 2:
                    newDamage = 1;
                    break;
            }
        }
        damageable.setDamage(newDamage);
        tool.setItemMeta(meta);
        toolManager.updateLore(tool);
    }
}
