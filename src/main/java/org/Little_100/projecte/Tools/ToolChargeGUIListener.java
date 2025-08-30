package org.Little_100.projecte.Tools;

import org.Little_100.projecte.ProjectE;
import org.bukkit.NamespacedKey;
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
        if (!event.getView().getTitle().equals(expectedTitle)) {
            return;
        }

        event.setCancelled(true);

        ItemMeta clickedMeta = clickedItem.getItemMeta();
        if (clickedMeta == null) return;

        PersistentDataContainer clickedContainer = clickedMeta.getPersistentDataContainer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        ToolManager toolManager = plugin.getToolManager();

        if (!toolManager.isProjectETool(tool)) return;

        ItemMeta toolMeta = tool.getItemMeta();
        if (toolMeta == null) return;
        PersistentDataContainer toolContainer = toolMeta.getPersistentDataContainer();

        NamespacedKey chargeKey = new NamespacedKey(plugin, "charge_level");
        if (clickedContainer.has(chargeKey, PersistentDataType.INTEGER)) {
            int newCharge = clickedContainer.get(chargeKey, PersistentDataType.INTEGER);
            int oldCharge = toolContainer.getOrDefault(new NamespacedKey(plugin, "projecte_charge"), PersistentDataType.INTEGER, 0);

            if (newCharge != oldCharge) {
                if (toolManager.isDarkMatterShovel(tool) && newCharge > 1) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }

                toolContainer.set(new NamespacedKey(plugin, "projecte_charge"), PersistentDataType.INTEGER, newCharge);
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

        NamespacedKey katarModeKey = new NamespacedKey(plugin, "katar_mode");
        if (clickedContainer.has(katarModeKey, PersistentDataType.INTEGER)) {
            int newMode = clickedContainer.get(katarModeKey, PersistentDataType.INTEGER);
            int oldMode = toolContainer.getOrDefault(new NamespacedKey(plugin, "projecte_katar_mode"), PersistentDataType.INTEGER, 0);

            if (newMode != oldMode) {
                toolContainer.set(new NamespacedKey(plugin, "projecte_katar_mode"), PersistentDataType.INTEGER, newMode);
                tool.setItemMeta(toolMeta);
                toolManager.updateLore(tool);
                
                player.getInventory().setItemInMainHand(tool);
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, 1.5f);
                
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
                case 0: newDamage = maxDurability - 2; break;
                case 1: newDamage = maxDurability * 2 / 3; break;
                case 2: newDamage = maxDurability / 3; break;
                case 3: newDamage = maxDurability / 4; break;
                case 4: newDamage = 1; break;
            }
        } else {
            switch (newCharge) {
                case 0: newDamage = maxDurability - 2; break;
                case 1: newDamage = maxDurability / 2; break;
                case 2: newDamage = 1; break;
            }
        }
        damageable.setDamage(newDamage);
        tool.setItemMeta(meta);
        toolManager.updateLore(tool);
    }
}