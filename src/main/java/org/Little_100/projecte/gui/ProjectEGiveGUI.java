package org.Little_100.projecte.gui;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.devices.EnergyCollector;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ProjectEGiveGUI implements Listener {

    private final ProjectE plugin;
    private final Player player;
    private Inventory inventory;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 45;
    private static final int PREV_PAGE_SLOT = 48;
    private static final int CLOSE_SLOT = 49;
    private static final int NEXT_PAGE_SLOT = 50;

    private final List<GiveItem> allItems = new ArrayList<>();

    private static class GiveItem {
        String id;
        String displayName;
        String category;
        ItemStack itemStack;

        GiveItem(String id, String displayName, String category, ItemStack itemStack) {
            this.id = id;
            this.displayName = displayName;
            this.category = category;
            this.itemStack = itemStack;
        }
    }

    public ProjectEGiveGUI(ProjectE plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        initializeItems();
    }

    private void initializeItems() {
        addItem("philosophers_stone", "§5贤者之石", "工具", plugin.getPhilosopherStone());
        addItem("dark_matter_pickaxe", "§d暗物质镐", "工具", plugin.getItemStackFromKey("dark_matter_pickaxe"));
        addItem("dark_matter_axe", "§d暗物质斧", "工具", plugin.getItemStackFromKey("dark_matter_axe"));
        addItem("dark_matter_shovel", "§d暗物质锹", "工具", plugin.getItemStackFromKey("dark_matter_shovel"));
        addItem("dark_matter_sword", "§d暗物质剑", "工具", plugin.getItemStackFromKey("dark_matter_sword"));
        addItem("dark_matter_hoe", "§d暗物质锄", "工具", plugin.getItemStackFromKey("dark_matter_hoe"));
        addItem("dark_matter_hammer", "§d暗物质锤", "工具", plugin.getItemStackFromKey("dark_matter_hammer"));
        addItem("dark_matter_shears", "§d暗物质剪刀", "工具", plugin.getItemStackFromKey("dark_matter_shears"));
        
        addItem("red_matter_pickaxe", "§c红物质镐", "工具", plugin.getItemStackFromKey("red_matter_pickaxe"));
        addItem("red_matter_axe", "§c红物质斧", "工具", plugin.getItemStackFromKey("red_matter_axe"));
        addItem("red_matter_shovel", "§c红物质锹", "工具", plugin.getItemStackFromKey("red_matter_shovel"));
        addItem("red_matter_sword", "§c红物质剑", "工具", plugin.getItemStackFromKey("red_matter_sword"));
        addItem("red_matter_hoe", "§c红物质锄", "工具", plugin.getItemStackFromKey("red_matter_hoe"));
        addItem("red_matter_hammer", "§c红物质锤", "工具", plugin.getItemStackFromKey("red_matter_hammer"));
        addItem("red_matter_shears", "§c红物质剪刀", "工具", plugin.getItemStackFromKey("red_matter_shears"));

        addItem("klein_star_ein", "§b克莱因之星 Ein", "存储", plugin.getItemStackFromKey("klein_star_ein"));
        addItem("klein_star_zwei", "§b克莱因之星 Zwei", "存储", plugin.getItemStackFromKey("klein_star_zwei"));
        addItem("klein_star_drei", "§b克莱因之星 Drei", "存储", plugin.getItemStackFromKey("klein_star_drei"));
        addItem("klein_star_vier", "§b克莱因之星 Vier", "存储", plugin.getItemStackFromKey("klein_star_vier"));
        addItem("klein_star_sphere", "§b克莱因之星 Sphere", "存储", plugin.getItemStackFromKey("klein_star_sphere"));
        addItem("klein_star_omega", "§b克莱因之星 Omega", "存储", plugin.getItemStackFromKey("klein_star_omega"));

        addItem("dark_matter", "§d暗物质", "材料", plugin.getItemStackFromKey("dark_matter"));
        addItem("red_matter", "§c红物质", "材料", plugin.getItemStackFromKey("red_matter"));
        addItem("dark_matter_block", "§d暗物质块", "材料", plugin.getItemStackFromKey("dark_matter_block"));
        addItem("red_matter_block", "§c红物质块", "材料", plugin.getItemStackFromKey("red_matter_block"));
        addItem("low_covalence_dust", "§f低等共价粉", "材料", plugin.getItemStackFromKey("low_covalence_dust"));
        addItem("medium_covalence_dust", "§a中等共价粉", "材料", plugin.getItemStackFromKey("medium_covalence_dust"));
        addItem("high_covalence_dust", "§e高等共价粉", "材料", plugin.getItemStackFromKey("high_covalence_dust"));

        addItem("alchemical_coal", "§e炼金煤炭", "燃料", plugin.getItemStackFromKey("alchemical_coal"));
        addItem("mobius_fuel", "§b莫比乌斯燃料", "燃料", plugin.getItemStackFromKey("mobius_fuel"));
        addItem("aeternalis_fuel", "§d永恒燃料", "燃料", plugin.getItemStackFromKey("aeternalis_fuel"));
        addItem("alchemical_coal_block", "§e炼金煤炭块", "燃料", plugin.getItemStackFromKey("alchemical_coal_block"));
        addItem("mobius_fuel_block", "§b莫比乌斯燃料块", "燃料", plugin.getItemStackFromKey("mobius_fuel_block"));
        addItem("aeternalis_fuel_block", "§d永恒燃料块", "燃料", plugin.getItemStackFromKey("aeternalis_fuel_block"));

        addItem("dark_matter_helmet", "§d暗物质头盔", "护甲", plugin.getItemStackFromKey("dark_matter_helmet"));
        addItem("dark_matter_chestplate", "§d暗物质胸甲", "护甲", plugin.getItemStackFromKey("dark_matter_chestplate"));
        addItem("dark_matter_leggings", "§d暗物质护腿", "护甲", plugin.getItemStackFromKey("dark_matter_leggings"));
        addItem("dark_matter_boots", "§d暗物质靴子", "护甲", plugin.getItemStackFromKey("dark_matter_boots"));
        
        addItem("red_matter_helmet", "§c红物质头盔", "护甲", plugin.getItemStackFromKey("red_matter_helmet"));
        addItem("red_matter_chestplate", "§c红物质胸甲", "护甲", plugin.getItemStackFromKey("red_matter_chestplate"));
        addItem("red_matter_leggings", "§c红物质护腿", "护甲", plugin.getItemStackFromKey("red_matter_leggings"));
        addItem("red_matter_boots", "§c红物质靴子", "护甲", plugin.getItemStackFromKey("red_matter_boots"));
        
        addItem("gem_helmet", "§b宝石头盔", "护甲", plugin.getItemStackFromKey("gem_helmet"));

        addItem("body_stone", "§6身体之石", "饰品", plugin.getItemStackFromKey("body_stone"));
        addItem("soul_stone", "§6灵魂之石", "饰品", plugin.getItemStackFromKey("soul_stone"));
        addItem("life_stone", "§6生命之石", "饰品", plugin.getItemStackFromKey("life_stone"));
        addItem("mind_stone", "§6心灵之石", "饰品", plugin.getItemStackFromKey("mind_stone"));

        addItem("dark_matter_furnace", "§d暗物质熔炉", "设备", plugin.getItemStackFromKey("dark_matter_furnace"));
        addItem("red_matter_furnace", "§c红物质熔炉", "设备", plugin.getItemStackFromKey("red_matter_furnace"));
        addItem("alchemical_chest", "§e炼金术箱子", "设备", plugin.getItemStackFromKey("alchemical_chest"));
        addItem("energy_condenser", "§e能量凝聚器", "设备", plugin.getItemStackFromKey("energy_condenser"));
        addItem("energy_condenser_mk2", "§c能量凝聚器 MK2", "设备", plugin.getItemStackFromKey("energy_condenser_mk2"));
        addItem("energy_collector_mk1", "§e能量收集器 MK1", "设备", plugin.getItemStackFromKey("energy_collector_mk1"));
        addItem("energy_collector_mk2", "§b能量收集器 MK2", "设备", plugin.getItemStackFromKey("energy_collector_mk2"));
        addItem("energy_collector_mk3", "§c能量收集器 MK3", "设备", plugin.getItemStackFromKey("energy_collector_mk3"));

        addItem("alchemical_bag", "§f炼金术袋", "其他", plugin.getItemStackFromKey("alchemical_bag"));
        addItem("transmutation_tablet_book", "§5转换桌", "其他", plugin.getItemStackFromKey("transmutation_tablet_book"));
        addItem("repair_talisman", "§6修复护符", "其他", plugin.getItemStackFromKey("repair_talisman"));
        addItem("divining_rod", "§b探矿杖", "其他", plugin.getItemStackFromKey("divining_rod"));
    }

    private void addItem(String id, String displayName, String category, ItemStack item) {
        if (item != null) {
            allItems.add(new GiveItem(id, displayName, category, item));
        }
    }

    public void open() {
        openPage(0);
    }

    private void openPage(int page) {
        this.currentPage = page;
        
        String title = plugin.getLanguageManager().get("gui.projecte_give.title")
                .replace("%page%", String.valueOf(page + 1))
                .replace("%total%", String.valueOf(getTotalPages()));
        
        inventory = Bukkit.createInventory(null, 54, title);

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            GiveItem giveItem = allItems.get(i);
            ItemStack displayItem = giveItem.itemStack.clone();
            
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                lore.add("");
                lore.add("§7类别: §f" + giveItem.category);
                lore.add("§7ID: §f" + giveItem.id);
                lore.add("");
                lore.add("§e左键点击获取 1 个");
                lore.add("§e右键点击获取 64 个");
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            
            inventory.setItem(i - startIndex, displayItem);
        }

        ItemStack glassPane = createGlassPane();
        for (int i = endIndex - startIndex; i < ITEMS_PER_PAGE; i++) {
            inventory.setItem(i, glassPane);
        }

        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, glassPane);
        }

        if (currentPage > 0) {
            inventory.setItem(PREV_PAGE_SLOT, createControlButton(Material.ARROW, 
                plugin.getLanguageManager().get("gui.projecte_give.prev_page")));
        }

        inventory.setItem(CLOSE_SLOT, createControlButton(Material.BARRIER,
            plugin.getLanguageManager().get("gui.projecte_give.close")));

        if (currentPage < getTotalPages() - 1) {
            inventory.setItem(NEXT_PAGE_SLOT, createControlButton(Material.ARROW,
                plugin.getLanguageManager().get("gui.projecte_give.next_page")));
        }

        player.openInventory(inventory);
    }

    private int getTotalPages() {
        return (int) Math.ceil((double) allItems.size() / ITEMS_PER_PAGE);
    }

    private ItemStack createControlButton(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGlassPane() {
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glassPane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glassPane.setItemMeta(meta);
        }
        return glassPane;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getWhoClicked().equals(player)) return;
        if (event.getInventory() != inventory) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        
        if (slot < 0 || slot >= 54) {
            return;
        }

        if (slot >= 45) {
            if (slot == PREV_PAGE_SLOT && currentPage > 0) {
                openPage(currentPage - 1);
            } else if (slot == NEXT_PAGE_SLOT && currentPage < getTotalPages() - 1) {
                openPage(currentPage + 1);
            } else if (slot == CLOSE_SLOT) {
                player.closeInventory();
            }
            return;
        }

        int itemIndex = currentPage * ITEMS_PER_PAGE + slot;
        if (itemIndex < allItems.size()) {
            GiveItem giveItem = allItems.get(itemIndex);
            ItemStack item = giveItem.itemStack.clone();
            
            if (event.isRightClick()) {
                item.setAmount(64);
            } else {
                item.setAmount(1);
            }
            
            HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item);
            if (remaining.isEmpty()) {
                player.sendMessage("§a已获得: " + giveItem.displayName + " §7x" + item.getAmount());
            } else {
                player.sendMessage("§c背包空间不足！");
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        if (!event.getPlayer().equals(player)) return;
        if (event.getInventory() != inventory) return;

        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
    }
}

