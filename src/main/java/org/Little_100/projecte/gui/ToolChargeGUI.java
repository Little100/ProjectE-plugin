package org.Little_100.projecte.gui;

import java.util.List;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.tools.ToolManager;
import org.Little_100.projecte.util.Constants;
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

public class ToolChargeGUI {

    private final ProjectE plugin;
    private final Player player;
    private final ItemStack tool;
    private Inventory inventory;

    private static final int INVENTORY_SIZE = 36;

    private static final int[] CHARGE_SLOTS_DM = {11, 12, 13};
    private static final int[] CHARGE_SLOTS_RM = {10, 11, 12, 13, 14};
    private static final int KATAR_MODE_SLOT = 22;

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
        int currentCharge = container.getOrDefault(Constants.CHARGE_KEY, PersistentDataType.INTEGER, 0);
        boolean isRedMatter = plugin.getToolManager().isRedMatterTool(tool);

        int[] chargeSlots = isRedMatter ? CHARGE_SLOTS_RM : CHARGE_SLOTS_DM;
        int maxCharge = isRedMatter ? 4 : 2;

        for (int i = 0; i <= maxCharge; i++) {
            Material material = (i <= currentCharge) ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;
            String name = plugin.getLanguageManager()
                    .get("clientside.dark_matter_tool.charge")
                    .replace("{level}", String.valueOf(i));
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
        PersistentDataContainer container = toolMeta.getPersistentDataContainer();

        if (toolManager.isRedMatterKatar(tool)) {
            int currentMode = container.getOrDefault(Constants.KATAR_MODE_KEY, PersistentDataType.INTEGER, 0);

            Material material;
            String name;
            int nextMode;

            if (currentMode == 0) {
                material = Material.ZOMBIE_HEAD;
                name = plugin.getLanguageManager().get("clientside.red_matter_katar.mode_hostile");
                nextMode = 1;
            } else {
                material = Material.PLAYER_HEAD;
                name = plugin.getLanguageManager().get("clientside.red_matter_katar.mode_all");
                nextMode = 0;
            }

            if (currentMode == 0) {
                material = Material.PLAYER_HEAD;
                name = plugin.getLanguageManager().get("clientside.red_matter_katar.mode_all");
                nextMode = 1;
            } else {
                material = Material.ZOMBIE_HEAD;
                name = plugin.getLanguageManager().get("clientside.red_matter_katar.mode_hostile");
                nextMode = 0;
            }

            ItemStack modeToggleItem = createGuiItem(material, name, null, "katar_mode", nextMode);
            addGlow(modeToggleItem);
            inventory.setItem(KATAR_MODE_SLOT, modeToggleItem);
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
