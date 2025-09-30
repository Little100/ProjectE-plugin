package org.Little_100.projecte.listeners;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.util.InventoryViewHelper;
import org.Little_100.projecte.gui.PhilosopherStoneGUI;
import org.Little_100.projecte.util.Constants;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PhilosopherStoneGUIListener implements Listener {

    private final ProjectE plugin;

    // 数据键
    private static final NamespacedKey CURRENT_MODE_KEY = new NamespacedKey(ProjectE.getInstance(), "current_mode");

    public PhilosopherStoneGUIListener(ProjectE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String guiTitle = plugin.getLanguageManager().get("clientside.philosopher_stone_gui.title");

        // 检查是否为贤者之石GUI
        if (!InventoryViewHelper.getTitle(event).equals(guiTitle)) {
            return;
        }

        // 在贤者之石GUI中，默认取消所有点击事件，然后根据点击的槽位有选择地处理
        event.setCancelled(true);

        int slot = event.getRawSlot();
        ItemStack clickedItem = event.getCurrentItem();

        // 如果点击的是玩家背包，不处理
        if (slot >= event.getInventory().getSize()) {
            event.setCancelled(false); // 允许玩家操作自己的背包
            return;
        }

        // 处理充能系统点击
        if (isChargeSlot(slot)) {
            handleChargeClick(player, clickedItem);
        }
        // 处理模式选择点击
        else if (isModeSlot(slot)) {
            handleModeClick(player, clickedItem);
        }
        // 处理工作台入口点击
        else if (slot == PhilosopherStoneGUI.CRAFTING_TABLE_SLOT) {
            handleCraftingTableClick(player);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String guiTitle = plugin.getLanguageManager().get("clientside.philosopher_stone_gui.title");
        if (InventoryViewHelper.getTitle(event).equals(guiTitle)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {}

    /**
     * 检查是否为充能槽位。
     */
    private boolean isChargeSlot(int slot) {
        if (slot == 10) return true;
        for (int i = 11; i <= 14; i++) {
            if (slot == i) return true;
        }
        return false;
    }

    /**
     * 检查是否为模式槽位。
     */
    private boolean isModeSlot(int slot) {
        return slot >= 20 && slot <= 22;
    }

    /**
     * 处理充能点击。
     */
    private void handleChargeClick(Player player, ItemStack clickedItem) {
        if (clickedItem == null || clickedItem.getItemMeta() == null) {
            return;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        if (!meta.getPersistentDataContainer().has(Constants.CHARGE_LEVEL_KEY, PersistentDataType.INTEGER)) {
            return;
        }

        int clickedLevel =
                meta.getPersistentDataContainer().get(Constants.CHARGE_LEVEL_KEY, PersistentDataType.INTEGER);
        int newChargeLevel;

        if (clickedLevel == -1) {
            int currentChargeLevel = PhilosopherStoneGUI.getChargeLevel(player);
            newChargeLevel = (currentChargeLevel + 1) % (4 + 1);
        } else {
            newChargeLevel = clickedLevel;
        }

        PhilosopherStoneGUI.setChargeLevel(player, newChargeLevel);
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);

        plugin.getSchedulerAdapter()
                .runTaskLater(
                        () -> {
                            new PhilosopherStoneGUI(plugin, player).open();
                        },
                        1L);
    }

    /**
     * 处理模式点击。
     */
    private void handleModeClick(Player player, ItemStack clickedItem) {
        if (clickedItem == null || clickedItem.getItemMeta() == null) {
            return;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        if (!meta.getPersistentDataContainer().has(CURRENT_MODE_KEY, PersistentDataType.STRING)) {
            return;
        }

        String modeString = meta.getPersistentDataContainer().get(CURRENT_MODE_KEY, PersistentDataType.STRING);
        PhilosopherStoneGUI.StoneMode newMode = PhilosopherStoneGUI.StoneMode.valueOf(modeString);

        if (PhilosopherStoneGUI.getCurrentMode(player) != newMode) {
            PhilosopherStoneGUI.setCurrentMode(player, newMode);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            String message = plugin.getLanguageManager()
                    .get("clientside.philosopher_stone.mode_change")
                    .replace("{mode}", newMode.getDisplayName(plugin));
            player.sendMessage(ChatColor.GREEN + message);
        }

        plugin.getSchedulerAdapter()
                .runTaskLater(
                        () -> {
                            new PhilosopherStoneGUI(plugin, player).open();
                        },
                        1L);
    }

    /**
     * 处理工作台入口点击。
     */
    private void handleCraftingTableClick(Player player) {
        // 关闭
        player.closeInventory();

        plugin.getSchedulerAdapter()
                .runTaskLater(
                        () -> {
                            player.openWorkbench(null, true);
                        },
                        1L);
    }
}
