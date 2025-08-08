package org.Little_100.projecte.TransmutationTable;

import org.Little_100.projecte.EmcManager;
import org.Little_100.projecte.LanguageManager;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.compatibility.VersionAdapter;
import org.Little_100.projecte.storage.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GUIListener implements Listener {

    public GUIListener() {
        // huh?
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof NoEmcItemGUI) {
            handleNoEmcItemGUIClick(event);
            return;
        }
        if (!(holder instanceof TransmutationGUI)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        TransmutationGUI gui = (TransmutationGUI) holder;

        if (gui.getCurrentState() == TransmutationGUI.GuiState.SELL) {
            if (event.getClickedInventory() == player.getInventory() || isTransactionArea(event.getSlot())) {
                // 允许玩家将物品移入出售GUI并更新总价
                event.setCancelled(false);
                updateSellButton(gui);
                return;
            }
        } else if (gui.getCurrentState() == TransmutationGUI.GuiState.LEARN) {
            if (event.getClickedInventory() == player.getInventory() || isTransactionArea(event.getSlot())) {
                // 允许玩家将物品移入学习GUI，但不要更新按钮的lore
                event.setCancelled(false);
                return;
            }
        }

        if (event.getClickedInventory() != gui.getInventory()) {
            return;
        }
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }

        switch (gui.getCurrentState()) {
            case MAIN:
                handleMainScreenClick(event, gui);
                break;
            case SELL:
                handleSellScreenClick(event, gui);
                break;
            case BUY:
                handleBuyScreenClick(event, gui);
                break;
            case LEARN:
                handleLearnScreenClick(event, gui);
                break;
        }
    }

    private void handleMainScreenClick(InventoryClickEvent event, TransmutationGUI gui) {
        int slot = event.getSlot();
        if (slot == 21) {
            gui.setState(TransmutationGUI.GuiState.SELL);
        } else if (slot == 23) {
            gui.setState(TransmutationGUI.GuiState.BUY);
        } else if (slot == 22) {
            gui.setState(TransmutationGUI.GuiState.LEARN);
        } // 卖学买
    }

    private void handleSellScreenClick(InventoryClickEvent event, TransmutationGUI gui) {
        int slot = event.getSlot();

        if (slot == 0) {
            gui.setState(TransmutationGUI.GuiState.MAIN);
        } else if (slot == 49) {
            handleTransaction(event.getWhoClicked(), gui);
        } // 返回 确认
    }

    private void updateSellButton(TransmutationGUI gui) {
        ProjectE.getInstance().getSchedulerAdapter().runTaskLater(() -> {
            Inventory inventory = gui.getInventory();
            long totalEmcChange = 0;
            for (int i = 0; i < 54; i++) {
                if (isTransactionArea(i)) {
                    ItemStack item = inventory.getItem(i);
                    if (item != null && !item.getType().isAir()) {
                        long itemEmc = ProjectE.getInstance().getEmcManager()
                                .getEmc(ProjectE.getInstance().getEmcManager().getItemKey(item));
                        if (itemEmc > 0) {
                            totalEmcChange += itemEmc * item.getAmount();
                        }
                    }
                }
            }

            ItemStack confirmButton = inventory.getItem(49);
            if (confirmButton != null) {
                ItemMeta meta = confirmButton.getItemMeta();
                if (meta != null) {
                    String formattedEmc = String.format("%,d", totalEmcChange);
                    LanguageManager lang = ProjectE.getInstance().getLanguageManager();
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("emc", formattedEmc);
                    meta.setLore(Arrays.asList(
                            lang.get("clientside.transmutation_table.buttons.confirm_transaction_lore"),
                            lang.get("clientside.transmutation_table.buttons.you_will_get", placeholders)));
                    confirmButton.setItemMeta(meta);
                }
            }
        }, 1L);
    }

    private void handleBuyScreenClick(InventoryClickEvent event, TransmutationGUI gui) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (event.getClickedInventory() != gui.getInventory())
            return;

        // 处理导航和边框点击
        if (slot == 0) { // 返回按钮
            gui.setState(TransmutationGUI.GuiState.MAIN);
            return;
        } else if (slot == 48) {
            if (gui.getPage() > 0) {
                gui.setPage(gui.getPage() - 1);
            }
            return;
        } else if (slot == 50) {
            gui.setPage(gui.getPage() + 1);
            return;
        }

        if (slot < 9 || slot > 44 || slot % 9 == 0 || slot % 9 == 8) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && !clickedItem.getType().isAir()) {
            String itemKey = ProjectE.getInstance().getEmcManager().getItemKey(clickedItem);
            long itemEmc = ProjectE.getInstance().getEmcManager().getEmc(itemKey);
            if (itemEmc <= 0)
                return;

            long playerEmc = ProjectE.getInstance().getDatabaseManager().getPlayerEmc(player.getUniqueId());
            int amountToBuy;
            long totalCost;

            // 检查购买的是否是贤者之石
            if (ProjectE.getInstance().isPhilosopherStone(clickedItem)) {
                // 检查玩家是否已经拥有贤者之石
                if (player.getInventory().containsAtLeast(ProjectE.getInstance().getPhilosopherStone(), 1)) {
                    player.sendMessage(ProjectE.getInstance().getLanguageManager()
                            .get("serverside.command.generic.already_have_philosopher_stone"));
                    return;
                }
                // 对于贤者之石，强制购买数量为1，无论左右键
                amountToBuy = 1;
                totalCost = itemEmc;
            } else {
                // 对于其他物品，根据点击类型决定购买数量
                if (event.isLeftClick()) {
                    amountToBuy = 1;
                    totalCost = itemEmc;
                } else if (event.isRightClick()) {
                    amountToBuy = clickedItem.getMaxStackSize();
                    totalCost = itemEmc * amountToBuy;
                } else {
                    return;
                }
            }

            if (amountToBuy > 0) {
                if (playerEmc >= totalCost) {
                    ProjectE.getInstance().getDatabaseManager().setPlayerEmc(player.getUniqueId(),
                            playerEmc - totalCost);

                    ItemStack purchasedItem;
                    if (ProjectE.getInstance().isPhilosopherStone(clickedItem)) {
                        purchasedItem = ProjectE.getInstance().getPhilosopherStone();
                        purchasedItem.setAmount(1);
                    } else {
                        purchasedItem = clickedItem.clone();
                        purchasedItem.setAmount(amountToBuy);
                        ItemMeta meta = purchasedItem.getItemMeta();
                        if (meta != null) {
                            meta.setLore(null);
                            meta.setDisplayName(null);
                            purchasedItem.setItemMeta(meta);
                        }
                    }
                    player.getInventory().addItem(purchasedItem);

                    String displayName;
                    ItemMeta meta = purchasedItem.getItemMeta();
                    if (meta != null && meta.hasDisplayName()) {
                        displayName = meta.getDisplayName();
                    } else {
                        ItemStack tempItem = new ItemStack(purchasedItem.getType());
                        ItemMeta tempMeta = tempItem.getItemMeta();
                        if (tempMeta != null && tempMeta.hasDisplayName()) {
                            displayName = tempMeta.getDisplayName();
                        } else {
                            displayName = tempItem.getType().name();
                        }
                    }
                    LanguageManager lang = ProjectE.getInstance().getLanguageManager();
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("amount", String.valueOf(amountToBuy));
                    placeholders.put("item", displayName);
                    player.sendMessage(lang.get("serverside.command.generic.buy_success", placeholders));

                    refreshGui(player, TransmutationGUI.GuiState.BUY, gui.getPage());

                } else {
                    player.sendMessage(ProjectE.getInstance().getLanguageManager()
                            .get("serverside.command.generic.not_enough_emc"));
                }
            }
        }
    }

    private void handleTransaction(org.bukkit.entity.HumanEntity humanEntity, TransmutationGUI gui) {
        Player player = (Player) humanEntity;
        Inventory inventory = gui.getInventory();
        long totalEmcChange = 0;
        boolean transactionValid = true;

        for (int i = 0; i < 54; i++) {
            if (isTransactionArea(i)) {
                ItemStack item = inventory.getItem(i);
                if (item != null && !item.getType().isAir()) {
                    String itemKey = ProjectE.getInstance().getEmcManager().getItemKey(item);
                    long itemEmc = ProjectE.getInstance().getEmcManager().getEmc(itemKey);
                    if (itemEmc > 0) {
                        totalEmcChange += itemEmc * item.getAmount();
                        ProjectE.getInstance().getDatabaseManager().addLearnedItem(player.getUniqueId(), itemKey);
                    } else {
                        LanguageManager lang = ProjectE.getInstance().getLanguageManager();
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("item", item.getType().name());
                        player.sendMessage(lang.get("serverside.command.generic.no_emc_value_trade", placeholders));
                        player.getInventory().addItem(item);
                        inventory.setItem(i, null);
                        transactionValid = false;
                    }
                }
            }
        }

        if (!transactionValid) {
            player.sendMessage(
                    ProjectE.getInstance().getLanguageManager().get("serverside.command.generic.partial_trade_fail"));
            return;
        }

        if (totalEmcChange > 0) {
            long currentEmc = ProjectE.getInstance().getDatabaseManager().getPlayerEmc(player.getUniqueId());
            long newEmc = currentEmc + totalEmcChange;
            ProjectE.getInstance().getDatabaseManager().setPlayerEmc(player.getUniqueId(), newEmc);
            LanguageManager lang = ProjectE.getInstance().getLanguageManager();
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("emc", String.format("%,d", totalEmcChange));
            player.sendMessage(lang.get("serverside.command.generic.sell_success", placeholders));
        }

        for (int i = 0; i < 54; i++) {
            if (isTransactionArea(i)) {
                inventory.setItem(i, null);
            }
        }

        refreshGui(player, TransmutationGUI.GuiState.SELL, 0);
    }

    private void handleLearnScreenClick(InventoryClickEvent event, TransmutationGUI gui) {
        int slot = event.getSlot();
        if (event.getClickedInventory() != gui.getInventory())
            return;

        if (slot == 0) {
            gui.setState(TransmutationGUI.GuiState.MAIN);
        } else if (slot == 49) {
            handleLearn(event.getWhoClicked(), gui);
        } else if (isTransactionArea(slot)) {
            event.setCancelled(false);
        }
    }

    private void handleLearn(org.bukkit.entity.HumanEntity humanEntity, TransmutationGUI gui) {
        Player player = (Player) humanEntity;
        Inventory inventory = gui.getInventory();
        boolean learnedSomething = false;

        for (int i = 0; i < 54; i++) {
            if (isTransactionArea(i)) {
                ItemStack item = inventory.getItem(i);
                if (item != null && !item.getType().isAir()) {
                    String itemKey = ProjectE.getInstance().getEmcManager().getItemKey(item);
                    long itemEmc = ProjectE.getInstance().getEmcManager().getEmc(itemKey);
                    if (itemEmc > 0) {
                        ProjectE.getInstance().getDatabaseManager().addLearnedItem(player.getUniqueId(), itemKey);
                        learnedSomething = true;
                    } else {
                        LanguageManager lang = ProjectE.getInstance().getLanguageManager();
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("item", item.getType().name());
                        player.sendMessage(lang.get("serverside.command.generic.no_emc_value_learn", placeholders));
                    }
                }
            }
        }

        if (learnedSomething) {
            player.sendMessage(
                    ProjectE.getInstance().getLanguageManager().get("serverside.command.generic.learn_success"));
        }

        for (int i = 0; i < 54; i++) {
            if (isTransactionArea(i)) {
                ItemStack item = inventory.getItem(i);
                if (item != null && !item.getType().isAir()) {
                    player.getInventory().addItem(item);
                    inventory.setItem(i, null);
                }
            }
        }
        player.closeInventory();
    }

    private void refreshGui(Player player, TransmutationGUI.GuiState state, int page) {
        player.closeInventory();
        ProjectE.getInstance().getSchedulerAdapter().runTaskLater(() -> {
            TransmutationGUI newGui = new TransmutationGUI(player);
            newGui.setState(state);
            newGui.setPage(page);
            newGui.open();
        }, 1L);
    }

    private boolean isTransactionArea(int slot) {
        if (slot > 9 && slot < 44 && slot % 9 != 0 && slot % 9 != 8) {
            return true;
        }
        return false;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof TransmutationGUI)) {
            return;
        }

        TransmutationGUI gui = (TransmutationGUI) holder;
        if (gui.getCurrentState() == TransmutationGUI.GuiState.SELL
                || gui.getCurrentState() == TransmutationGUI.GuiState.LEARN) {
            Inventory inventory = event.getInventory();
            Player player = (Player) event.getPlayer();

            for (int i = 0; i < 54; i++) {
                if (isTransactionArea(i)) {
                    ItemStack item = inventory.getItem(i);
                    if (item != null && !item.getType().isAir()) {
                        player.getInventory().addItem(item);
                    }
                }
            }
        }
    }

    private void handleNoEmcItemGUIClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        NoEmcItemGUI gui = (NoEmcItemGUI) event.getInventory().getHolder();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }

        int slot = event.getSlot();
        if (slot == 45 && gui.getPage() > 0) {
            new NoEmcItemGUI(gui.getItems(), gui.getPage() - 1).openInventory(player);
        } else if (slot == 53) {
            int startIndex = (gui.getPage() + 1) * 45;
            if (startIndex < gui.getItems().size()) {
                new NoEmcItemGUI(gui.getItems(), gui.getPage() + 1).openInventory(player);
            }
        }
    }
}