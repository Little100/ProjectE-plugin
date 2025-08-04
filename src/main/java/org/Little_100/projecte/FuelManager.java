package org.Little_100.projecte;

import org.Little_100.projecte.storage.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FuelManager implements Listener {
    private final ProjectE plugin;
    private final org.Little_100.projecte.compatibility.SchedulerAdapter scheduler;
    
    private final NamespacedKey alchemicalCoalKey;
    private final NamespacedKey mobiusFuelKey;
    private final NamespacedKey aeternalisFuelKey;
    
    // 燃料
    private ItemStack alchemicalCoal;
    private ItemStack mobiusFuel;
    private ItemStack aeternalisFuel;
    
    // 燃料块
    private ItemStack alchemicalCoalBlock;
    private ItemStack mobiusFuelBlock;
    private ItemStack aeternalisFuelBlock;
    
    private final Map<String, Integer> burnTimes = new HashMap<>();
    private final boolean isModernVersion;
    
    public FuelManager(ProjectE plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getSchedulerAdapter();
        
        alchemicalCoalKey = new NamespacedKey(plugin, "alchemical_coal");
        mobiusFuelKey = new NamespacedKey(plugin, "mobius_fuel");
        aeternalisFuelKey = new NamespacedKey(plugin, "aeternalis_fuel");
        
        // 检查版本1.21.4=+
        this.isModernVersion = isVersion1_21_4OrNewer();
        
        burnTimes.put("alchemical_coal", 320 * 20);
        burnTimes.put("mobius_fuel", 1280 * 20);
        burnTimes.put("aeternalis_fuel", 5120 * 20);
        burnTimes.put("alchemical_coal_block", 2880 * 20);
        burnTimes.put("mobius_fuel_block", 11520 * 20);
        burnTimes.put("aeternalis_fuel_block", 46080 * 20);
        
        plugin.getLogger().info("FuelManager: Detected version " + Bukkit.getServer().getBukkitVersion() + (isModernVersion ? " (using modern material handling)" : " (using legacy material handling)"));
        
        createFuelItems();
    }
    
    private boolean isVersion1_21_4OrNewer() {
        try {
            String version = Bukkit.getServer().getBukkitVersion().split("-")[0];
            String[] versionParts = version.split("\\.");
            
            if (versionParts.length >= 2) {
                int major = Integer.parseInt(versionParts[0]);
                int minor = Integer.parseInt(versionParts[1]);
                
                if (major > 1) return true;
                if (major == 1 && minor > 21) return true;
                if (major == 1 && minor == 21 && versionParts.length >= 3) {
                    int patch = Integer.parseInt(versionParts[2]);
                    return patch >= 4;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not parse version number, using legacy mode: " + e.getMessage());
        }
        return false;
    }
    
    private void createFuelItems() {
        if (isModernVersion) {
            createModernFuelItems();
        } else {
            createLegacyFuelItems();
        }
    }
    
    private void createModernFuelItems() {
        plugin.getLogger().info("Creating fuel items using modern API (PLAYER_HEAD)...");

        alchemicalCoal = createFuelItem(Material.COAL, "item.alchemical_coal.name", 1,
                Arrays.asList("item.alchemical_coal.lore1", "item.alchemical_coal.lore2"),
                alchemicalCoalKey, (byte) 1, "alchemical_coal");

        mobiusFuel = createFuelItem(Material.COAL, "item.mobius_fuel.name", 2,
                Arrays.asList("item.mobius_fuel.lore1", "item.mobius_fuel.lore2"),
                mobiusFuelKey, (byte) 1, "mobius_fuel");

        aeternalisFuel = createFuelItem(Material.COAL, "item.aeternalis_fuel.name", 3,
                Arrays.asList("item.aeternalis_fuel.lore1", "item.aeternalis_fuel.lore2"),
                aeternalisFuelKey, (byte) 1, "aeternalis_fuel");

        alchemicalCoalBlock = createFuelItem(Material.COAL_BLOCK, "item.alchemical_coal_block.name", 1,
                Arrays.asList("item.alchemical_coal_block.lore1", "item.alchemical_coal_block.lore2"),
                alchemicalCoalKey, (byte) 2, "alchemical_coal_block");

        mobiusFuelBlock = createFuelItem(Material.COAL_BLOCK, "item.mobius_fuel_block.name", 2,
                Arrays.asList("item.mobius_fuel_block.lore1", "item.mobius_fuel_block.lore2"),
                mobiusFuelKey, (byte) 2, "mobius_fuel_block");

        aeternalisFuelBlock = createFuelItem(Material.COAL_BLOCK, "item.aeternalis_fuel_block.name", 3,
                Arrays.asList("item.aeternalis_fuel_block.lore1", "item.aeternalis_fuel_block.lore2"),
                aeternalisFuelKey, (byte) 2, "aeternalis_fuel_block");
    }

    private ItemStack createFuelItem(Material material, String displayNameKey, int customModelData, java.util.List<String> loreKeys, NamespacedKey key, byte value, String id) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getLanguageManager().get(displayNameKey));
            List<String> translatedLore = loreKeys.stream()
                    .map(loreKey -> plugin.getLanguageManager().get(loreKey))
                    .collect(java.util.stream.Collectors.toList());
            meta.setLore(translatedLore);
            
            if (isModernVersion) {
                // 使用新的CustomModelDataComponent(1.21.4+)
                // 操蛋ojang更新什么custommodeldata😡
                try {
                    CustomModelDataComponent component = meta.getCustomModelDataComponent();
                    if (component == null) {
                        component = meta.getCustomModelDataComponent();
                    }
                    // 使用字符串标识符
                    component.setStrings(List.of(String.valueOf(customModelData)));
                    meta.setCustomModelDataComponent(component);
                } catch (Exception e) {
                    plugin.getLogger().warning("Could not set CustomModelDataComponent: " + e.getMessage());
                    // 回退到旧版
                    meta.setCustomModelData(customModelData);
                }
            } else {
                // 使用旧版的CustomModelData
                meta.setCustomModelData(customModelData);
            }
            
            item.setItemMeta(meta);
            addFuelTags(item, key, value, id);
        }
        return item;
    }
    
    private void addFuelTags(ItemStack item, NamespacedKey key, byte value, String id) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(key, PersistentDataType.BYTE, value);
            container.set(new NamespacedKey(plugin, "projecte_id"), PersistentDataType.STRING, id);
            item.setItemMeta(meta);
        }
    }
    
    private void createLegacyFuelItems() {
        plugin.getLogger().info("Creating fuel items using legacy method...");

        alchemicalCoal = createFuelItem(Material.PLAYER_HEAD, "item.alchemical_coal.name", 1,
                Arrays.asList("item.alchemical_coal.lore1", "item.alchemical_coal.lore2"),
                alchemicalCoalKey, (byte) 1, "alchemical_coal");

        mobiusFuel = createFuelItem(Material.PLAYER_HEAD, "item.mobius_fuel.name", 2,
                Arrays.asList("item.mobius_fuel.lore1", "item.mobius_fuel.lore2"),
                mobiusFuelKey, (byte) 1, "mobius_fuel");

        aeternalisFuel = createFuelItem(Material.PLAYER_HEAD, "item.aeternalis_fuel.name", 3,
                Arrays.asList("item.aeternalis_fuel.lore1", "item.aeternalis_fuel.lore2"),
                aeternalisFuelKey, (byte) 1, "aeternalis_fuel");

        alchemicalCoalBlock = createFuelItem(Material.PLAYER_HEAD, "item.alchemical_coal_block.name", 4,
                Arrays.asList("item.alchemical_coal_block.lore1", "item.alchemical_coal_block.lore2"),
                alchemicalCoalKey, (byte) 2, "alchemical_coal_block");

        mobiusFuelBlock = createFuelItem(Material.PLAYER_HEAD, "item.mobius_fuel_block.name", 5,
                Arrays.asList("item.mobius_fuel_block.lore1", "item.mobius_fuel_block.lore2"),
                mobiusFuelKey, (byte) 2, "mobius_fuel_block");

        aeternalisFuelBlock = createFuelItem(Material.PLAYER_HEAD, "item.aeternalis_fuel_block.name", 6,
                Arrays.asList("item.aeternalis_fuel_block.lore1", "item.aeternalis_fuel_block.lore2"),
                aeternalisFuelKey, (byte) 2, "aeternalis_fuel_block");
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
    
    // 检查是否为特定的燃料物品
    public boolean isAlchemicalCoal(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(alchemicalCoalKey, PersistentDataType.BYTE) &&
               container.get(alchemicalCoalKey, PersistentDataType.BYTE) == (byte) 1;
    }
    
    public boolean isMobiusFuel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(mobiusFuelKey, PersistentDataType.BYTE) &&
               container.get(mobiusFuelKey, PersistentDataType.BYTE) == (byte) 1;
    }
    
    public boolean isAeternalisFuel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(aeternalisFuelKey, PersistentDataType.BYTE) &&
               container.get(aeternalisFuelKey, PersistentDataType.BYTE) == (byte) 1;
    }
    
    public boolean isAlchemicalCoalBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(alchemicalCoalKey, PersistentDataType.BYTE) && 
               container.get(alchemicalCoalKey, PersistentDataType.BYTE) == (byte) 2;
    }
    
    public boolean isMobiusFuelBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(mobiusFuelKey, PersistentDataType.BYTE) && 
               container.get(mobiusFuelKey, PersistentDataType.BYTE) == (byte) 2;
    }
    
    public boolean isAeternalisFuelBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(aeternalisFuelKey, PersistentDataType.BYTE) && 
               container.get(aeternalisFuelKey, PersistentDataType.BYTE) == (byte) 2;
    }
    
    // 熔炉燃烧事件处理器
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
    
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        ItemStack[] matrix = event.getInventory().getMatrix();
        ItemStack result = checkFuelRecipe(matrix);

        if (result != null) {
            event.getInventory().setResult(result);
        }
    }

    @EventHandler
    public void onCraftItem(InventoryClickEvent event) {
        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;
        if (!(event.getInventory() instanceof CraftingInventory)) return;

        CraftingInventory inv = (CraftingInventory) event.getInventory();
        ItemStack result = inv.getResult();
        if (result == null) return;

        ItemStack recipeResult = checkFuelRecipe(inv.getMatrix());
        if (recipeResult == null || !recipeResult.isSimilar(result)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (event.isShiftClick()) {
            event.setCancelled(true);
            int maxCrafts = calculateMaxCrafts(inv);
            if (maxCrafts <= 0) return;

            ItemStack resultStack = result.clone();
            resultStack.setAmount(result.getAmount() * maxCrafts);

            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(resultStack);

            int craftedAmount = resultStack.getAmount();
            if (!leftovers.isEmpty()) {
                craftedAmount -= leftovers.get(0).getAmount();
            }
            int numCrafted = craftedAmount / result.getAmount();

            if (numCrafted > 0) {
                consumeIngredients(inv, numCrafted);
                player.updateInventory();
            }
        } else { // Normal click
            event.setCancelled(true);
            if (player.getItemOnCursor() == null || player.getItemOnCursor().getType() == Material.AIR) {
                consumeIngredients(inv, 1);
                player.setItemOnCursor(result);
                player.updateInventory();
            } else if (player.getItemOnCursor().isSimilar(result)) {
                int canAdd = result.getMaxStackSize() - player.getItemOnCursor().getAmount();
                if (canAdd >= result.getAmount()) {
                    consumeIngredients(inv, 1);
                    player.getItemOnCursor().setAmount(player.getItemOnCursor().getAmount() + result.getAmount());
                    player.updateInventory();
                }
            }
        }
    }

    private int getItemsPerCraft(CraftingInventory inv) {
        ItemStack[] matrix = inv.getMatrix();
        if (isRecipe(matrix, Material.COAL, 4)) return 4;
        if (isRecipe(matrix, "alchemical_coal", 4)) return 4;
        if (isRecipe(matrix, "mobius_fuel", 4)) return 4;
        if (isRecipe(matrix, "alchemical_coal", 9)) return 9;
        if (isRecipe(matrix, "mobius_fuel", 9)) return 9;
        if (isRecipe(matrix, "aeternalis_fuel", 9)) return 9;
        if (isSingleItemRecipe(matrix, "alchemical_coal_block")) return 1;
        if (isSingleItemRecipe(matrix, "mobius_fuel_block")) return 1;
        if (isSingleItemRecipe(matrix, "aeternalis_fuel_block")) return 1;
        if (isDowngradeRecipe(matrix, "aeternalis_fuel")) return 1;
        if (isDowngradeRecipe(matrix, "mobius_fuel")) return 1;
        if (isDowngradeRecipe(matrix, "alchemical_coal")) return 1;
        return 0;
    }

    private int calculateMaxCrafts(CraftingInventory inv) {
        int itemsPerCraft = getItemsPerCraft(inv);
        if (itemsPerCraft == 0) return 0;

        int totalIngredients = 0;
        for (ItemStack item : inv.getMatrix()) {
            if (item != null && item.getType() != Material.AIR && !plugin.isPhilosopherStone(item)) {
                totalIngredients += item.getAmount();
            }
        }
        return totalIngredients / itemsPerCraft;
    }

    private void consumeIngredients(CraftingInventory inv, int numCrafts) {
        int itemsPerCraft = getItemsPerCraft(inv);
        if (itemsPerCraft == 0) return;

        ItemStack[] matrix = inv.getMatrix();
        
        // 检查是否为9个物品的配方（燃料块合成）
        if (itemsPerCraft == 9) {
            // 对于9个物品的配方，每个格子消耗 numCrafts 个物品
            for (int i = 0; i < matrix.length; i++) {
                if (matrix[i] != null && matrix[i].getType() != Material.AIR && !plugin.isPhilosopherStone(matrix[i])) {
                    int amount = matrix[i].getAmount();
                    int consume = Math.min(amount, numCrafts);
                    matrix[i].setAmount(amount - consume);
                }
            }
        } else {
            // 对于其他配方，使用原来的逻辑
            int toConsume = numCrafts * itemsPerCraft;
            for (int i = 0; i < matrix.length; i++) {
                if (matrix[i] != null && matrix[i].getType() != Material.AIR && !plugin.isPhilosopherStone(matrix[i])) {
                    int amount = matrix[i].getAmount();
                    int consume = Math.min(amount, toConsume);
                    matrix[i].setAmount(amount - consume);
                    toConsume -= consume;
                    if (toConsume <= 0) break;
                }
            }
        }
        inv.setMatrix(matrix);
    }

    private boolean isSingleItemRecipe(ItemStack[] matrix, String fuelBlockType) {
        int itemCount = 0;
        ItemStack singleItem = null;
        for (ItemStack item : matrix) {
            if (item != null && item.getType() != Material.AIR) {
                itemCount++;
                singleItem = item;
            }
        }
        if (itemCount != 1) {
            return false;
        }
        return isFuelItem(singleItem, fuelBlockType);
    }

    private boolean isDowngradeRecipe(ItemStack[] matrix, String inputFuelType) {
        int fuelCount = 0;
        boolean hasPhilosopherStone = false;
        int otherItems = 0;
        for (ItemStack item : matrix) {
            if (item != null && item.getType() != Material.AIR) {
                if (isFuelItem(item, inputFuelType)) {
                    fuelCount++;
                } else if (plugin.isPhilosopherStone(item)) {
                    hasPhilosopherStone = true;
                } else {
                    otherItems++;
                }
            }
        }
        return fuelCount == 1 && hasPhilosopherStone && otherItems == 0;
    }

    private ItemStack checkFuelRecipe(ItemStack[] matrix) {
        // 升级配方
        if (isRecipe(matrix, Material.COAL, 4)) return getAlchemicalCoal();
        if (isRecipe(matrix, "alchemical_coal", 4)) return getMobiusFuel();
        if (isRecipe(matrix, "mobius_fuel", 4)) return getAeternalisFuel();
        if (isRecipe(matrix, "alchemical_coal", 9)) return getAlchemicalCoalBlock();
        if (isRecipe(matrix, "mobius_fuel", 9)) return getMobiusFuelBlock();
        if (isRecipe(matrix, "aeternalis_fuel", 9)) return getAeternalisFuelBlock();

        // 方块转燃料配方
        if (isSingleItemRecipe(matrix, "alchemical_coal_block")) {
            ItemStack result = getAlchemicalCoal();
            result.setAmount(9);
            return result;
        }
        if (isSingleItemRecipe(matrix, "mobius_fuel_block")) {
            ItemStack result = getMobiusFuel();
            result.setAmount(9);
            return result;
        }
        if (isSingleItemRecipe(matrix, "aeternalis_fuel_block")) {
            ItemStack result = getAeternalisFuel();
            result.setAmount(9);
            return result;
        }

        // 降级配方
        if (isDowngradeRecipe(matrix, "aeternalis_fuel")) {
            ItemStack result = getMobiusFuel();
            result.setAmount(4);
            return result;
        }
        if (isDowngradeRecipe(matrix, "mobius_fuel")) {
            ItemStack result = getAlchemicalCoal();
            result.setAmount(4);
            return result;
        }
        if (isDowngradeRecipe(matrix, "alchemical_coal")) {
            return new ItemStack(Material.COAL, 4);
        }

        return null;
    }

    private boolean isRecipe(ItemStack[] matrix, Material material, int count) {
        int materialCount = 0;
        boolean hasPhilosopherStone = false;
        for (ItemStack item : matrix) {
            if (item != null && item.getType() != Material.AIR) {
                if (item.getType() == material && !isFuelItem(item)) {
                    materialCount++;
                } else if (plugin.isPhilosopherStone(item)) {
                    hasPhilosopherStone = true;
                } else {
                    return false; // 合成格中有额外物品
                }
            }
        }
        return materialCount == count && hasPhilosopherStone;
    }

    private boolean isRecipe(ItemStack[] matrix, String fuelType, int count) {
        int fuelCount = 0;
        boolean hasPhilosopherStone = false;
        for (ItemStack item : matrix) {
            if (item != null && item.getType() != Material.AIR) {
                if (isFuelItem(item, fuelType)) {
                    fuelCount++;
                } else if (plugin.isPhilosopherStone(item)) {
                    hasPhilosopherStone = true;
                } else {
                    return false; // 合成格中有额外物品
                }
            }
        }
        return fuelCount == count && (count == 9 || hasPhilosopherStone);
    }
    
    
    // 获取燃料类型的CustomModelData
    private String getModelDataForFuelType(String fuelType) {
        switch (fuelType) {
            case "alchemical_coal": return "1";
            case "mobius_fuel": return "2";
            case "aeternalis_fuel": return "3";
            case "alchemical_coal_block": return "1";
            case "mobius_fuel_block": return "2";
            case "aeternalis_fuel_block": return "3";
            default: return "1";
        }
    }
    
    // 检查是否为燃料物品
    private boolean isFuelItem(ItemStack item, String fuelType) {
        if (item == null || !item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        return container.has(new NamespacedKey(plugin, "projecte_id"), PersistentDataType.STRING) &&
               fuelType.equals(container.get(new NamespacedKey(plugin, "projecte_id"), PersistentDataType.STRING));
    }

    private boolean isFuelItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(new NamespacedKey(plugin, "projecte_id"), PersistentDataType.STRING);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isFuelItem(event.getItemInHand())) {
            event.setCancelled(true);
        }
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
        
        return info.toString();
    }
    
    // 给特定的物品设置EMC值
    public void setFuelEmcValues() {
        // 煤炭的EMC值为128，我们可以根据配方计算出其他燃料的EMC值
        DatabaseManager db = plugin.getDatabaseManager();
        
        // 炼金煤炭 = 4个煤炭 + 1个贤者之石 = 4 * 128 = 512 EMC
        db.setEmc(plugin.getEmcManager().getItemKey(getAlchemicalCoal()), 512);

        // 莫比乌斯燃料 = 4个炼金煤炭 + 1个贤者之石 = 4 * 512 = 2048 EMC
        db.setEmc(plugin.getEmcManager().getItemKey(getMobiusFuel()), 2048);

        // 永恒燃料 = 4个莫比乌斯燃料 + 1个贤者之石 = 4 * 2048 = 8192 EMC
        db.setEmc(plugin.getEmcManager().getItemKey(getAeternalisFuel()), 8192);

        // 炼金煤炭块 = 9个炼金煤炭 = 9 * 512 = 4608 EMC
        db.setEmc(plugin.getEmcManager().getItemKey(getAlchemicalCoalBlock()), 4608);

        // 莫比乌斯燃料块 = 9个莫比乌斯燃料 = 9 * 2048 = 18432 EMC
        db.setEmc(plugin.getEmcManager().getItemKey(getMobiusFuelBlock()), 18432);

        // 永恒燃料块 = 9个永恒燃料 = 9 * 8192 = 73728 EMC
        db.setEmc(plugin.getEmcManager().getItemKey(getAeternalisFuelBlock()), 73728);
    }
}