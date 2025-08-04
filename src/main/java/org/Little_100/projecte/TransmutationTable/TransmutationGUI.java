package org.Little_100.projecte.TransmutationTable;

import org.Little_100.projecte.EmcManager;
import org.Little_100.projecte.LanguageManager;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.compatibility.VersionAdapter;
import org.Little_100.projecte.storage.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TransmutationGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Player player;
    private final DatabaseManager databaseManager;
    private final VersionAdapter versionAdapter;
    private final EmcManager emcManager;
    private final LanguageManager languageManager;
    private long playerEmc;
    private GuiState currentState = GuiState.MAIN;
    private int page = 0;

    public enum GuiState {
        MAIN,
        SELL,
        BUY,
        LEARN
    }

    public TransmutationGUI(Player player) {
        this.player = player;
        this.databaseManager = ProjectE.getInstance().getDatabaseManager();
        this.versionAdapter = ProjectE.getInstance().getVersionAdapter();
        this.emcManager = ProjectE.getInstance().getEmcManager();
        this.languageManager = ProjectE.getInstance().getLanguageManager();
        this.playerEmc = databaseManager.getPlayerEmc(player.getUniqueId());
        this.inventory = Bukkit.createInventory(this, 54, getTitle());
        initializeItems();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open() {
        player.openInventory(inventory);
    }

    private String getTitle() {
        String formattedEmc = String.format("%,d", playerEmc);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("emc", formattedEmc);

        switch (currentState) {
            case SELL:
                return languageManager.get("clientside.transmutation_table.sell_title", placeholders);
            case BUY:
                return languageManager.get("clientside.transmutation_table.buy_title", placeholders);
            default:
                return languageManager.get("clientside.transmutation_table.title", placeholders);
        }
    }

    private void initializeItems() {
        inventory.clear();
        switch (currentState) {
            case MAIN:
                setupMainScreen();
                break;
            case SELL:
                setupSellScreen();
                break;
            case BUY:
                setupBuyScreen();
                break;
            case LEARN:
                setupLearnScreen();
                break;
        }
    }

    private void setupMainScreen() {
        ItemStack sellButton = createGuiItem(Material.ANVIL, languageManager.get("clientside.transmutation_table.buttons.sell"), languageManager.get("clientside.transmutation_table.buttons.sell_lore"));
        ItemStack buyButton = createGuiItem(Material.EMERALD, languageManager.get("clientside.transmutation_table.buttons.buy"), languageManager.get("clientside.transmutation_table.buttons.buy_lore"));
        ItemStack learnButton = createGuiItem(Material.BOOK, languageManager.get("clientside.transmutation_table.buttons.learn"), languageManager.get("clientside.transmutation_table.buttons.learn_lore"));
        inventory.setItem(21, sellButton);
        inventory.setItem(23, buyButton);
        inventory.setItem(22, learnButton);
    }

    private void setupSellScreen() {
        ItemStack grayPane = createGuiItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "");
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, grayPane);
        }
        for (int i = 1; i < 5; i++) {
            for (int j = 1; j < 8; j++) {
                inventory.setItem(i * 9 + j, null);
            }
        }
        ItemStack confirmButton = createGuiItem(Material.EMERALD_BLOCK, languageManager.get("clientside.transmutation_table.buttons.confirm_sell"), languageManager.get("clientside.transmutation_table.buttons.confirm_sell_lore"));
        ItemStack backButton = createGuiItem(Material.BARRIER, languageManager.get("clientside.transmutation_table.buttons.back"), languageManager.get("clientside.transmutation_table.buttons.back_lore"));
        inventory.setItem(49, confirmButton);
        inventory.setItem(0, backButton);
    }

    public void setupBuyScreen() {
        inventory.clear();
        // 创建边框
        ItemStack grayPane = createGuiItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, grayPane);
            }
        }

        java.util.List<String> learnedItems = databaseManager.getLearnedItems(player.getUniqueId());
        int itemsPerPage = 28; // 4*7
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, learnedItems.size());

        int slotIndex = 0;
        for (int i = startIndex; i < endIndex; i++) {
            String itemKey = learnedItems.get(i);
            ItemStack item = ProjectE.getInstance().getItemStackFromKey(itemKey);
            if (item != null) {
                long emc = emcManager.getEmc(itemKey);
                if (emc > 0) {
                    ItemMeta meta = item.getItemMeta();
                    long stackEmc = emc * item.getMaxStackSize();
                    Map<String, String> lorePlaceholders = new HashMap<>();
                    lorePlaceholders.put("emc", String.format("%,d", emc));
                    lorePlaceholders.put("stack_emc", String.format("%,d", stackEmc));
                    lorePlaceholders.put("amount", String.valueOf(item.getMaxStackSize()));

                    meta.setLore(Arrays.asList(
                            languageManager.get("clientside.transmutation_table.item_lore.emc_single", lorePlaceholders),
                            languageManager.get("clientside.transmutation_table.item_lore.emc_stack", lorePlaceholders),
                            languageManager.get("clientside.transmutation_table.item_lore.buy_one", lorePlaceholders),
                            languageManager.get("clientside.transmutation_table.item_lore.buy_stack", lorePlaceholders)
                    ));
                    item.setItemMeta(meta);
                    // 计算槽位
                    int row = slotIndex / 7;
                    int col = slotIndex % 7;
                    inventory.setItem(9 + (row * 9) + (col + 1), item);
                    slotIndex++;
                }
            }
        }

        // 控制按钮
        ItemStack backButton = createGuiItem(Material.BARRIER, languageManager.get("clientside.transmutation_table.buttons.back"), languageManager.get("clientside.transmutation_table.buttons.back_lore"));
        inventory.setItem(0, backButton);

        if (page > 0) {
            ItemStack prevButton = createGuiItem(Material.ARROW, languageManager.get("clientside.transmutation_table.buttons.prev_page"));
            inventory.setItem(48, prevButton);
        }

        if (endIndex < learnedItems.size()) {
            ItemStack nextButton = createGuiItem(Material.ARROW, languageManager.get("clientside.transmutation_table.buttons.next_page"));
            inventory.setItem(50, nextButton);
        }
    }

    private void setupLearnScreen() {
        ItemStack grayPane = createGuiItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "");
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, grayPane);
        }
        for (int i = 1; i < 5; i++) {
            for (int j = 1; j < 8; j++) {
                inventory.setItem(i * 9 + j, null);
            }
        }
        ItemStack confirmButton = createGuiItem(Material.EMERALD_BLOCK, languageManager.get("clientside.transmutation_table.buttons.confirm_learn"), languageManager.get("clientside.transmutation_table.buttons.confirm_learn_lore"));
        ItemStack backButton = createGuiItem(Material.BARRIER, languageManager.get("clientside.transmutation_table.buttons.back"), languageManager.get("clientside.transmutation_table.buttons.back_lore"));
        inventory.setItem(49, confirmButton);
        inventory.setItem(0, backButton);
    }

    public GuiState getCurrentState() {
        return currentState;
    }

    public void setState(GuiState state) {
        this.currentState = state;
        this.page = 0;
        initializeItems();
    }

    public void setPage(int page) {
        this.page = page;
        initializeItems();
    }

    public int getPage() {
        return page;
    }

    protected ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}