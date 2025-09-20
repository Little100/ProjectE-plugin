package org.Little_100.projecte.managers;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.storage.DatabaseManager;
import org.Little_100.projecte.util.Constants;
import org.Little_100.projecte.util.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FuelManager implements Listener {
    private final ProjectE plugin;

    private final NamespacedKey alchemicalCoalKey;
    private final NamespacedKey mobiusFuelKey;
    private final NamespacedKey aeternalisFuelKey;
    private final NamespacedKey darkMatterKey;
    private final NamespacedKey redMatterKey;

    // 燃料
    private ItemStack alchemicalCoal;
    private ItemStack mobiusFuel;
    private ItemStack aeternalisFuel;

    // 燃料块
    private ItemStack alchemicalCoalBlock;
    private ItemStack mobiusFuelBlock;
    private ItemStack aeternalisFuelBlock;

    // 物质
    private ItemStack darkMatter;
    private ItemStack redMatter;

    // 物质块
    private ItemStack darkMatterBlock;
    private ItemStack redMatterBlock;

    private final Map<String, Integer> burnTimes = new HashMap<>();

    public FuelManager(ProjectE plugin) {
        this.plugin = plugin;

        alchemicalCoalKey = new NamespacedKey(plugin, "alchemical_coal");
        mobiusFuelKey = new NamespacedKey(plugin, "mobius_fuel");
        aeternalisFuelKey = new NamespacedKey(plugin, "aeternalis_fuel");
        darkMatterKey = new NamespacedKey(plugin, "dark_matter");
        redMatterKey = new NamespacedKey(plugin, "red_matter");

        burnTimes.put("alchemical_coal", 320 * 20);
        burnTimes.put("mobius_fuel", 1280 * 20);
        burnTimes.put("aeternalis_fuel", 5120 * 20);
        burnTimes.put("alchemical_coal_block", 2880 * 20);
        burnTimes.put("mobius_fuel_block", 11520 * 20);
        burnTimes.put("aeternalis_fuel_block", 46080 * 20);

        plugin.getLogger()
                .info("FuelManager: Detected version " + Bukkit.getServer().getBukkitVersion()
                        + (VersionUtils.is1214OrNewer()
                                ? " (using modern material handling)"
                                : " (using legacy material handling)"));

        createFuelItems();
        registerCustomBlocks();
    }

    private void createFuelItems() {
        createModernFuelItems();
    }

    private void createModernFuelItems() {
        // 煤炭
        alchemicalCoal = createFuelItem(
                Material.COAL,
                "item.alchemical_coal.name",
                1,
                Arrays.asList("item.alchemical_coal.lore1", "item.alchemical_coal.lore2"),
                alchemicalCoalKey,
                (byte) 1,
                "alchemical_coal");

        mobiusFuel = createFuelItem(
                Material.COAL,
                "item.mobius_fuel.name",
                2,
                Arrays.asList("item.mobius_fuel.lore1", "item.mobius_fuel.lore2"),
                mobiusFuelKey,
                (byte) 1,
                "mobius_fuel");

        aeternalisFuel = createFuelItem(
                Material.COAL,
                "item.aeternalis_fuel.name",
                3,
                Arrays.asList("item.aeternalis_fuel.lore1", "item.aeternalis_fuel.lore2"),
                aeternalisFuelKey,
                (byte) 1,
                "aeternalis_fuel");

        alchemicalCoalBlock = createFuelItem(
                Material.COAL_BLOCK,
                "item.alchemical_coal_block.name",
                1,
                Arrays.asList("item.alchemical_coal_block.lore1", "item.alchemical_coal_block.lore2"),
                alchemicalCoalKey,
                (byte) 2,
                "alchemical_coal_block");

        mobiusFuelBlock = createFuelItem(
                Material.COAL_BLOCK,
                "item.mobius_fuel_block.name",
                2,
                Arrays.asList("item.mobius_fuel_block.lore1", "item.mobius_fuel_block.lore2"),
                mobiusFuelKey,
                (byte) 2,
                "mobius_fuel_block");

        aeternalisFuelBlock = createFuelItem(
                Material.COAL_BLOCK,
                "item.aeternalis_fuel_block.name",
                3,
                Arrays.asList("item.aeternalis_fuel_block.lore1", "item.aeternalis_fuel_block.lore2"),
                aeternalisFuelKey,
                (byte) 2,
                "aeternalis_fuel_block");

        // 物质
        darkMatter = createFuelItem(
                Material.SLIME_BALL,
                "item.dark_matter.name",
                1,
                Arrays.asList("item.dark_matter.lore1", "item.dark_matter.lore2"),
                darkMatterKey,
                (byte) 1,
                "dark_matter");

        redMatter = createFuelItem(
                Material.SLIME_BALL,
                "item.red_matter.name",
                2,
                Arrays.asList("item.red_matter.lore1", "item.red_matter.lore2"),
                redMatterKey,
                (byte) 1,
                "red_matter");

        // 物质块
        darkMatterBlock = createFuelItem(
                Material.BLACK_CONCRETE,
                "item.dark_matter_block.name",
                1,
                Arrays.asList("item.dark_matter_block.lore1", "item.dark_matter_block.lore2"),
                darkMatterKey,
                (byte) 2,
                "dark_matter_block");

        redMatterBlock = createFuelItem(
                Material.REDSTONE_BLOCK,
                "item.red_matter_block.name",
                1,
                Arrays.asList("item.red_matter_block.lore1", "item.red_matter_block.lore2"),
                redMatterKey,
                (byte) 2,
                "red_matter_block");
    }

    private ItemStack createFuelItem(
            Material material,
            String displayNameKey,
            int customModelData,
            List<String> loreKeys,
            NamespacedKey key,
            byte value,
            String id) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getLanguageManager().get(displayNameKey));
            List<String> translatedLore = loreKeys.stream()
                    .map(loreKey -> plugin.getLanguageManager().get(loreKey))
                    .collect(Collectors.toList());
            meta.setLore(translatedLore);

            item.setItemMeta(meta);
            // 统一用CustomModelDataUtil设置cmd
            if (plugin.getConfig().getBoolean("debug")) {
                System.out.println("Before setting CustomModelData: " + item);
            }
            item = org.Little_100.projecte.util.CustomModelDataUtil.setCustomModelDataBoth(item, id, customModelData);
            if (plugin.getConfig().getBoolean("debug")) {
                System.out.println("After setting CustomModelData: " + item);
            }
            addFuelTags(item, key, value, id);
        }
        return item;
    }

    private void addFuelTags(ItemStack item, NamespacedKey key, byte value, String id) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(key, PersistentDataType.BYTE, value);
            container.set(Constants.ID_KEY, PersistentDataType.STRING, id);
            item.setItemMeta(meta);
        }
    }

    // 获取燃料物品
    public ItemStack getAlchemicalCoal() {
        return alchemicalCoal.clone();
    }

    public ItemStack getMobiusFuel() {
        return mobiusFuel.clone();
    }

    public ItemStack getAeternalisFuel() {
        return aeternalisFuel.clone();
    }

    public ItemStack getAlchemicalCoalBlock() {
        return alchemicalCoalBlock.clone();
    }

    public ItemStack getMobiusFuelBlock() {
        return mobiusFuelBlock.clone();
    }

    public ItemStack getAeternalisFuelBlock() {
        return aeternalisFuelBlock.clone();
    }

    public ItemStack getDarkMatter() {
        return darkMatter.clone();
    }

    public ItemStack getRedMatter() {
        return redMatter.clone();
    }

    public ItemStack getDarkMatterBlock() {
        return darkMatterBlock.clone();
    }

    public ItemStack getRedMatterBlock() {
        return redMatterBlock.clone();
    }

    // 检查是否为特定的燃料物品
    public boolean isAlchemicalCoal(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(alchemicalCoalKey, PersistentDataType.BYTE)
                && container.get(alchemicalCoalKey, PersistentDataType.BYTE) == (byte) 1;
    }

    public boolean isMobiusFuel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(mobiusFuelKey, PersistentDataType.BYTE)
                && container.get(mobiusFuelKey, PersistentDataType.BYTE) == (byte) 1;
    }

    public boolean isAeternalisFuel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(aeternalisFuelKey, PersistentDataType.BYTE)
                && container.get(aeternalisFuelKey, PersistentDataType.BYTE) == (byte) 1;
    }

    public boolean isAlchemicalCoalBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(alchemicalCoalKey, PersistentDataType.BYTE)
                && container.get(alchemicalCoalKey, PersistentDataType.BYTE) == (byte) 2;
    }

    public boolean isMobiusFuelBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(mobiusFuelKey, PersistentDataType.BYTE)
                && container.get(mobiusFuelKey, PersistentDataType.BYTE) == (byte) 2;
    }

    public boolean isAeternalisFuelBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(aeternalisFuelKey, PersistentDataType.BYTE)
                && container.get(aeternalisFuelKey, PersistentDataType.BYTE) == (byte) 2;
    }

    public boolean isDarkMatter(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(darkMatterKey, PersistentDataType.BYTE)
                && container.get(darkMatterKey, PersistentDataType.BYTE) == (byte) 1;
    }

    public boolean isRedMatter(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(redMatterKey, PersistentDataType.BYTE)
                && container.get(redMatterKey, PersistentDataType.BYTE) == (byte) 1;
    }

    public boolean isDarkMatterBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(darkMatterKey, PersistentDataType.BYTE)
                && container.get(darkMatterKey, PersistentDataType.BYTE) == (byte) 2;
    }

    public boolean isRedMatterBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(redMatterKey, PersistentDataType.BYTE)
                && container.get(redMatterKey, PersistentDataType.BYTE) == (byte) 2;
    }

    // 熔炉燃烧事件处理器
    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        ItemStack fuel = event.getFuel();

        int burnTime = 0;

        if (isAlchemicalCoal(fuel)) {
            burnTime = burnTimes.get("alchemical_coal");
        } else if (isMobiusFuel(fuel)) {
            burnTime = burnTimes.get("mobius_fuel");
        } else if (isAeternalisFuel(fuel)) {
            burnTime = burnTimes.get("aeternalis_fuel");
        } else if (isAlchemicalCoalBlock(fuel)) {
            burnTime = burnTimes.get("alchemical_coal_block");
        } else if (isMobiusFuelBlock(fuel)) {
            burnTime = burnTimes.get("mobius_fuel_block");
        } else if (isAeternalisFuelBlock(fuel)) {
            burnTime = burnTimes.get("aeternalis_fuel_block");
        }

        if (burnTime > 0) {
            event.setBurnTime(burnTime);
        }
    }

    // 获取燃料类型的CustomModelData
    private String getModelDataForFuelType(String fuelType) {
        switch (fuelType) {
            case "alchemical_coal":
                return "1";
            case "mobius_fuel":
                return "2";
            case "aeternalis_fuel":
                return "3";
            case "alchemical_coal_block":
                return "1";
            case "mobius_fuel_block":
                return "2";
            case "aeternalis_fuel_block":
                return "3";
            default:
                return "1";
        }
    }

    // 检查是否为燃料物品
    private boolean isFuelItem(ItemStack item, String fuelType) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        return container.has(Constants.ID_KEY, PersistentDataType.STRING)
                && fuelType.equals(container.get(Constants.ID_KEY, PersistentDataType.STRING));
    }

    private boolean isFuelItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(Constants.ID_KEY, PersistentDataType.STRING);
    }

    // 获取特殊燃料的NBT标签信息以供材质包使用
    public String getNbtTagInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Special Fuel NBT Tag Info:\n\n");

        info.append("1. Alchemical Coal (CustomModelData: 1)\n");
        info.append("   - NamespacedKey: ").append(alchemicalCoalKey.toString()).append("\n");
        info.append("   - DataType: BYTE\n");
        info.append("   - DataValue: 1\n\n");

        info.append("2. Mobius Fuel (CustomModelData: 2)\n");
        info.append("   - NamespacedKey: ").append(mobiusFuelKey.toString()).append("\n");
        info.append("   - DataType: BYTE\n");
        info.append("   - DataValue: 1\n\n");

        info.append("3. Aeternalis Fuel (CustomModelData: 3)\n");
        info.append("   - NamespacedKey: ").append(aeternalisFuelKey.toString()).append("\n");
        info.append("   - DataType: BYTE\n");
        info.append("   - DataValue: 1\n\n");

        info.append("4. Alchemical Coal Block (CustomModelData: 1)\n");
        info.append("   - NamespacedKey: ").append(alchemicalCoalKey.toString()).append("\n");
        info.append("   - DataType: BYTE\n");
        info.append("   - DataValue: 2\n\n");

        info.append("5. Mobius Fuel Block (CustomModelData: 2)\n");
        info.append("   - NamespacedKey: ").append(mobiusFuelKey.toString()).append("\n");
        info.append("   - DataType: BYTE\n");
        info.append("   - DataValue: 2\n\n");

        info.append("6. Aeternalis Fuel Block (CustomModelData: 3)\n");
        info.append("   - NamespacedKey: ").append(aeternalisFuelKey.toString()).append("\n");
        info.append("   - DataType: BYTE\n");
        info.append("   - DataValue: 2\n\n");

        info.append("7. Dark Matter (Material: SLIME_BALL, CustomModelData: 1)\n");
        info.append("   - NamespacedKey: ").append(darkMatterKey.toString()).append("\n");
        info.append("   - DataType: BYTE\n");
        info.append("   - DataValue: 1\n\n");

        info.append("8. Red Matter (Material: SLIME_BALL, CustomModelData: 2)\n");
        info.append("   - NamespacedKey: ").append(redMatterKey.toString()).append("\n");
        info.append("   - DataType: BYTE\n");
        info.append("   - DataValue: 1\n\n");

        info.append("9. Dark Matter Block (Material: SLIME_BALL, CustomModelData: 3)\n");
        info.append("   - NamespacedKey: ").append(darkMatterKey.toString()).append("\n");
        info.append("   - DataType: BYTE\n");
        info.append("   - DataValue: 2\n\n");

        info.append("10. Red Matter Block (Material: SLIME_BALL, CustomModelData: 4)\n");
        info.append("   - NamespacedKey: ").append(redMatterKey.toString()).append("\n");
        info.append("   - DataType: BYTE\n");
        info.append("   - DataValue: 2\n\n");

        return info.toString();
    }

    // 给特定的物品设置EMC值
    public void setFuelEmcValues() {
        DatabaseManager db = plugin.getDatabaseManager();

        // 尝试加载自定义EMC配置文件
        File configFile = new File(plugin.getDataFolder(), "custommoditememc.yml");
        YamlConfiguration config = null;

        if (configFile.exists()) {
            config = YamlConfiguration.loadConfiguration(configFile);
            plugin.getLogger().info("Loaded custom EMC values from custommoditememc.yml");
        } else {
            plugin.getLogger().info("custommoditememc.yml not found, using default EMC values");
        }

        // 设置EMC值，优先使用配置文件中的值，否则使用默认值
        // 炼金煤炭 = 4个煤炭 + 1个贤者之石 = 4 * 128 = 512 EMC
        long alchemicalCoalEmc = (config != null) ? config.getLong("alchemical_coal", 512) : 512;
        db.setEmc(plugin.getEmcManager().getItemKey(getAlchemicalCoal()), alchemicalCoalEmc);

        // 莫比乌斯燃料 = 4个炼金煤炭 + 1个贤者之石 = 4 * 512 = 2048 EMC
        long mobiusFuelEmc = (config != null) ? config.getLong("mobius_fuel", 2048) : 2048;
        db.setEmc(plugin.getEmcManager().getItemKey(getMobiusFuel()), mobiusFuelEmc);

        // 永恒燃料 = 4个莫比乌斯燃料 + 1个贤者之石 = 4 * 2048 = 8192 EMC
        long aeternalisFuelEmc = (config != null) ? config.getLong("aeternalis_fuel", 8192) : 8192;
        db.setEmc(plugin.getEmcManager().getItemKey(getAeternalisFuel()), aeternalisFuelEmc);

        // 炼金煤炭块 = 9个炼金煤炭 = 9 * 512 = 4608 EMC
        long alchemicalCoalBlockEmc = (config != null) ? config.getLong("alchemical_coal_block", 4608) : 4608;
        db.setEmc(plugin.getEmcManager().getItemKey(getAlchemicalCoalBlock()), alchemicalCoalBlockEmc);

        // 莫比乌斯燃料块 = 9个莫比乌斯燃料 = 9 * 2048 = 18432 EMC
        long mobiusFuelBlockEmc = (config != null) ? config.getLong("mobius_fuel_block", 18432) : 18432;
        db.setEmc(plugin.getEmcManager().getItemKey(getMobiusFuelBlock()), mobiusFuelBlockEmc);

        // 永恒燃料块 = 9个永恒燃料 = 9 * 8192 = 73728 EMC
        long aeternalisFuelBlockEmc = (config != null) ? config.getLong("aeternalis_fuel_block", 73728) : 73728;
        db.setEmc(plugin.getEmcManager().getItemKey(getAeternalisFuelBlock()), aeternalisFuelBlockEmc);

        // 暗物质 = 8个永恒燃料 + 1个钻石块 = 8 * 8192 + 73728 = 139264 EMC
        long darkMatterEmc = (config != null) ? config.getLong("dark_matter", 139264) : 139264;
        db.setEmc(plugin.getEmcManager().getItemKey(getDarkMatter()), darkMatterEmc);

        // 红物质 = 6个永恒燃料 + 3个暗物质 = 6 * 8192 + 3 * 139264 = 466944 EMC
        long redMatterEmc = (config != null) ? config.getLong("red_matter", 466944) : 466944;
        db.setEmc(plugin.getEmcManager().getItemKey(getRedMatter()), redMatterEmc);

        // 暗物质块 = 4个暗物质 = 4 * 139264 = 557056 EMC
        long darkMatterBlockEmc = (config != null) ? config.getLong("dark_matter_block", 557056) : 557056;
        db.setEmc(plugin.getEmcManager().getItemKey(getDarkMatterBlock()), darkMatterBlockEmc);

        // 红物质块 = 4个红物质 = 4 * 466944 = 1867776 EMC
        long redMatterBlockEmc = (config != null) ? config.getLong("red_matter_block", 1867776) : 1867776;
        db.setEmc(plugin.getEmcManager().getItemKey(getRedMatterBlock()), redMatterBlockEmc);
    }

    private void registerCustomBlocks() {
        BlockDataManager bdm = plugin.getBlockDataManager();
        bdm.saveBlock("alchemical_coal_block", getAlchemicalCoalBlock(), null);
        bdm.saveBlock("mobius_fuel_block", getMobiusFuelBlock(), null);
        bdm.saveBlock("aeternalis_fuel_block", getAeternalisFuelBlock(), null);
        bdm.saveBlock("dark_matter_block", getDarkMatterBlock(), null);
        bdm.saveBlock("red_matter_block", getRedMatterBlock(), null);
        plugin.getLogger().info("Registered custom fuel blocks with BlockDataManager.");
    }
}
