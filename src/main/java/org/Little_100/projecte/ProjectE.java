package org.Little_100.projecte;

import org.Little_100.projecte.AlchemicalBag.AlchemicalBagManager;
import org.Little_100.projecte.Tome.TransmutationTabletBookListener;
import org.Little_100.projecte.util.CustomBlockArtUtil;
import org.Little_100.projecte.Tools.DiviningRodGUI;
import org.Little_100.projecte.Tools.Divining_Rod;
import org.Little_100.projecte.Tools.KleinStar.KleinStarManager;
import org.Little_100.projecte.Tools.Repair_Talisman;
import org.Little_100.projecte.compatibility.GeyserAdapter;
import org.Little_100.projecte.compatibility.SchedulerAdapter;
import org.Little_100.projecte.compatibility.SchedulerMatcher;
import org.Little_100.projecte.compatibility.VersionAdapter;
import org.Little_100.projecte.compatibility.VersionMatcher;
import org.bukkit.Bukkit;
import org.Little_100.projecte.TransmutationTable.GUIListener;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.Little_100.projecte.storage.DatabaseManager;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.lang.reflect.Field;
import java.io.File;
 
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class ProjectE extends JavaPlugin {

    private static ProjectE instance;
    private ItemStack philosopherStone;
    private NamespacedKey philosopherStoneKey;
    private RecipeManager recipeManager;
    private DatabaseManager databaseManager;
    private EmcManager emcManager;
    private VersionAdapter versionAdapter;
    private AlchemicalBagManager alchemicalBagManager;
    private LanguageManager languageManager;
    private ResourcePackManager resourcePackManager;
    private DatapackManager datapackManager;
    private SchedulerAdapter schedulerAdapter;
    private BlockDataManager blockDataManager;
    private PhilosopherStoneListener philosopherStoneListener;
    private FuelManager fuelManager;
    private CovalenceDust covalenceDust;
    private Divining_Rod diviningRod;
    private Repair_Talisman repairTalisman;
    private DiviningRodGUI diviningRodGUI;
    private KleinStarManager kleinStarManager;
    private final Map<Material, Material> upgradeMap = new HashMap<>();
    private final Map<Material, Material> downgradeMap = new HashMap<>();
    private boolean excludePDC;
    private GeyserAdapter geyserAdapter;
 
     @Override
     public void onEnable() {
         instance = this;
 
         // 保存默认配置
         saveDefaultConfig();
         
        // 加载配置
        loadConfigOptions();
 
         // 初始化语言管理器
         languageManager = new LanguageManager(this);

        // 初始化调试管理器
        DebugManager.init(this);

        // 自动将jar中的所有yml文件释放到数据文件夹中
        try {
            java.util.zip.ZipInputStream zip = new java.util.zip.ZipInputStream(
                    getClass().getProtectionDomain().getCodeSource().getLocation().openStream());
            java.util.zip.ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                // 跳过plugin.yml，只释放其他yml文件
                // 只释放符合 xx_xx.yml 格式的语言文件
                if (name.matches("[a-z]{2}_[a-z]{2}\\.yml") && !entry.isDirectory()) {
                    java.io.File outFile = new java.io.File(getDataFolder(), name);
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
            getLogger().warning("Error auto-releasing yml language files: " + e.getMessage());
        }

        // 初始化数据库
        databaseManager = new DatabaseManager(getDataFolder());

        // 初始化兼容性适配器
        versionAdapter = VersionMatcher.getAdapter();
        if (versionAdapter == null) {
            getLogger().severe("Failed to find a compatible version adapter. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 初始化调度程序
        schedulerAdapter = SchedulerMatcher.getSchedulerAdapter(this);

        // 初始化方块数据管理器
        blockDataManager = new BlockDataManager(this);

        // 初始化EMC管理器
        emcManager = new EmcManager(this);
        try {
            getLogger().info("Calculating EMC values...");
            emcManager.calculateAndStoreEmcValues(false);
            getLogger().info("EMC calculation complete.");
        } catch (Exception e) {
            getLogger().severe(
                    "An error occurred during EMC calculation. Some items may not have EMC values. Please check your config.yml for formatting errors.");
            e.printStackTrace();
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
        new CustomBlockArtUtil(this, schedulerAdapter);

        // 设置特殊燃料的EMC值
        fuelManager.setFuelEmcValues();

        // 输出特殊燃料的NBT标签信息，供用户添加材质

        // 初始化共价粉
        covalenceDust = new CovalenceDust(this);
        covalenceDust.setCovalenceDustEmcValues();
 
        // 初始化探知之杖
        diviningRod = new Divining_Rod(this);
        diviningRod.setDiviningRodEmcValues();
        diviningRodGUI = new DiviningRodGUI(this);

        // 初始化修复护符
        repairTalisman = new Repair_Talisman(this);
        repairTalisman.setEmcValue();
        new org.Little_100.projecte.Tools.RepairTalismanListener(this);

       // 初始化克莱因之星
       kleinStarManager = new KleinStarManager(this);

        // AlchemicalBagManager is now initialized below, based on config

        // 初始化配方管理器
        recipeManager = new RecipeManager(this);
        recipeManager.registerAllRecipes();

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
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new CovalenceDustListener(this), this);
        // AlchemicalBagManager's listeners are now registered within its own register() method
        Bukkit.getPluginManager().registerEvents(new CovalenceDustCraftListener(), this);
        Bukkit.getPluginManager().registerEvents(new org.Little_100.projecte.Tools.DiviningRodListener(this), this);
        Bukkit.getPluginManager().registerEvents(new org.Little_100.projecte.Tools.KleinStar.KleinStarListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CraftingListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TransmutationTabletBookListener(), this);
 
        // 初始化共价粉
        covalenceDust = new CovalenceDust(this);


        // 注册命令
        CommandManager commandManager = new CommandManager(this);
        getCommand("projecte").setExecutor(commandManager);
        getCommand("projecte").setTabCompleter(commandManager);

        registerCustomCommands(commandManager);

        // The setblock logic is now inside CommandManager
 
        // 初始化炼金袋管理器
        if (getConfig().getBoolean("AlchemicalBag.enabled", true)) {
            alchemicalBagManager = new AlchemicalBagManager(this);
            alchemicalBagManager.register();
            getLogger().info("Alchemical Bag feature is enabled.");
        } else {
            getLogger().info("Alchemical Bag feature is disabled in the config.");
        }

        // 初始化资源包管理器
        resourcePackManager = new ResourcePackManager(this);
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
    }

    @Override
    public void onDisable() {
        // 注销所有自定义配方
        if (recipeManager != null) {
            recipeManager.unregisterAllRecipes();
        }

        if (alchemicalBagManager != null) {
            alchemicalBagManager.unregister();
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
        return philosopherStone.clone();
    }

    public boolean isPhilosopherStone(ItemStack item) {
        if (item == null) {
            return false;
        }
        Material stoneMaterial = versionAdapter.getMaterial("POPPED_CHORUS_FRUIT");
        if (stoneMaterial == null) { // 与旧版本兼容
            stoneMaterial = Material.NETHER_STAR;
        }
        return item.getType() == stoneMaterial;
    }

    public NamespacedKey getPhilosopherStoneKey() {
        return philosopherStoneKey;
    }

    private void createPhilosopherStone() {
        Material stoneMaterial = versionAdapter.getMaterial("POPPED_CHORUS_FRUIT");
        if (stoneMaterial == null) { // 与旧版本兼容
            stoneMaterial = Material.NETHER_STAR;
        }
        philosopherStone = new ItemStack(stoneMaterial, 1);
        ItemMeta meta = philosopherStone.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Philosopher's Stone");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A powerful alchemical tool",
                ChatColor.YELLOW + "Can transmute minerals",
                ChatColor.YELLOW + "Sneak + Right-click to open workbench"));
        meta.setUnbreakable(true);
        philosopherStoneKey = new NamespacedKey(this, "philosopher_stone");
        philosopherStone.setItemMeta(meta);
        philosopherStone.setAmount(1); // 确保堆叠大小为1
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

        // 注销并重新注册所有配方
        if (recipeManager != null) {
            recipeManager.unregisterAllRecipes();
            recipeManager.registerAllRecipes();
            getLogger().info("All recipes have been reloaded from recipe.yml.");
        }

        emcManager = new EmcManager(this);
        emcManager.calculateAndStoreEmcValues(true);

        // 重新加载所有自定义EMC值
        loadCustomEmcValues();

        getLogger().info("ProjectE plugin has been reloaded!");
    }

    public VersionAdapter getVersionAdapter() {
        return versionAdapter;
    }

    public SchedulerAdapter getSchedulerAdapter() {
        return schedulerAdapter;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public ResourcePackManager getResourcePackManager() {
        return resourcePackManager;
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

    public Divining_Rod getDiviningRod() {
        return diviningRod;
    }

    public Repair_Talisman getRepairTalisman() {
        return repairTalisman;
    }

    public KleinStarManager getKleinStarManager() {
        return kleinStarManager;
    }

    public DiviningRodGUI getDiviningRodGUI() {
        return diviningRodGUI;
    }

 
    public boolean isPdcExcluded() {
        return excludePDC;
    }

    public BlockDataManager getBlockDataManager() {
        return blockDataManager;
    }
 
    private void loadConfigOptions() {
        excludePDC = getConfig().getBoolean("Exclude_PDC", true);
    }
 
    private void startContinuousParticleTask() {
        if (!getConfig().getBoolean("philosopher_stone.particle.enabled", true)) {
            return;
        }

        long keepAlive = getConfig().getLong("philosopher_stone.particle.keep-alive", 5);
        long period = (keepAlive == -1) ? 10L : keepAlive * 20L; // 如果为-1，则每半秒刷新一次，否则按配置的秒数刷新

        schedulerAdapter.runTimer(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                ItemStack offHand = player.getInventory().getItemInOffHand();

                if (isPhilosopherStone(mainHand) || isPhilosopherStone(offHand)) {
                    Block targetBlock = null;
                    try {
                        // 添加世界数据检查以防止在Folia环境下的NullPointerException
                        if (player.getWorld() != null && player.getWorld().isChunkLoaded(
                                player.getLocation().getBlockX() >> 4, player.getLocation().getBlockZ() >> 4)) {
                            targetBlock = player.getTargetBlock(null, 10);
                        }
                    } catch (Exception e) {
                    }
                    if (targetBlock != null && !targetBlock.getType().isAir()) {
                        philosopherStoneListener.showContinuousOutline(player, targetBlock);
                    }
                }
            }
        }, 1L, period);
    }

    public ItemStack getItemStackFromKey(String key) {
        // 首先，规范化键，移除任何已知的命名空间前缀
        if (key.startsWith("projecte:")) {
            key = key.substring("projecte:".length());
        } else if (key.startsWith("minecraft:")) {
            key = key.substring("minecraft:".length());
        }

        switch (key) {
            // Fuels
            case "alchemical_coal": return fuelManager.getAlchemicalCoal();
            case "mobius_fuel": return fuelManager.getMobiusFuel();
            case "aeternalis_fuel": return fuelManager.getAeternalisFuel();
            case "alchemical_coal_block": return fuelManager.getAlchemicalCoalBlock();
            case "mobius_fuel_block": return fuelManager.getMobiusFuelBlock();
            case "aeternalis_fuel_block": return fuelManager.getAeternalisFuelBlock();

            // Materials
            case "dark_matter": return fuelManager.getDarkMatter();
            case "red_matter": return fuelManager.getRedMatter();
            case "dark_matter_block": return fuelManager.getDarkMatterBlock();
            case "red_matter_block": return fuelManager.getRedMatterBlock();

            // Covalence Dust
            case "low_covalence_dust": return covalenceDust.getLowCovalenceDust();
            case "medium_covalence_dust": return covalenceDust.getMediumCovalenceDust();
            case "high_covalence_dust": return covalenceDust.getHighCovalenceDust();

            // Divining Rods
            case "low_divining_rod": return diviningRod.getLowDiviningRod();
            case "medium_divining_rod": return diviningRod.getMediumDiviningRod();
            case "high_divining_rod": return diviningRod.getHighDiviningRod();

            // Talismans
            case "repair_talisman": return repairTalisman.getRepairTalisman();

            // Klein Stars (Corrected Keys)
            case "klein_star_ein": return kleinStarManager.getKleinStar(1);
            case "klein_star_zwei": return kleinStarManager.getKleinStar(2);
            case "klein_star_drei": return kleinStarManager.getKleinStar(3);
            case "klein_star_vier": return kleinStarManager.getKleinStar(4);
            case "klein_star_sphere": return kleinStarManager.getKleinStar(5);
            case "klein_star_omega": return kleinStarManager.getKleinStar(6);

            default:
                // 尝试将剩余的键解析为原生Minecraft材料
                Material material = versionAdapter.getMaterial(key.toUpperCase());
                if (material != null) {
                    return new ItemStack(material);
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

                for (String key : commandsConfig.getConfigurationSection("OpenTransmutationTable").getKeys(false)) {
                    String commandName = commandsConfig.getString("OpenTransmutationTable." + key + ".command");
                    if (commandName.contains(" ")) {
                        getLogger().info("Skipping registration of sub-command: " + commandName);
                        continue;
                    }
                    String description = commandsConfig.getString("OpenTransmutationTable." + key + ".description");

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
                DebugManager.log("debug.emc.custom_emc_loaded", placeholders);
            }
        }
        getLogger().info("Loaded custom EMC values from custommoditememc.yml");
    }

    private void setKleinStarEmcValues() {
        // This method is now obsolete and replaced by loadCustomEmcValues()
        // It is kept to prevent compilation errors from old calls.
    }
}
