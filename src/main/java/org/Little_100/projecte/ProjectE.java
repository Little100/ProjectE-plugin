package org.Little_100.projecte;

import org.Little_100.projecte.accessories.*;
import org.Little_100.projecte.alchemicalbag.AlchemicalBagManager;
import org.Little_100.projecte.armor.ArmorManager;
import org.Little_100.projecte.command.CustomCommand;
import org.Little_100.projecte.compatibility.GeyserAdapter;
import org.Little_100.projecte.compatibility.scheduler.SchedulerAdapter;
import org.Little_100.projecte.compatibility.version.VersionAdapter;
import org.Little_100.projecte.devices.*;
import org.Little_100.projecte.gui.DiviningRodGUI;
import org.Little_100.projecte.gui.GUIListener;
import org.Little_100.projecte.listeners.*;
import org.Little_100.projecte.managers.*;
import org.Little_100.projecte.storage.DatabaseManager;
import org.Little_100.projecte.tome.TransmutationTabletBook;
import org.Little_100.projecte.tools.DiviningRod;
import org.Little_100.projecte.tools.RepairTalisman;
import org.Little_100.projecte.tools.RepairTalismanTask;
import org.Little_100.projecte.tools.ToolManager;
import org.Little_100.projecte.tools.kleinstar.KleinStarManager;
import org.Little_100.projecte.util.CustomBlockArtUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ProjectE extends JavaPlugin {

    private static ProjectE instance;
    private static SchedulerAdapter schedulerAdapter;

    private ItemStack philosopherStone;
    private RecipeManager recipeManager;
    private DatabaseManager databaseManager;
    private AccessoryRecipeManager accessoryRecipeManager;
    private EmcManager emcManager;
    private VersionAdapter versionAdapter;
    private AlchemicalBagManager alchemicalBagManager;
    private LanguageManager languageManager;
    private ResourcePackListener resourcePackManager;
    private SearchLanguageManager searchLanguageManager;
    private DatapackManager datapackManager;
    private BlockDataManager blockDataManager;
    private PhilosopherStoneListener philosopherStoneListener;
    private FuelManager fuelManager;
    private CovalenceDust covalenceDust;
    private DiviningRod diviningRod;
    private RepairTalisman repairTalisman;
    private DiviningRodGUI diviningRodGUI;
    private KleinStarManager kleinStarManager;
    private ToolManager toolManager;
    private ArmorManager armorManager;
    private ArmorListener armorListener;
    private GeyserAdapter geyserAdapter;
    private org.Little_100.projecte.listeners.PdcItemDebugGUIListener pdcItemDebugGUIListener;
    private FileConfiguration devicesConfig;
    private FileConfiguration opItemConfig;
    private FurnaceManager furnaceManager;
    private DeviceManager deviceManager;
    private CondenserManager condenserManager;
    private AlchemicalChestManager alchemicalChestManager;
    private EnergyCollectorManager energyCollectorManager;


    private final Map<Material, Material> upgradeMap = new HashMap<>();
    private final Map<Material, Material> downgradeMap = new HashMap<>();
    private final Map<String, ItemStack> pdcItemCache = new HashMap<>();

    public FileConfiguration getDevicesConfig() {
        if (devicesConfig == null) {
            File devicesFile = new File(getDataFolder(), "devices.yml");
            if (!devicesFile.exists()) {
                saveResource("devices.yml", false);
            }
            devicesConfig = YamlConfiguration.loadConfiguration(devicesFile);
        }
        return devicesConfig;
    }

    public FileConfiguration getOpItemConfig() {
        if (opItemConfig == null) {
            File opItemFile = new File(getDataFolder(), "op_item.yml");
            if (!opItemFile.exists()) {
                saveResource("op_item.yml", false);
            }
            opItemConfig = YamlConfiguration.loadConfiguration(opItemFile);
        }
        return opItemConfig;
    }

    @Override
    public void onEnable() {
        instance = this;
        
        // 初始化调度器适配器
        SchedulerAdapter.init(this);
        schedulerAdapter = SchedulerAdapter.getInstance();

        // 保存默认配置
        saveDefaultConfig();

        // 加载配置
        loadConfigOptions();
        getOpItemConfig();

        // 初始化语言管理器
        languageManager = new LanguageManager(this);

        // 初始化搜索语言管理器
        searchLanguageManager = new SearchLanguageManager(this);

        // 初始化调试管理器
        Debug.init(this);

        // 自动将jar中的所有yml文件释放到数据文件夹中
        try {
            File langFolder = new File(getDataFolder(), "lang");
            if (!langFolder.exists()) {
                langFolder.mkdirs();
            }

            ZipInputStream zip = new ZipInputStream(getClass()
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .openStream());
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                // 只释放 lang/ 目录下的 yml 文件
                if (name.startsWith("lang/") && name.endsWith(".yml") && !entry.isDirectory()) {
                    File outFile = new File(getDataFolder(), name);
                    if (!outFile.exists()) {
                        saveResource(name, false);
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("file", name);
                        getLogger().info(languageManager.get("plugin.autofix_language_file", placeholders));
                    }
                }
            }
            zip.close();
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Error auto-releasing yml language files", e);
        }

        // 初始化数据库
        databaseManager = new DatabaseManager(getDataFolder());

        // 初始化兼容性适配器
        versionAdapter = VersionAdapter.getInstance();
        if (versionAdapter == null) {
            getLogger().severe("Failed to find a compatible version adapter. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 初始化方块数据管理器
        blockDataManager = new BlockDataManager(this);

        // 初始化EMC管理器
        emcManager = new EmcManager(this);
        try {
            getLogger().info("Calculating EMC values...");
            emcManager.calculateAndStoreEmcValues(false);
            getLogger().info("EMC calculation complete.");
        } catch (Exception e) {
            getLogger()
                    .severe(
                            "An error occurred during EMC calculation. Some items may not have EMC values. Please check your config.yml for formatting errors.");
            e.printStackTrace();
        }
        if (deviceManager != null) {
            deviceManager.reloadDeviceItems();
        }

        // 加载自定义EMC值
        loadCustomEmcValues();

        // 初始化贤者之石物品
        createPhilosopherStone();

        // 初始化燃料管理器
        fuelManager = new FuelManager(this);
        getServer().getPluginManager().registerEvents(fuelManager, this);
        getLogger().info("FuelManager initialized with special fuels.");

        // 初始化地图画工具
        new CustomBlockArtUtil(this, getSchedulerAdapter());

        // 设置特殊燃料的EMC值
        fuelManager.setFuelEmcValues();

        // 输出特殊燃料的NBT标签信息，供用户添加材质

        // 初始化共价粉
        covalenceDust = new CovalenceDust(this);
        covalenceDust.setCovalenceDustEmcValues();

        // 初始化探知之杖
        diviningRod = new DiviningRod(this);
        diviningRod.setDiviningRodEmcValues();
        diviningRodGUI = new DiviningRodGUI(this);

        // 初始化修复护符
        repairTalisman = new RepairTalisman(this);
        repairTalisman.setEmcValue();
        new RepairTalismanTask(this);

        // 初始化克莱因之星
        kleinStarManager = new KleinStarManager(this);

        // 初始化物质工具
        toolManager = new ToolManager(this);

        // 初始化物质盔甲
        armorManager = new ArmorManager(this);

        // 初始化炼金袋管理器
        if (getConfig().getBoolean("AlchemicalBag.enabled", true)) {
            alchemicalBagManager = new AlchemicalBagManager(this);
            alchemicalBagManager.register();
            getLogger().info("Alchemical Bag feature is enabled.");
        } else {
            getLogger().info("Alchemical Bag feature is disabled in the config.");
        }

        // 初始化熔炉管理器
        furnaceManager = new FurnaceManager(this);

        // 初始化设备管理器
        deviceManager = new DeviceManager(this);
        deviceManager.registerDevices();

        // 初始化能量凝聚器管理器
        condenserManager = new CondenserManager(this);

        // 初始化炼金术箱子管理器
        alchemicalChestManager = new AlchemicalChestManager(this);

        // 初始化能量收集器管理器
        energyCollectorManager = new EnergyCollectorManager(this);

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new ToolListener(this), this);

        // 初始化饰品配方管理器
        accessoryRecipeManager = new AccessoryRecipeManager(this);
        accessoryRecipeManager.registerRecipes();

        // 删除原版爆裂紫颂果配方
        removeVanillaRecipe();

        // 初始化材料转换映射
        initMaterialMaps();

        // 注册事件监听器
        philosopherStoneListener = new PhilosopherStoneListener(this);
        Bukkit.getPluginManager().registerEvents(philosopherStoneListener, this);
        Bukkit.getPluginManager().registerEvents(new PhilosopherStoneGUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ItemStackLimitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
        Bukkit.getPluginManager().registerEvents(new ToolChargeGUIListener(this), this);
        pdcItemDebugGUIListener = new org.Little_100.projecte.listeners.PdcItemDebugGUIListener(this);
        Bukkit.getPluginManager().registerEvents(pdcItemDebugGUIListener, this);
        Bukkit.getPluginManager().registerEvents(new ToolAbilityListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new CovalenceDustListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CovalenceDustCraftListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DiviningRodListener(this), this);
        Bukkit.getPluginManager().registerEvents(new KleinStarListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ServerLoadListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CraftingListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TransmutationTabletBookListener(), this);
        Bukkit.getPluginManager().registerEvents(new org.Little_100.projecte.accessories.AccessoryListener(), this);
        armorListener = new ArmorListener(this);
        Bukkit.getPluginManager().registerEvents(armorListener, this);
        Bukkit.getPluginManager().registerEvents(new GemHelmetGUIListener(this), this);

        getServer().getPluginManager().registerEvents(new FurnaceListener(this, furnaceManager), this);
        getServer().getPluginManager().registerEvents(new CondenserListener(this), this);

        // 初始化共价粉
        covalenceDust = new CovalenceDust(this);

        // 注册命令
        CommandManager commandManager = new CommandManager(this);
        getCommand("projecte").setExecutor(commandManager);
        getCommand("projecte").setTabCompleter(commandManager);
        getServer().getPluginManager().registerEvents(new GUIEditorListener(this, commandManager), this);

        registerCustomCommands(commandManager);

        // 在所有物品管理器都初始化之后再注册配方
        recipeManager = new RecipeManager(this);
        recipeManager.registerAllRecipes();

        // 初始化资源包管理器
        resourcePackManager = new ResourcePackListener(this);
        getLogger().info("Resource Pack Manager initialized.");

        // 初始化数据包管理器
        datapackManager = new DatapackManager(this);
        datapackManager.setupDatapack();

        getLogger().info("ProjectE plugin has been enabled!");

        // 启动连续粒子效果任务
        startContinuousParticleTask();

        // 初始化 Geyser 适配器
        geyserAdapter = new GeyserAdapter(this);
        if (geyserAdapter.isGeyserApiAvailable()) {
            getServer().getPluginManager().registerEvents(new GeyserPlayerJoinListener(this), this);
        }

        getServer().getPluginManager().registerEvents(new DeviceListener(this), this);
    }

    @Override
    public void onDisable() {
        // 注销所有自定义配方
        if (recipeManager != null) {
            recipeManager.unregisterAllRecipes();
        }
        if (accessoryRecipeManager != null) {
            accessoryRecipeManager.unregisterAllRecipes();
        }

        if (alchemicalBagManager != null) {
            alchemicalBagManager.unregister();
        }

        // 关闭能量收集器管理器
        if (energyCollectorManager != null) {
            energyCollectorManager.shutdown();
        }

        // 关闭数据库连接
        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("ProjectE plugin has been disabled!");
    }

    public static ProjectE getInstance() {
        return instance;
    }

    public ItemStack getPhilosopherStone() {
        ItemStack stone = new ItemStack(Material.POPPED_CHORUS_FRUIT);
        setMaxStackSize(stone, 1);
        return stone;
    }

    public boolean isPhilosopherStone(ItemStack item) {
        if (item == null) {
            return false;
        }
        return item.getType() == Material.POPPED_CHORUS_FRUIT;
    }

    private void createPhilosopherStone() {
        philosopherStone = new ItemStack(Material.POPPED_CHORUS_FRUIT);
        setMaxStackSize(philosopherStone, 1);
    }
    
    private void setMaxStackSize(ItemStack item, int maxStackSize) {
        if (item == null || !item.hasItemMeta()) {
            if (item != null) {
                item.setItemMeta(item.getItemMeta());
            }
        }
        
        try {
            java.lang.reflect.Method setMaxStackSizeMethod = 
                item.getItemMeta().getClass().getMethod("setMaxStackSize", Integer.class);
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            setMaxStackSizeMethod.invoke(meta, maxStackSize);
            item.setItemMeta(meta);
        } catch (Exception e) {}
    }

    private void removeVanillaRecipe() {
        try {
            // 删除原版爆裂紫颂果配方（替换为贤者之石的配方）
            Bukkit.removeRecipe(NamespacedKey.minecraft("popped_chorus_fruit"));
        } catch (Exception e) {
            getLogger().warning("Could not remove vanilla popped chorus fruit recipe: " + e.getMessage());
        }
    }

    private void initMaterialMaps() {
        // 基础材料升级路径：煤炭 -> 铜锭 -> 铁锭 -> 金锭 -> 钻石 -> 下界合金锭
        upgradeMap.put(Material.COAL, Material.COPPER_INGOT);
        upgradeMap.put(Material.COPPER_INGOT, Material.IRON_INGOT);
        upgradeMap.put(Material.IRON_INGOT, Material.GOLD_INGOT);
        upgradeMap.put(Material.GOLD_INGOT, Material.DIAMOND);
        upgradeMap.put(Material.DIAMOND, Material.NETHERITE_INGOT);

        // 方块升级路径：煤炭块 -> 铜块 -> 铁块 -> 金块 -> 钻石块 -> 下界合金块
        upgradeMap.put(Material.COAL_BLOCK, Material.COPPER_BLOCK);
        upgradeMap.put(Material.COPPER_BLOCK, Material.IRON_BLOCK);
        upgradeMap.put(Material.IRON_BLOCK, Material.GOLD_BLOCK);
        upgradeMap.put(Material.GOLD_BLOCK, Material.DIAMOND_BLOCK);
        upgradeMap.put(Material.DIAMOND_BLOCK, Material.NETHERITE_BLOCK);

        // 矿石升级路径：煤矿石 -> 铜矿石 -> 铁矿石 -> 金矿石 -> 钻石矿石 -> 远古残骸
        upgradeMap.put(Material.COAL_ORE, Material.COPPER_ORE);
        upgradeMap.put(Material.COPPER_ORE, Material.IRON_ORE);
        upgradeMap.put(Material.IRON_ORE, Material.GOLD_ORE);
        upgradeMap.put(Material.GOLD_ORE, Material.DIAMOND_ORE);
        upgradeMap.put(Material.DIAMOND_ORE, Material.ANCIENT_DEBRIS);

        // 深层板岩矿石升级路径：深层煤矿石 -> 深层铜矿石 -> 深层铁矿石 -> 深层金矿石 -> 深层钻石矿石 -> 远古残骸
        upgradeMap.put(Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE_COPPER_ORE);
        upgradeMap.put(Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE_IRON_ORE);
        upgradeMap.put(Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_GOLD_ORE);
        upgradeMap.put(Material.DEEPSLATE_GOLD_ORE, Material.DEEPSLATE_DIAMOND_ORE);
        upgradeMap.put(Material.DEEPSLATE_DIAMOND_ORE, Material.ANCIENT_DEBRIS);

        // 基础材料降级路径
        downgradeMap.put(Material.COPPER_INGOT, Material.COAL);
        downgradeMap.put(Material.IRON_INGOT, Material.COPPER_INGOT);
        downgradeMap.put(Material.GOLD_INGOT, Material.IRON_INGOT);
        downgradeMap.put(Material.DIAMOND, Material.GOLD_INGOT);
        downgradeMap.put(Material.NETHERITE_INGOT, Material.DIAMOND);

        // 方块降级路径
        downgradeMap.put(Material.COPPER_BLOCK, Material.COAL_BLOCK);
        downgradeMap.put(Material.IRON_BLOCK, Material.COPPER_BLOCK);
        downgradeMap.put(Material.GOLD_BLOCK, Material.IRON_BLOCK);
        downgradeMap.put(Material.DIAMOND_BLOCK, Material.GOLD_BLOCK);
        downgradeMap.put(Material.NETHERITE_BLOCK, Material.DIAMOND_BLOCK);

        // 矿石降级路径
        downgradeMap.put(Material.COPPER_ORE, Material.COAL_ORE);
        downgradeMap.put(Material.IRON_ORE, Material.COPPER_ORE);
        downgradeMap.put(Material.GOLD_ORE, Material.IRON_ORE);
        downgradeMap.put(Material.DIAMOND_ORE, Material.GOLD_ORE);
        downgradeMap.put(Material.ANCIENT_DEBRIS, Material.DIAMOND_ORE);

        // 深层板岩矿石降级路径
        downgradeMap.put(Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE_COAL_ORE);
        downgradeMap.put(Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_COPPER_ORE);
        downgradeMap.put(Material.DEEPSLATE_GOLD_ORE, Material.DEEPSLATE_IRON_ORE);
        downgradeMap.put(Material.DEEPSLATE_DIAMOND_ORE, Material.DEEPSLATE_GOLD_ORE);
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public EmcManager getEmcManager() {
        return emcManager;
    }

    public void reloadPlugin() {
        reloadConfig();
        loadConfigOptions();

        // 重新加载语言文件
        languageManager.loadLanguageFiles();
        searchLanguageManager.loadSearchLanguageFile();

        // 注销并重新注册所有配方
        if (recipeManager != null) {
            recipeManager.unregisterAllRecipes();
            recipeManager.registerAllRecipes();
        }
        if (accessoryRecipeManager != null) {
            accessoryRecipeManager.unregisterAllRecipes();
            accessoryRecipeManager.registerRecipes();
        }
        getLogger().info("All recipes have been reloaded.");

        emcManager = new EmcManager(this);
        emcManager.calculateAndStoreEmcValues(true);
        if (deviceManager != null) {
            deviceManager.reloadDeviceItems();
        }

        // 重新加载所有自定义EMC值
        loadCustomEmcValues();

        // 重新设置程序化定义的EMC值，因为它们在重载时被清除了
        if (fuelManager != null) {
            fuelManager.setFuelEmcValues();
        }
        if (covalenceDust != null) {
            covalenceDust.setCovalenceDustEmcValues();
        }
        if (diviningRod != null) {
            diviningRod.setDiviningRodEmcValues();
        }
        if (repairTalisman != null) {
            repairTalisman.setEmcValue();
        }

        getLogger().info("ProjectE plugin has been reloaded!");
    }

    public VersionAdapter getVersionAdapter() {
        return versionAdapter;
    }

    public SchedulerAdapter getSchedulerAdapter() {
        return SchedulerAdapter.getInstance();
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public ResourcePackListener getResourcePackManager() {
        return resourcePackManager;
    }

    public SearchLanguageManager getSearchLanguageManager() {
        return searchLanguageManager;
    }

    public GeyserAdapter getGeyserAdapter() {
        return geyserAdapter;
    }

    public Material getUpgradedMaterial(Material material) {
        return upgradeMap.getOrDefault(material, null);
    }

    public Material getDowngradedMaterial(Material material) {
        return downgradeMap.getOrDefault(material, null);
    }

    public FuelManager getFuelManager() {
        return fuelManager;
    }

    public CovalenceDust getCovalenceDust() {
        return covalenceDust;
    }

    public DiviningRod getDiviningRod() {
        return diviningRod;
    }

    public RepairTalisman getRepairTalisman() {
        return repairTalisman;
    }

    public KleinStarManager getKleinStarManager() {
        return kleinStarManager;
    }

    public DiviningRodGUI getDiviningRodGUI() {
        return diviningRodGUI;
    }

    public ToolManager getToolManager() {
        return toolManager;
    }

    public ArmorManager getArmorManager() {
        return armorManager;
    }

    public ArmorListener getArmorListener() {
        return armorListener;
    }

    public org.Little_100.projecte.listeners.PdcItemDebugGUIListener getPdcItemDebugGUIListener() {
        return pdcItemDebugGUIListener;
    }

    public BlockDataManager getBlockDataManager() {
        return blockDataManager;
    }

    public FurnaceManager getFurnaceManager() {
        return furnaceManager;
    }

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public CondenserManager getCondenserManager() {
        return condenserManager;
    }

    public AlchemicalChestManager getAlchemicalChestManager() {
        return alchemicalChestManager;
    }

    public EnergyCollectorManager getEnergyCollectorManager() {
        return energyCollectorManager;
    }

    private void loadConfigOptions() {
        // 不再控制pdc物品是否开关
        }

    private void startContinuousParticleTask() {
        if (!getConfig().getBoolean("philosopher_stone.particle.enabled", true)) {
            return;
        }

        long keepAlive = getConfig().getLong("philosopher_stone.particle.keep-alive", 5);
        long period = (keepAlive == -1) ? 10L : keepAlive * 20L; // 如果为-1，则每半秒刷新一次，否则按配置的秒数刷新

        getSchedulerAdapter().runTimer(
                () -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        ItemStack mainHand = player.getInventory().getItemInMainHand();
                        ItemStack offHand = player.getInventory().getItemInOffHand();

                        if (isPhilosopherStone(mainHand) || isPhilosopherStone(offHand)) {
                            Block targetBlock = null;
                            try {
                                // 添加世界数据检查以防止在Folia环境下的NullPointerException
                                if (player.getWorld() != null
                                        && player.getWorld()
                                                .isChunkLoaded(
                                                        player.getLocation().getBlockX() >> 4,
                                                        player.getLocation().getBlockZ() >> 4)) {
                                    targetBlock = player.getTargetBlock(null, 10);
                                }
                            } catch (Exception e) {
                            }
                            if (targetBlock != null && !targetBlock.getType().isAir()) {
                                philosopherStoneListener.showContinuousOutline(player, targetBlock);
                            }
                        }
                    }
                },
                1L,
                period);
    }

    public ItemStack getItemStackFromKey(String key) {
        // 保存原始key用于PDC物品查找
        String originalKey = key;
        
        // 首先，规范化键，移除任何已知的命名空间前缀
        if (key.startsWith("projecte:")) {
            key = key.substring("projecte:".length());
        } else if (key.startsWith("minecraft:")) {
            key = key.substring("minecraft:".length());
        }

        switch (key) {
            case "philosopher_stone":
            case "transmutation_table":
                return getPhilosopherStone();
            // Fuels
            case "alchemical_coal":
                return fuelManager.getAlchemicalCoal();
            case "mobius_fuel":
                return fuelManager.getMobiusFuel();
            case "aeternalis_fuel":
                return fuelManager.getAeternalisFuel();
            case "alchemical_coal_block":
                return fuelManager.getAlchemicalCoalBlock();
            case "mobius_fuel_block":
                return fuelManager.getMobiusFuelBlock();
            case "aeternalis_fuel_block":
                return fuelManager.getAeternalisFuelBlock();

            // Materials
            case "dark_matter":
                return fuelManager.getDarkMatter();
            case "red_matter":
                return fuelManager.getRedMatter();
            case "dark_matter_block":
                return fuelManager.getDarkMatterBlock();
            case "red_matter_block":
                return fuelManager.getRedMatterBlock();

            // Covalence Dust
            case "low_covalence_dust":
                return covalenceDust.getLowCovalenceDust();
            case "medium_covalence_dust":
                return covalenceDust.getMediumCovalenceDust();
            case "high_covalence_dust":
                return covalenceDust.getHighCovalenceDust();

            // Divining Rods
            case "low_divining_rod":
                return diviningRod.getLowDiviningRod();
            case "medium_divining_rod":
                return diviningRod.getMediumDiviningRod();
            case "high_divining_rod":
                return diviningRod.getHighDiviningRod();

            // Talismans
            case "repair_talisman":
                return repairTalisman.getRepairTalisman();

            // Dark Matter Tools
            case "dark_matter_pickaxe":
                return toolManager.getDarkMatterPickaxe();
            case "dark_matter_axe":
                return toolManager.getDarkMatterAxe();
            case "dark_matter_shovel":
                return toolManager.getDarkMatterShovel();
            case "dark_matter_hoe":
                return toolManager.getDarkMatterHoe();
            case "dark_matter_sword":
                return toolManager.getDarkMatterSword();
            case "dark_matter_shears":
                return toolManager.getDarkMatterShears();
            case "dark_matter_hammer":
                return toolManager.getDarkMatterHammer();

            // Red Matter Tools
            case "red_matter_pickaxe":
                return toolManager.getRedMatterPickaxe();
            case "red_matter_axe":
                return toolManager.getRedMatterAxe();
            case "red_matter_shovel":
                return toolManager.getRedMatterShovel();
            case "red_matter_hoe":
                return toolManager.getRedMatterHoe();
            case "red_matter_sword":
                return toolManager.getRedMatterSword();
            case "red_matter_shears":
                return toolManager.getRedMatterShears();
            case "red_matter_hammer":
                return toolManager.getRedMatterHammer();
            case "red_matter_katar":
                return toolManager.getRedMatterKatar();
            case "red_matter_morningstar":
                return toolManager.getRedMatterMorningstar();

            // Dark Matter Armor
            case "dark_matter_helmet":
                return armorManager.getDarkMatterHelmet();
            case "dark_matter_chestplate":
                return armorManager.getDarkMatterChestplate();
            case "dark_matter_leggings":
                return armorManager.getDarkMatterLeggings();
            case "dark_matter_boots":
                return armorManager.getDarkMatterBoots();

            // Red Matter Armor
            case "red_matter_helmet":
                return armorManager.getRedMatterHelmet();
            case "red_matter_chestplate":
                return armorManager.getRedMatterChestplate();
            case "red_matter_leggings":
                return armorManager.getRedMatterLeggings();
            case "red_matter_boots":
                return armorManager.getRedMatterBoots();

            // Gem Armor
            case "gem_helmet":
                return armorManager.getGemHelmet();
            case "gem_chestplate":
                return armorManager.getGemChestplate();
            case "gem_leggings":
                return armorManager.getGemLeggings();
            case "gem_boots":
                return armorManager.getGemBoots();

            // Klein Stars (Corrected Keys)
            case "klein_star_ein":
                return kleinStarManager.getKleinStar(1);
            case "klein_star_zwei":
                return kleinStarManager.getKleinStar(2);
            case "klein_star_drei":
                return kleinStarManager.getKleinStar(3);
            case "klein_star_vier":
                return kleinStarManager.getKleinStar(4);
            case "klein_star_sphere":
                return kleinStarManager.getKleinStar(5);
            case "klein_star_omega":
                return kleinStarManager.getKleinStar(6);

            // Accessories
            case "body_stone":
                return BodyStone.createBodyStone();
            case "soul_stone":
                return SoulStone.createSoulStone();
            case "life_stone":
                return LifeStone.createLifeStone();
            case "mind_stone":
                return MindStone.createMindStone();

            // Devices
            case "dark_matter_furnace":
                return deviceManager.getDarkMatterFurnaceItem();
            case "red_matter_furnace":
                return deviceManager.getRedMatterFurnaceItem();
            case "alchemical_chest":
                return deviceManager.getAlchemicalChestItem();
            case "energy_condenser":
                return deviceManager.getEnergyCondenserItem();
            case "energy_condenser_mk2":
                return deviceManager.getEnergyCondenserMK2Item();
            case "energy_collector_mk1":
                return deviceManager.getEnergyCollectorItem(EnergyCollector.TYPE_MK1);
            case "energy_collector_mk2":
                return deviceManager.getEnergyCollectorItem(EnergyCollector.TYPE_MK2);
            case "energy_collector_mk3":
                return deviceManager.getEnergyCollectorItem(EnergyCollector.TYPE_MK3);
            case "alchemical_bag":
                return AlchemicalBagManager.getAlchemicalBag();
            case "transmutation_tablet_book":
                return TransmutationTabletBook.createTransmutationTabletBook();

            default:
                // 尝试将剩余的键解析为原生Minecraft材料
                Material material = versionAdapter.getMaterial(key.toUpperCase());
                if (material != null) {
                    return new ItemStack(material);
                }
                
                // 尝试从配方中查找PDC物品
                // 如果原始key包含:判断为其他插件的PDC物品
                if (originalKey.contains(":") && !originalKey.startsWith("minecraft:")) {
                    // 先检查缓存
                    if (pdcItemCache.containsKey(originalKey)) {
                        return pdcItemCache.get(originalKey).clone();
                    }
                    
                    // 遍历所有配方查找
                    Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
                    while (recipeIterator.hasNext()) {
                        try {
                            Recipe recipe = recipeIterator.next();
                            if (recipe.getResult() != null && !recipe.getResult().getType().isAir()) {
                                ItemStack result = recipe.getResult();
                                String resultKey = emcManager.getItemKey(result);
                                if (resultKey.equals(originalKey)) {
                                    // 找到匹配的PDC物品缓存并返回副本
                                    pdcItemCache.put(originalKey, result.clone());
                                    return result.clone();
                                }
                            }
                        } catch (Exception e) {
                            // 忽略损坏的配方
                        }
                    }
                }
        }
        return null;
    }

    private void registerCustomCommands(CommandManager commandManager) {
        File commandsFile = new File(getDataFolder(), "command.yml");
        if (!commandsFile.exists()) {
            saveResource("command.yml", false);
        }
        FileConfiguration commandsConfig = YamlConfiguration.loadConfiguration(commandsFile);
        if (commandsConfig.isConfigurationSection("OpenTransmutationTable")) {
            try {
                final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                bukkitCommandMap.setAccessible(true);
                CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

                for (String key : commandsConfig
                        .getConfigurationSection("OpenTransmutationTable")
                        .getKeys(false)) {
                    String commandName = commandsConfig.getString("OpenTransmutationTable." + key + ".command", "");
                    if (commandName.contains(" ")) {
                        getLogger().info("Skipping registration of sub-command: " + commandName);
                        continue;
                    }
                    String description = commandsConfig.getString("OpenTransmutationTable." + key + ".description", "");

                    CustomCommand command = new CustomCommand(commandName, commandManager);
                    command.setDescription(description);
                    commandMap.register(getDescription().getName(), command);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadCustomEmcValues() {
        File customEmcFile = new File(getDataFolder(), "custommoditememc.yml");
        if (!customEmcFile.exists()) {
            saveResource("custommoditememc.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(customEmcFile);
        DatabaseManager db = getDatabaseManager();

        for (String key : config.getKeys(false)) {
            long emcValue = config.getLong(key);
            if (emcValue > 0) {
                String fullKey = "projecte:" + key;
                db.setEmc(fullKey, emcValue);
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("item", fullKey);
                placeholders.put("emc", String.valueOf(emcValue));
                Debug.log("debug.emc.custom_emc_loaded", placeholders);
            }
        }
        getLogger().info("Loaded custom EMC values from custommoditememc.yml");
    }

    private void setKleinStarEmcValues() {}
}
