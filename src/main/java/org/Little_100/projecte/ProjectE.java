package org.Little_100.projecte;

import org.Little_100.projecte.AlchemicalBag.AlchemicalBagManager;
import org.Little_100.projecte.compatibility.SchedulerAdapter;
import org.Little_100.projecte.compatibility.SchedulerMatcher;
import org.Little_100.projecte.compatibility.VersionAdapter;
import org.Little_100.projecte.compatibility.VersionMatcher;
import org.bukkit.Bukkit;
import org.Little_100.projecte.TransmutationTable.GUIListener;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.Little_100.projecte.storage.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

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
    private SchedulerAdapter schedulerAdapter;
    //private FuelManager fuelManager;
    // 禁用fuelmanager因为没昨晚 仍然有问题
    // 定义矿物升级顺序
    private final Map<Material, Material> upgradeMap = new HashMap<>();
    private final Map<Material, Material> downgradeMap = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        // 保存默认配置
        saveDefaultConfig();

        // 自动释放所有 jar 内的 yml 文件到 data 文件夹
        try {
            java.util.zip.ZipInputStream zip = new java.util.zip.ZipInputStream(
                getClass().getProtectionDomain().getCodeSource().getLocation().openStream()
            );
            java.util.zip.ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                // 跳过 plugin.yml，只释放其他 yml 文件
                if (name.endsWith(".yml") && !entry.isDirectory() && !name.equals("plugin.yml")) {
                    java.io.File outFile = new java.io.File(getDataFolder(), name.substring(name.lastIndexOf('/') + 1));
                    if (!outFile.exists()) {
                        saveResource(name, false);
                        getLogger().info("自动释放语言文件: " + name);
                    }
                }
            }
            zip.close();
        } catch (Exception e) {
            getLogger().warning("自动释放 yml 语言文件时出错: " + e.getMessage());
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
        
        // 初始化调度器
        schedulerAdapter = SchedulerMatcher.getSchedulerAdapter(this);
 
        // 初始化语言管理器
        languageManager = new LanguageManager(this);

        // 初始化EMC管理器
        emcManager = new EmcManager(this);
        try {
            getLogger().info("Calculating EMC values...");
            emcManager.calculateAndStoreEmcValues();
            getLogger().info("EMC calculation complete.");
        } catch (Exception e) {
            getLogger().severe("An error occurred during EMC calculation. Some items may not have EMC values. Please check your config.yml for formatting errors.");
            e.printStackTrace();
        }

        // 初始化贤者之石物品
        createPhilosopherStone();
        
        // 初始化特殊燃料管理器
        //fuelManager = new FuelManager(this);
        //getServer().getPluginManager().registerEvents(fuelManager, this);
        //getLogger().info("FuelManager initialized with special fuels.");
        
        // 设置特殊燃料的EMC值
        //fuelManager.setFuelEmcValues();
        
        // 输出特殊燃料的NBT标签信息，供用户添加材质
        //getLogger().info(fuelManager.getNbtTagInfo());

        // 初始化配方管理器
        recipeManager = new RecipeManager(this);
        recipeManager.registerAllRecipes();

        // 移除原版爆裂紫颂果配方
        removeVanillaRecipe();

        // 初始化矿物转换映射
        initMaterialMaps();

        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(new PhilosopherStoneListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PhilosopherStoneListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ItemStackLimitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);

        // 注册命令
        CommandManager commandManager = new CommandManager(this);
        getCommand("projecte").setExecutor(commandManager);
        getCommand("projecte").setTabCompleter(commandManager);

        // 初始化炼金术袋管理器
        if (getConfig().getBoolean("AlchemicalBag.enabled", true)) {
            alchemicalBagManager = new AlchemicalBagManager(this);
            alchemicalBagManager.register();
            getLogger().info("Alchemical Bag feature is enabled.");
        } else {
            getLogger().info("Alchemical Bag feature is disabled in the config.");
        }
        
        // 初始化资源包管理器
        resourcePackManager = new ResourcePackManager(this);
        //getServer().getPluginManager().registerEvents(resourcePackManager, this);
        getLogger().info("Resource Pack Manager initialized.");

        getLogger().info("ProjectE插件已启用!");
    }
    
    @Override
    public void onDisable() {
        // 卸载所有自定义配方
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

        getLogger().info("ProjectE插件已禁用!");
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
        if (stoneMaterial == null) { // 兼容旧版本
            stoneMaterial = Material.NETHER_STAR;
        }
        return item.getType() == stoneMaterial;
    }
    
    public NamespacedKey getPhilosopherStoneKey() {
        return philosopherStoneKey;
    }
    
    private void createPhilosopherStone() {
        Material stoneMaterial = versionAdapter.getMaterial("POPPED_CHORUS_FRUIT");
        if (stoneMaterial == null) { // 兼容旧版本
            stoneMaterial = Material.NETHER_STAR;
        }
        philosopherStone = new ItemStack(stoneMaterial, 1);
        ItemMeta meta = philosopherStone.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "贤者之石");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "强大的炼金术道具",
                ChatColor.YELLOW + "可转换矿物",
                ChatColor.YELLOW + "潜行+右键打开工作台"
        ));
        meta.setUnbreakable(true);
        philosopherStoneKey = new NamespacedKey(this, "philosopher_stone");
        philosopherStone.setItemMeta(meta);
        philosopherStone.setAmount(1); // 确保堆叠数量为1
    }
    
    private void removeVanillaRecipe() {
        try {
            // 移除原版爆裂紫颂果配方(替换为贤者之石的配方)
            Bukkit.removeRecipe(NamespacedKey.minecraft("popped_chorus_fruit"));
        } catch (Exception e) {
            getLogger().warning("无法移除原版爆裂紫颂果配方: " + e.getMessage());
        }
    }
    
    private void initMaterialMaps() {
        // 设置矿物升级路径
        upgradeMap.put(Material.COAL, Material.COPPER_INGOT);
        upgradeMap.put(Material.COPPER_INGOT, Material.IRON_INGOT);
        upgradeMap.put(Material.IRON_INGOT, Material.GOLD_INGOT);
        upgradeMap.put(Material.GOLD_INGOT, Material.DIAMOND);
        upgradeMap.put(Material.DIAMOND_BLOCK, Material.NETHERITE_INGOT);
        
        // 设置矿物降级路径
        downgradeMap.put(Material.COPPER_INGOT, Material.COAL);
        downgradeMap.put(Material.IRON_INGOT, Material.COPPER_INGOT);
        downgradeMap.put(Material.GOLD_INGOT, Material.IRON_INGOT);
        downgradeMap.put(Material.DIAMOND, Material.GOLD_INGOT);
        downgradeMap.put(Material.NETHERITE_INGOT, Material.DIAMOND);
    }
    
    /**
     * 获取配方管理器
     */
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

        // 重新加载语言文件
        languageManager.loadLanguageFiles();

        // 注销并重新注册所有配方
        if (recipeManager != null) {
            recipeManager.unregisterAllRecipes();
            recipeManager.registerAllRecipes();
            getLogger().info("All recipes have been reloaded from recipe.yml.");
        }

        // 重新计算 EMC 值
        emcManager = new EmcManager(this);
        emcManager.calculateAndStoreEmcValues();

        // 重新注册命令执行器 (如果需要的话，但通常不是必须的)
        // CommandManager commandManager = new CommandManager(this);
        // getCommand("projecte").setExecutor(commandManager);
        // getCommand("projecte").setTabCompleter(commandManager);

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
    
    public Material getUpgradedMaterial(Material material) {
        return upgradeMap.getOrDefault(material, null);
    }
    
    public Material getDowngradedMaterial(Material material) {
        return downgradeMap.getOrDefault(material, null);
    }
    
    /*
    public FuelManager getFuelManager() {
        return fuelManager;
    }
    */
}
