package org.Little_100.projecte.TransmutationTable;

import org.Little_100.projecte.DebugManager;
import org.Little_100.projecte.EmcManager;
import org.Little_100.projecte.LanguageManager;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.Tools.KleinStar.KleinStarManager;
import org.Little_100.projecte.storage.DatabaseManager;
import org.Little_100.projecte.util.ShulkerBoxUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GUIListener implements Listener {

    private final Map<Player, Boolean> searchingPlayers = new HashMap<>();

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
        } else if (gui.getCurrentState() == TransmutationGUI.GuiState.LEARN || gui.getCurrentState() == TransmutationGUI.GuiState.CHARGE) {
            if (event.getClickedInventory() == player.getInventory() || isTransactionArea(event.getSlot())) {
                event.setCancelled(false);
                return;
            }
        }

        if (gui.getCurrentState() == TransmutationGUI.GuiState.BUY || gui.getCurrentState() == TransmutationGUI.GuiState.MAIN) {
            if (event.getClickedInventory() != gui.getInventory()) {
                event.setCancelled(true);
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
            case CHARGE:
                handleChargeScreenClick(event, gui);
                break;
        }
    }

    private void handleMainScreenClick(InventoryClickEvent event, TransmutationGUI gui) {
        int slot = event.getSlot();
        if (slot == 20) {
            gui.setState(TransmutationGUI.GuiState.SELL);
        } else if (slot == 22) {
            gui.setState(TransmutationGUI.GuiState.BUY);
        } else if (slot == 24) {
            gui.setState(TransmutationGUI.GuiState.LEARN);
        } else if (slot == 21) {
            gui.setState(TransmutationGUI.GuiState.CHARGE);
        }
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
                        totalEmcChange += calculateItemSellEmc(item);
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

    private long calculateItemSellEmc(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return 0;
        }

        EmcManager emcManager = ProjectE.getInstance().getEmcManager();
        KleinStarManager kleinStarManager = ProjectE.getInstance().getKleinStarManager();
        long itemEmc = emcManager.getEmc(item);

        // 处理潜影盒
        if (item.getItemMeta() instanceof BlockStateMeta && ((BlockStateMeta) item.getItemMeta()).getBlockState() instanceof ShulkerBox) {
            if (ShulkerBoxUtil.getFirstItemWithoutEmc(item) == null) {
                return (itemEmc + ShulkerBoxUtil.getTotalEmcOfContents(item)) * item.getAmount();
            }
            return 0; // 如果潜影盒内有物品没有EMC，则整个潜影盒不能出售
        }

        // 处理卡莱恩之星
        if (kleinStarManager.isKleinStar(item)) {
            long baseEmc = emcManager.getEmc(emcManager.getItemKey(item));
            long storedEmc = kleinStarManager.getStoredEmc(item);
            return (baseEmc + storedEmc) * item.getAmount();
        }

        if (item.getItemMeta() instanceof Damageable) {
            Damageable damageable = (Damageable) item.getItemMeta();
            int maxDurability = item.getType().getMaxDurability();
            if (maxDurability > 0) {
                int currentDamage = damageable.getDamage();
                double durabilityPercentage = (double) (maxDurability - currentDamage) / maxDurability;
                long durabilityAdjustedEmc = (long) Math.max(1, itemEmc * durabilityPercentage);
                return durabilityAdjustedEmc * item.getAmount();
            }
        }

        return itemEmc * item.getAmount();
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
        } else if (slot == 4) { // 搜索按钮
            player.closeInventory();
            DebugManager.log("Player " + player.getName() + " clicked search button.");
            
            searchingPlayers.put(player, true);
            
            LanguageManager languageManager = ProjectE.getInstance().getLanguageManager();
            // 发送搜索提示
            player.sendMessage("§a" + languageManager.get("clientside.transmutation_table.search_sign.line1"));
            player.sendMessage("§e例如: 钻石, 铁锭, 煤炭, 红石, 青金石, 木材, 石头等");
            player.sendMessage("§e支持搜索材料类型: §b铁, 金, 钻石, 木, 石, 铜, 羊毛, 玻璃, 混凝土等");
            player.sendMessage("§e支持搜索物品类型: §b剑, 斧, 镐, 锄, 锹, 盔甲, 床, 箱子等");
            player.sendMessage("§e您还可以搜索: §b方块, 矿石, 工具, 食物, 装饰品, 种子, 树苗等");
            player.sendMessage("§a请输入您想要查找的物品关键词:");
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

                    ItemStack purchasedItem = ProjectE.getInstance().getItemStackFromKey(itemKey);
                    if (purchasedItem != null) {
                        purchasedItem.setAmount(amountToBuy);
                    } else {
                        purchasedItem = new ItemStack(clickedItem.getType(), amountToBuy);
                        if (clickedItem.hasItemMeta()) {
                            purchasedItem.setItemMeta(clickedItem.getItemMeta());
                        }
                    }


                    player.getInventory().addItem(purchasedItem);

                    String displayName;
                    ItemMeta meta = purchasedItem.getItemMeta();
                    if (meta != null && meta.hasDisplayName()) {
                        displayName = meta.getDisplayName();
                    } else {
                        displayName = purchasedItem.getType().name();
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
                    // 潜影盒检查
                    if (item.getItemMeta() instanceof BlockStateMeta && ((BlockStateMeta) item.getItemMeta()).getBlockState() instanceof ShulkerBox) {
                        ItemStack itemWithoutEmc = ShulkerBoxUtil.getFirstItemWithoutEmc(item);
                        if (itemWithoutEmc != null) {
                            LanguageManager lang = ProjectE.getInstance().getLanguageManager();
                            Map<String, String> placeholders = new HashMap<>();
                            String shulkerColor = item.getType().name().replace("_SHULKER_BOX", "").toLowerCase();
                            placeholders.put("color", shulkerColor);
                            placeholders.put("item_id", item.getType().name());
                            placeholders.put("item", itemWithoutEmc.hasItemMeta() && itemWithoutEmc.getItemMeta().hasDisplayName() ? itemWithoutEmc.getItemMeta().getDisplayName() : itemWithoutEmc.getType().name());
                            player.sendMessage(lang.get("serverside.command.generic.shulker_box_no_emc", placeholders));
                            
                            player.getInventory().addItem(item);
                            inventory.setItem(i, null);
                            transactionValid = false;
                            continue; // 继续检查下一个物品
                        }
                    }

                    long itemEmc = calculateItemSellEmc(item);
                    if (itemEmc > 0) {
                        totalEmcChange += itemEmc;
                        String itemKey = ProjectE.getInstance().getEmcManager().getItemKey(item);
                        ProjectE.getInstance().getDatabaseManager().addLearnedItem(player.getUniqueId(), itemKey);
                        // 如果是潜影盒，学习内部所有物品
                        if (item.getItemMeta() instanceof BlockStateMeta && ((BlockStateMeta) item.getItemMeta()).getBlockState() instanceof ShulkerBox) {
                            BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
                            ShulkerBox shulkerBox = (ShulkerBox) bsm.getBlockState();
                            for(ItemStack contentItem : shulkerBox.getInventory().getContents()) {
                                if(contentItem != null && !contentItem.getType().isAir()) {
                                    ProjectE.getInstance().getDatabaseManager().addLearnedItem(player.getUniqueId(), ProjectE.getInstance().getEmcManager().getItemKey(contentItem));
                                }
                            }
                        }
                    } else {
                        handleNoEmcItem(player, item);
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
            updateSellButton(gui); // 更新总价显示
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
                        handleNoEmcItem(player, item);
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

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (searchingPlayers.containsKey(player) && searchingPlayers.get(player)) {
            event.setCancelled(true);
            String searchQuery = event.getMessage();
            
            DebugManager.log("Search query from chat: '" + searchQuery + "'");
            searchingPlayers.remove(player);
            
            ProjectE.getInstance().getSchedulerAdapter().runTask(() -> {
                DebugManager.log("Re-opening TransmutationGUI for " + player.getName() + " with search query: '" + searchQuery + "'");
                TransmutationGUI newGui = new TransmutationGUI(player);
                newGui.setState(TransmutationGUI.GuiState.BUY);
                if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                    newGui.setSearchQuery(searchQuery);
                }
                newGui.open();
            });
        }
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
                || gui.getCurrentState() == TransmutationGUI.GuiState.LEARN
                || gui.getCurrentState() == TransmutationGUI.GuiState.CHARGE) {
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
    private void handleChargeScreenClick(InventoryClickEvent event, TransmutationGUI gui) {
        int slot = event.getSlot();
        if (event.getClickedInventory() != gui.getInventory())
            return;

        if (slot == 0) {
            gui.setState(TransmutationGUI.GuiState.MAIN);
        } else if (slot == 49) {
            // The entire charge logic is now here
            Player player = (Player) event.getWhoClicked();
            Inventory inventory = gui.getInventory();
            DatabaseManager databaseManager = ProjectE.getInstance().getDatabaseManager();
            EmcManager emcManager = ProjectE.getInstance().getEmcManager();
            LanguageManager languageManager = ProjectE.getInstance().getLanguageManager();
            KleinStarManager kleinStarManager = ProjectE.getInstance().getKleinStarManager();

            ItemStack kleinStarItem = null;
            int kleinStarSlot = -1;
            int itemCount = 0;

            for (int i = 0; i < 54; i++) {
                if (isTransactionArea(i)) {
                    ItemStack item = inventory.getItem(i);
                    if (item != null && !item.getType().isAir()) {
                        itemCount++;
                        if (kleinStarManager.isKleinStar(item)) {
                            if (kleinStarItem != null) {
                                player.sendMessage(languageManager.get("serverside.command.generic.multiple_klein_stars"));
                                return;
                            }
                            kleinStarItem = item;
                            kleinStarSlot = i;
                        } else {
                            player.sendMessage(languageManager.get("serverside.command.generic.invalid_charge_item"));
                            return;
                        }
                    }
                }
            }

            if (kleinStarItem == null) {
                player.sendMessage(languageManager.get("serverside.command.generic.no_klein_star"));
                return;
            }

            if (itemCount > 1) {
                player.sendMessage(languageManager.get("serverside.command.generic.multiple_items_in_charge"));
                return;
            }

            long playerEmc = databaseManager.getPlayerEmc(player.getUniqueId());
            long storedEmc = kleinStarManager.getStoredEmc(kleinStarItem);
            long capacity = kleinStarManager.getCapacity(kleinStarItem);
            long space = capacity - storedEmc;



            if (playerEmc <= 0) {
                player.sendMessage(languageManager.get("serverside.command.generic.not_enough_emc_to_charge"));
                return;
            }

            if (space <= 0) {
                player.sendMessage(languageManager.get("serverside.command.generic.klein_star_full"));
                return;
            }

            long amountToCharge;
            if (event.isShiftClick()) {
                amountToCharge = space;
            } else {
                amountToCharge = Math.min(playerEmc, space);
            }

            if (amountToCharge <= 0) {
                player.sendMessage(languageManager.get("serverside.command.generic.not_enough_emc_to_charge"));
                return;
            }

            if (playerEmc < amountToCharge) {
                player.sendMessage(languageManager.get("serverside.command.generic.not_enough_emc_to_charge"));
                return;
            }

            databaseManager.setPlayerEmc(player.getUniqueId(), playerEmc - amountToCharge);
            ItemStack updatedKleinStar = kleinStarManager.setStoredEmc(kleinStarItem, storedEmc + amountToCharge);

            inventory.setItem(kleinStarSlot, null);
            player.getInventory().addItem(updatedKleinStar);

            refreshGui(player, TransmutationGUI.GuiState.CHARGE, gui.getPage());

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("emc", String.format("%,d", amountToCharge));
            player.sendMessage(languageManager.get("serverside.command.generic.charge_success", placeholders));

        } else if (isTransactionArea(slot)) {
            event.setCancelled(false);
        }
    }

    private void handleNoEmcItem(Player player, ItemStack item) {
        LanguageManager lang = ProjectE.getInstance().getLanguageManager();
        Map<String, String> placeholders = new HashMap<>();
        
        String displayName;
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            displayName = item.getItemMeta().getDisplayName();
        } else {
            displayName = item.getType().name();
        }
        
        placeholders.put("item", displayName);
        player.sendMessage(lang.get("serverside.command.generic.no_emc_value_trade", placeholders));
    }
}