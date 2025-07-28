package org.Little_100.projecte;

import org.Little_100.projecte.storage.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FuelManager implements Listener {
    // 注意 此文件目前属于禁用状态 未完成
    
    private final ProjectE plugin;
    
    private final NamespacedKey alchemicalCoalKey;
    private final NamespacedKey mobiusFuelKey;
    private final NamespacedKey aeternalisFuelKey;
    
    // 燃料物品
    private ItemStack alchemicalCoal;
    private ItemStack mobiusFuel;
    private ItemStack aeternalisFuel;
    
    // 燃料块
    private ItemStack alchemicalCoalBlock;
    private ItemStack mobiusFuelBlock;
    private ItemStack aeternalisFuelBlock;
    
    private final Map<String, Integer> burnTimes = new HashMap<>();
    
    public FuelManager(ProjectE plugin) {
        this.plugin = plugin;
        
        alchemicalCoalKey = new NamespacedKey(plugin, "alchemical_coal");
        mobiusFuelKey = new NamespacedKey(plugin, "mobius_fuel");
        aeternalisFuelKey = new NamespacedKey(plugin, "aeternalis_fuel");
        
        burnTimes.put("alchemical_coal", 320 * 20);       // 320秒
        burnTimes.put("mobius_fuel", 1280 * 20);          // 1280秒
        burnTimes.put("aeternalis_fuel", 5120 * 20);      // 5120秒
        burnTimes.put("alchemical_coal_block", 2880 * 20); // 2880秒
        burnTimes.put("mobius_fuel_block", 11520 * 20);    // 11520秒
        burnTimes.put("aeternalis_fuel_block", 46080 * 20); // 46080秒
        
        // 创建燃料物品
        createFuelItems();
    }
    
    private void createFuelItems() {
        // 炼金煤炭
        alchemicalCoal = new ItemStack(Material.COAL);
        ItemMeta alchemicalCoalMeta = alchemicalCoal.getItemMeta();
        alchemicalCoalMeta.setDisplayName(ChatColor.RED + "炼金煤炭");
        alchemicalCoalMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "高效的燃料",
                ChatColor.YELLOW + "燃烧时间: 320秒"
        ));
        alchemicalCoalMeta.setCustomModelData(1);
        alchemicalCoalMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "custom_model_data"), PersistentDataType.STRING, "alchemical_coal");
        PersistentDataContainer alchemicalCoalContainer = alchemicalCoalMeta.getPersistentDataContainer();
        alchemicalCoalContainer.set(alchemicalCoalKey, PersistentDataType.BYTE, (byte) 1);
        alchemicalCoal.setItemMeta(alchemicalCoalMeta);
        
        // 莫比乌斯燃料
        mobiusFuel = new ItemStack(Material.COAL);
        ItemMeta mobiusFuelMeta = mobiusFuel.getItemMeta();
        mobiusFuelMeta.setDisplayName(ChatColor.RED + "莫比乌斯燃料");
        mobiusFuelMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "高度浓缩的燃料",
                ChatColor.YELLOW + "燃烧时间: 1280秒"
        ));
        mobiusFuelMeta.setCustomModelData(2);
        mobiusFuelMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "custom_model_data"), PersistentDataType.STRING, "mobius_fuel");
        PersistentDataContainer mobiusFuelContainer = mobiusFuelMeta.getPersistentDataContainer();
        mobiusFuelContainer.set(mobiusFuelKey, PersistentDataType.BYTE, (byte) 1);
        mobiusFuel.setItemMeta(mobiusFuelMeta);
        
        // 永恒燃料
        aeternalisFuel = new ItemStack(Material.COAL);
        ItemMeta aeternalisFuelMeta = aeternalisFuel.getItemMeta();
        aeternalisFuelMeta.setDisplayName(ChatColor.RED + "永恒燃料");
        aeternalisFuelMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "几近永恒的燃料",
                ChatColor.YELLOW + "燃烧时间: 5120秒"
        ));
        aeternalisFuelMeta.setCustomModelData(3);
        aeternalisFuelMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "custom_model_data"), PersistentDataType.STRING, "aeternalis_fuel");
        PersistentDataContainer aeternalisFuelContainer = aeternalisFuelMeta.getPersistentDataContainer();
        aeternalisFuelContainer.set(aeternalisFuelKey, PersistentDataType.BYTE, (byte) 1);
        aeternalisFuel.setItemMeta(aeternalisFuelMeta);
        
        // 炼金煤炭块
        alchemicalCoalBlock = new ItemStack(Material.COAL_BLOCK);
        ItemMeta alchemicalCoalBlockMeta = alchemicalCoalBlock.getItemMeta();
        alchemicalCoalBlockMeta.setDisplayName(ChatColor.RED + "炼金煤炭块");
        alchemicalCoalBlockMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "高效的燃料块",
                ChatColor.YELLOW + "燃烧时间: 2880秒"
        ));
        alchemicalCoalBlockMeta.setCustomModelData(4);
        alchemicalCoalBlockMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "custom_model_data"), PersistentDataType.STRING, "alchemical_coal_block");
        PersistentDataContainer alchemicalCoalBlockContainer = alchemicalCoalBlockMeta.getPersistentDataContainer();
        alchemicalCoalBlockContainer.set(alchemicalCoalKey, PersistentDataType.BYTE, (byte) 2);
        alchemicalCoalBlock.setItemMeta(alchemicalCoalBlockMeta);
        
        // 莫比乌斯燃料块
        mobiusFuelBlock = new ItemStack(Material.COAL_BLOCK);
        ItemMeta mobiusFuelBlockMeta = mobiusFuelBlock.getItemMeta();
        mobiusFuelBlockMeta.setDisplayName(ChatColor.RED + "莫比乌斯燃料块");
        mobiusFuelBlockMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "高度浓缩的燃料块",
                ChatColor.YELLOW + "燃烧时间: 11520秒"
        ));
        mobiusFuelBlockMeta.setCustomModelData(5);
        mobiusFuelBlockMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "custom_model_data"), PersistentDataType.STRING, "mobius_fuel_block");
        PersistentDataContainer mobiusFuelBlockContainer = mobiusFuelBlockMeta.getPersistentDataContainer();
        mobiusFuelBlockContainer.set(mobiusFuelKey, PersistentDataType.BYTE, (byte) 2);
        mobiusFuelBlock.setItemMeta(mobiusFuelBlockMeta);
        
        // 永恒燃料块
        aeternalisFuelBlock = new ItemStack(Material.COAL_BLOCK);
        ItemMeta aeternalisFuelBlockMeta = aeternalisFuelBlock.getItemMeta();
        aeternalisFuelBlockMeta.setDisplayName(ChatColor.RED + "永恒燃料块");
        aeternalisFuelBlockMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "几近永恒的燃料块",
                ChatColor.YELLOW + "燃烧时间: 46080秒"
        ));
        aeternalisFuelBlockMeta.setCustomModelData(6);
        aeternalisFuelBlockMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "custom_model_data"), PersistentDataType.STRING, "aeternalis_fuel_block");
        PersistentDataContainer aeternalisFuelBlockContainer = aeternalisFuelBlockMeta.getPersistentDataContainer();
        aeternalisFuelBlockContainer.set(aeternalisFuelKey, PersistentDataType.BYTE, (byte) 2);
        aeternalisFuelBlock.setItemMeta(aeternalisFuelBlockMeta);
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
    
    // 检查是否是特定的燃料物品
    public boolean isAlchemicalCoal(ItemStack item) {
        if (item == null || item.getType() != Material.COAL) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(alchemicalCoalKey, PersistentDataType.BYTE);
    }
    
    public boolean isMobiusFuel(ItemStack item) {
        if (item == null || item.getType() != Material.COAL) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(mobiusFuelKey, PersistentDataType.BYTE);
    }
    
    public boolean isAeternalisFuel(ItemStack item) {
        if (item == null || item.getType() != Material.COAL) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(aeternalisFuelKey, PersistentDataType.BYTE);
    }
    
    public boolean isAlchemicalCoalBlock(ItemStack item) {
        if (item == null || item.getType() != Material.COAL_BLOCK) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(alchemicalCoalKey, PersistentDataType.BYTE) && 
               container.get(alchemicalCoalKey, PersistentDataType.BYTE) == (byte) 2;
    }
    
    public boolean isMobiusFuelBlock(ItemStack item) {
        if (item == null || item.getType() != Material.COAL_BLOCK) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(mobiusFuelKey, PersistentDataType.BYTE) && 
               container.get(mobiusFuelKey, PersistentDataType.BYTE) == (byte) 2;
    }
    
    public boolean isAeternalisFuelBlock(ItemStack item) {
        if (item == null || item.getType() != Material.COAL_BLOCK) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(aeternalisFuelKey, PersistentDataType.BYTE) && 
               container.get(aeternalisFuelKey, PersistentDataType.BYTE) == (byte) 2;
    }
    
    // 燃烧事件处理
    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        ItemStack fuel = event.getFuel();
        if (fuel == null) return;
        
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
    
    // 获取特殊燃料的NBT标签信息，用于添加材质
    public String getNbtTagInfo() {
        StringBuilder info = new StringBuilder();
        info.append("特殊燃料NBT标签信息：\n\n");
        
        info.append("1. 炼金煤炭 (CustomModelData: 1)\n");
        info.append("   - 命名空间键: ").append(alchemicalCoalKey.toString()).append("\n");
        info.append("   - 数据类型: BYTE\n");
        info.append("   - 数据值: 1\n\n");
        
        info.append("2. 莫比乌斯燃料 (CustomModelData: 2)\n");
        info.append("   - 命名空间键: ").append(mobiusFuelKey.toString()).append("\n");
        info.append("   - 数据类型: BYTE\n");
        info.append("   - 数据值: 1\n\n");
        
        info.append("3. 永恒燃料 (CustomModelData: 3)\n");
        info.append("   - 命名空间键: ").append(aeternalisFuelKey.toString()).append("\n");
        info.append("   - 数据类型: BYTE\n");
        info.append("   - 数据值: 1\n\n");
        
        info.append("4. 炼金煤炭块 (CustomModelData: 4)\n");
        info.append("   - 命名空间键: ").append(alchemicalCoalKey.toString()).append("\n");
        info.append("   - 数据类型: BYTE\n");
        info.append("   - 数据值: 2\n\n");
        
        info.append("5. 莫比乌斯燃料块 (CustomModelData: 5)\n");
        info.append("   - 命名空间键: ").append(mobiusFuelKey.toString()).append("\n");
        info.append("   - 数据类型: BYTE\n");
        info.append("   - 数据值: 2\n\n");
        
        info.append("6. 永恒燃料块 (CustomModelData: 6)\n");
        info.append("   - 命名空间键: ").append(aeternalisFuelKey.toString()).append("\n");
        info.append("   - 数据类型: BYTE\n");
        info.append("   - 数据值: 2\n\n");
        
        return info.toString();
    }
    
    // 设置物品的EMC值
    public void setFuelEmcValues() {
        // 煤炭的EMC值是128，根据配方我们可以计算出其他燃料的EMC值
        DatabaseManager db = plugin.getDatabaseManager();
        
        // 炼金煤炭 = 4个煤炭 + 1个贤者之石（假设贤者之石不消耗）= 4 * 128 = 512 EMC
        db.setEmc(alchemicalCoalKey.toString(), 512);
        
        // 莫比乌斯燃料 = 4个炼金煤炭 + 1个贤者之石 = 4 * 512 = 2048 EMC
        db.setEmc(mobiusFuelKey.toString(), 2048);
        
        // 永恒燃料 = 4个莫比乌斯燃料 + 1个贤者之石 = 4 * 2048 = 8192 EMC
        db.setEmc(aeternalisFuelKey.toString(), 8192);
        
        // 炼金煤炭块 = 9个炼金煤炭 = 9 * 512 = 4608 EMC
        db.setEmc(alchemicalCoalKey.toString() + "_block", 4608);
        
        // 莫比乌斯燃料块 = 9个莫比乌斯燃料 = 9 * 2048 = 18432 EMC
        db.setEmc(mobiusFuelKey.toString() + "_block", 18432);
        
        // 永恒燃料块 = 9个永恒燃料 = 9 * 8192 = 73728 EMC
        db.setEmc(aeternalisFuelKey.toString() + "_block", 73728);
    }
}