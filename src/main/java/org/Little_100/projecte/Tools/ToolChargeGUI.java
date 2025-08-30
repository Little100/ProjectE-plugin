package org.Little_100.projecte.Tools;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ToolChargeGUI {

    private final ProjectE plugin;
    private final Player player;
    private final ItemStack tool;
    private Inventory inventory;

    private static final int INVENTORY_SIZE = 27;

    private static final int[] CHARGE_SLOTS_DM = {11, 12, 13};
    private static final int[] CHARGE_SLOTS_RM = {10, 11, 12, 13};
    private static final int[] MODE_SLOTS = {20, 22, 24};

    public ToolChargeGUI(ProjectE plugin, Player player, ItemStack tool) {
        this.plugin = plugin;
        this.player = player;
        this.tool = tool;
    }

    public void open() {
        String title = plugin.getLanguageManager().get("clientside.tool_charge_gui.title");
        inventory = Bukkit.createInventory(null, INVENTORY_SIZE, title);

        setupBackground();
        setupChargeSystem();
        setupModeSelection();

        player.openInventory(inventory);
    }

    private void setupBackground() {
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glassPane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glassPane.setItemMeta(meta);
        }
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventory.setItem(i, glassPane);
        }
    }

    private void setupChargeSystem() {
        ItemMeta toolMeta = tool.getItemMeta();
        if (toolMeta == null) return;

        PersistentDataContainer container = toolMeta.getPersistentDataContainer();
        int currentCharge = container.getOrDefault(new NamespacedKey(plugin, "projecte_charge"), PersistentDataType.INTEGER, 0);
        boolean isRedMatter = plugin.getToolManager().isRedMatterTool(tool);

        int[] chargeSlots = isRedMatter ? CHARGE_SLOTS_RM : CHARGE_SLOTS_DM;
        int maxCharge = isRedMatter ? 3 : 2;

        for (int i = 0; i <= maxCharge; i++) {
            Material material = (i <= currentCharge) ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;
            String name = plugin.getLanguageManager().get("clientside.dark_matter_tool.charge").replace("{level}", String.valueOf(i));
            ItemStack chargeItem = createGuiItem(material, name, null, "charge_level", i);
            if (i == currentCharge) {
                addGlow(chargeItem);
            }
            inventory.setItem(chargeSlots[i], chargeItem);
        }
    }

    private void setupModeSelection() {
        ItemMeta toolMeta = tool.getItemMeta();
        if (toolMeta == null) return;

       ToolManager toolManager = plugin.getToolManager();

       if (!toolManager.isDarkMatterSword(tool) && !toolManager.isRedMatterSword(tool) &&
           !toolManager.isDarkMatterShears(tool) && !toolManager.isRedMatterShears(tool)) {
           PersistentDataContainer container = toolMeta.getPersistentDataContainer();
           String currentMode = container.getOrDefault(new NamespacedKey(plugin, "projecte_mode"), PersistentDataType.STRING, "tall");

           ItemStack tall = createGuiItem(Material.IRON_BARS, plugin.getLanguageManager().get("clientside.tool_charge_gui.tall.name"), null, "tool_mode", "tall");
           if (currentMode.equals("tall")) addGlow(tall);
           inventory.setItem(MODE_SLOTS[0], tall);

           ItemStack wide = createGuiItem(Material.CHAIN, plugin.getLanguageManager().get("clientside.tool_charge_gui.wide.name"), null, "tool_mode", "wide");
           if (currentMode.equals("wide")) addGlow(wide);
           inventory.setItem(MODE_SLOTS[1], wide);

           ItemStack threeByThree = createGuiItem(Material.IRON_BLOCK, plugin.getLanguageManager().get("clientside.tool_charge_gui.3x3.name"), null, "tool_mode", "3x3");
           if (currentMode.equals("3x3")) addGlow(threeByThree);
           inventory.setItem(MODE_SLOTS[2], threeByThree);
       }

        if (toolManager.isRedMatterSword(tool)) {
            int currentSwordMode = toolManager.getSwordMode(tool);
            ItemStack modeItem;
            if (currentSwordMode == 0) {
                modeItem = createGuiItem(Material.ZOMBIE_HEAD, plugin.getLanguageManager().get("clientside.red_matter_sword.mode_hostile"), null, "sword_mode_toggle", 1);
            } else {
                modeItem = createGuiItem(Material.PLAYER_HEAD, plugin.getLanguageManager().get("clientside.red_matter_sword.mode_all"), null, "sword_mode_toggle", 1);
            }
            addGlow(modeItem);
            inventory.setItem(4, modeItem);
        }
    }

    private ItemStack createGuiItem(Material material, String name, List<String> lore, String key, Object value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
            }
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (value instanceof Integer) {
                container.set(new NamespacedKey(plugin, key), PersistentDataType.INTEGER, (Integer) value);
            } else if (value instanceof String) {
                container.set(new NamespacedKey(plugin, key), PersistentDataType.STRING, (String) value);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private void addGlow(ItemStack item) {
        item.addUnsafeEnchantment(Enchantment.LURE, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
    }
}