package org.Little_100.projecte.gui;

import java.util.*;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.util.Constants;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PhilosopherStoneGUI {

    private final ProjectE plugin;
    private final Player player;
    private Inventory inventory;

    // GUI相关常量
    private static final int INVENTORY_SIZE = 27; // 3行箱子界面

    // 充能系统相关常量
    private static final int CHARGE_LEVEL_MAX = 4;
    private static final int EMERALD_SLOT = 10; // 第2行第2列
    private static final int[] REDSTONE_SLOTS = {11, 12, 13, 14}; // 第2行第3-6列

    // 模式选择相关常量
    private static final int MODE_PANEL_SLOT = 20; // 第3行第3列
    private static final int MODE_LINE_SLOT = 21; // 第3行第4列
    private static final int MODE_CUBE_SLOT = 22; // 第3行第5列

    // 工作台入口
    public static final int CRAFTING_TABLE_SLOT = 16; // 第2行第8列

    // 玻璃板槽位
    private static final int[] GLASS_PANE_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 15, 17, 18, 19, 23, 24, 25, 26};

    private static final NamespacedKey CURRENT_MODE_KEY = new NamespacedKey(ProjectE.getInstance(), "current_mode");

    // 存储玩家当前充能等级和模式
    private static final Map<Player, Integer> playerChargeLevel = new HashMap<>();
    private static final Map<Player, StoneMode> playerCurrentMode = new HashMap<>();

    // 模式类型
    public enum StoneMode {
        PANEL("panel"),
        LINE("line"),
        CUBE("cube");

        private final String configKey;

        StoneMode(String configKey) {
            this.configKey = configKey;
        }

        public String getConfigKey() {
            return configKey;
        }

        /**
         * 获取指定充能等级的范围描述。
         */
        public String getRangeDescription(int chargeLevel, ProjectE plugin) {
            String key = "clientside.philosopher_stone_gui.ranges." + configKey + "_" + chargeLevel;
            return plugin.getLanguageManager().get(key);
        }

        /**
         * 获取模式的显示名称。
         */
        public String getDisplayName(ProjectE plugin) {
            String key = "clientside.philosopher_stone_gui.modes." + configKey;
            return plugin.getLanguageManager().get(key);
        }
    }

    public PhilosopherStoneGUI(ProjectE plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        if (!playerChargeLevel.containsKey(player)) {
            playerChargeLevel.put(player, 0);
        }

        if (!playerCurrentMode.containsKey(player)) {
            playerCurrentMode.put(player, StoneMode.PANEL);
        }
    }

    /**
     * 打开贤者之石GUI。
     */
    public void open() {
        String title = plugin.getLanguageManager().get("clientside.philosopher_stone_gui.title");
        inventory = Bukkit.createInventory(null, INVENTORY_SIZE, title);

        // 设置界面背景（黑色玻璃板）
        setupBackground();

        // 设置充能系统
        setupChargeSystem();

        // 设置模式选择
        setupModeSelection();

        // 设置工作台入口
        setupCraftingEntryPoint();

        // 打开GUI
        player.openInventory(inventory);
    }

    /**
     * 根据指定布局设置GUI背景。
     */
    private void setupBackground() {
        ItemStack glassPane = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", new ArrayList<>());

        // 设置玻璃板槽位
        for (int slot : GLASS_PANE_SLOTS) {
            inventory.setItem(slot, glassPane);
        }

        // 新设计不需要箭头
    }

    /**
     * 设置充能系统。
     */
    private void setupChargeSystem() {
        int chargeLevel = playerChargeLevel.get(player);
        StoneMode currentMode = playerCurrentMode.get(player);

        // 设置绿宝石块（显示当前充能等级）
        String chargeLevelText =
                plugin.getLanguageManager().get("clientside.philosopher_stone_gui.charge_levels.level_" + chargeLevel);
        String rangeText = currentMode.getRangeDescription(chargeLevel, plugin);
        String clickHint = plugin.getLanguageManager().get("clientside.philosopher_stone_gui.lore.charge_click");
        String currentRangeText = plugin.getLanguageManager()
                .get("clientside.philosopher_stone_gui.lore.current_range")
                .replace("{range}", rangeText);

        List<String> emeraldLore = Arrays.asList(ChatColor.GRAY + clickHint, ChatColor.YELLOW + currentRangeText);

        ItemStack emeraldItem = createGuiItem(Material.EMERALD_BLOCK, ChatColor.GOLD + chargeLevelText, emeraldLore);
        ItemMeta emeraldMeta = emeraldItem.getItemMeta();
        if (emeraldMeta != null) {
            emeraldMeta
                    .getPersistentDataContainer()
                    .set(Constants.CHARGE_LEVEL_KEY, PersistentDataType.INTEGER, -1); // -1 表示绿宝石块
            emeraldItem.setItemMeta(emeraldMeta);
        }
        inventory.setItem(EMERALD_SLOT, emeraldItem);

        // 设置红石块（显示充能进度）
        for (int i = 0; i < REDSTONE_SLOTS.length; i++) {
            Material material = (i < chargeLevel) ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;
            Map<String, String> progressPlaceholders = new HashMap<>();
            progressPlaceholders.put("level", String.valueOf(i + 1));
            progressPlaceholders.put("max_level", String.valueOf(CHARGE_LEVEL_MAX));
            String progressText = plugin.getLanguageManager()
                    .get("clientside.philosopher_stone_gui.charge_progress", progressPlaceholders);

            List<String> redstoneLore = Arrays.asList(ChatColor.GRAY + clickHint, ChatColor.YELLOW + progressText);

            ItemStack redstoneItem = createGuiItem(material, ChatColor.GOLD + progressText, redstoneLore);
            ItemMeta redstoneMeta = redstoneItem.getItemMeta();
            if (redstoneMeta != null) {
                redstoneMeta
                        .getPersistentDataContainer()
                        .set(Constants.CHARGE_LEVEL_KEY, PersistentDataType.INTEGER, i + 1);
                redstoneItem.setItemMeta(redstoneMeta);
            }

            inventory.setItem(REDSTONE_SLOTS[i], redstoneItem);
        }
    }

    /**
     * 设置模式选择。
     */
    private void setupModeSelection() {
        StoneMode currentMode = playerCurrentMode.get(player);
        int chargeLevel = playerChargeLevel.get(player);

        // 面板模式
        boolean isPanelMode = StoneMode.PANEL.equals(currentMode);
        ItemStack panelItem = createModeItem(Material.STONE_PRESSURE_PLATE, StoneMode.PANEL, isPanelMode, chargeLevel);
        inventory.setItem(MODE_PANEL_SLOT, panelItem);

        // 直线模式
        boolean isLineMode = StoneMode.LINE.equals(currentMode);
        ItemStack lineItem = createModeItem(Material.ACACIA_FENCE, StoneMode.LINE, isLineMode, chargeLevel);
        inventory.setItem(MODE_LINE_SLOT, lineItem);

        // 立方体模式
        boolean isCubeMode = StoneMode.CUBE.equals(currentMode);
        ItemStack cubeItem = createModeItem(Material.GRASS_BLOCK, StoneMode.CUBE, isCubeMode, chargeLevel);
        inventory.setItem(MODE_CUBE_SLOT, cubeItem);
    }

    /**
     * 创建模式物品。
     */
    private ItemStack createModeItem(Material material, StoneMode mode, boolean selected, int chargeLevel) {
        String modeName = mode.getDisplayName(plugin);
        String rangeText = mode.getRangeDescription(chargeLevel, plugin);

        List<String> lore = new ArrayList<>();

        if (selected) {
            String currentModeText = plugin.getLanguageManager()
                    .get("clientside.philosopher_stone_gui.modes.current_mode")
                    .replace("{mode}", modeName);
            lore.add(ChatColor.GREEN + "✓ " + currentModeText);
            lore.add(ChatColor.YELLOW
                    + plugin.getLanguageManager()
                            .get("clientside.philosopher_stone_gui.lore.range")
                            .replace("{range}", rangeText));
            modeName = ChatColor.GREEN + "✓ " + modeName;
        } else {
            String clickHint = plugin.getLanguageManager().get("clientside.philosopher_stone_gui.lore.mode_click");
            lore.add(ChatColor.GRAY + clickHint);
            lore.add(ChatColor.YELLOW
                    + plugin.getLanguageManager()
                            .get("clientside.philosopher_stone_gui.lore.range")
                            .replace("{range}", rangeText));
        }

        ItemStack item = createGuiItem(material, ChatColor.GOLD + modeName, lore);

        // 添加模式标识
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(CURRENT_MODE_KEY, PersistentDataType.STRING, mode.name());
            item.setItemMeta(meta);
        }

        return item;
    }

    private void setupCraftingEntryPoint() {
        String name = plugin.getLanguageManager().get("clientside.philosopher_stone_gui.crafting.title");
        String loreText = plugin.getLanguageManager().get("clientside.philosopher_stone_gui.crafting.lore");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + loreText);
        ItemStack craftingTable = createGuiItem(Material.CRAFTING_TABLE, ChatColor.GOLD + name, lore);
        inventory.setItem(CRAFTING_TABLE_SLOT, craftingTable);
    }

    /**
     * 创建GUI物品。
     */
    private ItemStack createGuiItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // 设置名称
            meta.setDisplayName(name);

            // 设置lore
            if (lore != null) {
                meta.setLore(lore);
            }

            // 标记为GUI物品
            meta.getPersistentDataContainer().set(Constants.GUI_ITEM_KEY, PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 获取当前充能等级。
     */
    public static int getChargeLevel(Player player) {
        return playerChargeLevel.getOrDefault(player, 0);
    }

    /**
     * 设置充能等级。
     */
    public static void setChargeLevel(Player player, int level) {
        int oldLevel = getChargeLevel(player);
        int actualLevel = Math.min(Math.max(0, level), CHARGE_LEVEL_MAX);

        if (oldLevel != actualLevel) {
            playerChargeLevel.put(player, actualLevel);
            if (actualLevel > oldLevel) {
                player.playSound(player.getLocation(), "projecte:custom.pecharge", 1.0f, 1.0f);
            } else {
                player.playSound(player.getLocation(), "projecte:custom.peuncharge", 1.0f, 1.0f);
            }
        }
    }

    /**
     * 获取当前模式。
     */
    public static StoneMode getCurrentMode(Player player) {
        return playerCurrentMode.getOrDefault(player, StoneMode.PANEL);
    }

    /**
     * 设置当前模式。
     */
    public static void setCurrentMode(Player player, StoneMode mode) {
        playerCurrentMode.put(player, mode);
    }

    /**
     * 获取GUI。
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * 获取变换范围。
     */
    public static TransformationArea getTransformationArea(Player player) {
        StoneMode mode = getCurrentMode(player);
        int chargeLevel = getChargeLevel(player);

        return new TransformationArea(mode, chargeLevel);
    }

    /**
     * 变换范围的数据类。
     */
    public static class TransformationArea {
        private final StoneMode mode;
        private final int chargeLevel;
        private final int width;
        private final int height;
        private final int depth;

        public TransformationArea(StoneMode mode, int chargeLevel) {
            this.mode = mode;
            this.chargeLevel = chargeLevel;

            // 根据模式和充能等级计算范围
            int size = 1 + (chargeLevel * 2); // 等级0=1，1=3，2=5，3=7，4=9

            switch (mode) {
                case PANEL:
                    this.width = size;
                    this.height = size;
                    this.depth = 1;
                    break;
                case LINE:
                    this.width = 1;
                    this.height = size;
                    this.depth = 1;
                    break;
                case CUBE:
                    this.width = size;
                    this.height = size;
                    this.depth = size;
                    break;
                default:
                    this.width = 1;
                    this.height = 1;
                    this.depth = 1;
            }
        }

        public StoneMode getMode() {
            return mode;
        }

        public int getChargeLevel() {
            return chargeLevel;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getDepth() {
            return depth;
        }
    }
}
