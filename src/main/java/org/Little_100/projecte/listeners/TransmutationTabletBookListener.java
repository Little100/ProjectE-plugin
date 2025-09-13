package org.Little_100.projecte.listeners;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.gui.TransmutationGUI;
import org.Little_100.projecte.util.CustomModelDataUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class TransmutationTabletBookListener implements Listener {

    private final NamespacedKey projecteIdKey;
    
    public TransmutationTabletBookListener() {
        this.projecteIdKey = new NamespacedKey(ProjectE.getInstance(), "projecte_id");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (item != null) {
                boolean extremeMode = ProjectE.getInstance().getConfig().getBoolean("debug.extreme_tablet_mode", true);
                
                if (extremeMode && item.getType() == Material.ENCHANTED_BOOK) {
                    ProjectE.getInstance().getLogger().info("极端宽松模式激活：任何附魔书都可以作为转换卓");
                    try {
                        new TransmutationGUI(player).open();
                        event.setCancelled(true);
                        return;
                    } catch (Exception e) {
                        ProjectE.getInstance().getLogger().warning("极端模式下打开GUI失败: " + e.getMessage());
                    }
                }
                
                if (item.getType() == Material.ENCHANTED_BOOK) {
                    ProjectE.getInstance().getLogger().info("玩家右键了附魔书: " + player.getName());
                    
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        ProjectE.getInstance().getLogger().info("物品名称: " + (meta.hasDisplayName() ? meta.getDisplayName() : "无名称"));
                        
                        try {
                            if (meta.hasCustomModelData()) {
                                ProjectE.getInstance().getLogger().info("CustomModelData值: " + meta.getCustomModelData());
                            } else {
                                ProjectE.getInstance().getLogger().info("物品没有CustomModelData");
                            }
                        } catch (Exception e) {
                            ProjectE.getInstance().getLogger().warning("检查CustomModelData时出错: " + e.getMessage());
                        }
                        
                        try {
                            if (meta.getPersistentDataContainer().has(projecteIdKey, org.bukkit.persistence.PersistentDataType.STRING)) {
                                String id = meta.getPersistentDataContainer().get(projecteIdKey, org.bukkit.persistence.PersistentDataType.STRING);
                                ProjectE.getInstance().getLogger().info("PersistentDataContainer ID: " + id);
                                
                                if ("transmutation_tablet_book".equals(id)) {
                                    ProjectE.getInstance().getLogger().info("通过PDC ID直接匹配成功");
                                    new TransmutationGUI(player).open();
                                    event.setCancelled(true);
                                    return;
                                }
                            } else {
                                ProjectE.getInstance().getLogger().info("物品没有ProjectE ID标记");
                            }
                        } catch (Exception e) {
                            ProjectE.getInstance().getLogger().warning("检查PDC时出错: " + e.getMessage());
                        }
                    }
                    
                    try {
                        boolean shouldOpenGui = false;
                        String reason = "未知";
                        
                        if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == 1) {
                            shouldOpenGui = true;
                            reason = "CMD=1";
                        }
                        else if (meta != null && meta.hasDisplayName()) {
                            String displayName = meta.getDisplayName();
                            if (displayName.contains("转换") || displayName.contains("便携") ||
                                displayName.toLowerCase().contains("transmutation")) {
                                shouldOpenGui = true;
                                reason = "名称关键词";
                            }
                        }
                        else if (meta != null && meta.hasLore() && !meta.getLore().isEmpty()) {
                            for (String loreLine : meta.getLore()) {
                                if (loreLine.contains("转换") || loreLine.contains("transmutation")) {
                                    shouldOpenGui = true;
                                    reason = "Lore关键词";
                                    break;
                                }
                            }
                        }
                        else if (isTransmutationTabletBook(item)) {
                            shouldOpenGui = true;
                            reason = "标准方法";
                        }
                        
                        if (shouldOpenGui) {
                            ProjectE.getInstance().getLogger().info("成功识别转换卓 (原因: " + reason + ")，打开GUI");
                            new TransmutationGUI(player).open();
                            event.setCancelled(true);
                            return;
                        }
                        
                        if (ProjectE.getInstance().getConfig().getBoolean("debug.try_all_books", true)) {
                            ProjectE.getInstance().getLogger().info("尝试将任何附魔书作为转换卓");
                            new TransmutationGUI(player).open();
                            event.setCancelled(true);
                        }
                    } catch (Exception e) {
                        ProjectE.getInstance().getLogger().warning("检查转换卓时发生错误: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);

        if (isTransmutationTabletBookSafe(first) || isTransmutationTabletBookSafe(second)) {
            event.setResult(null);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.GRINDSTONE) {
            ItemStack first = event.getInventory().getItem(0);
            ItemStack second = event.getInventory().getItem(1);

            if (isTransmutationTabletBookSafe(first) || isTransmutationTabletBookSafe(second)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isTransmutationTabletBookSafe(ItemStack item) {
        try {
            return isTransmutationTabletBook(item);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTransmutationTabletBook(ItemStack item) {
        ProjectE.getInstance().getLogger().info("检查物品是否为转换卓...");
        
        if (item == null) {
            ProjectE.getInstance().getLogger().info("物品为null");
            return false;
        }
        
        if (item.getType() != Material.ENCHANTED_BOOK) {
            ProjectE.getInstance().getLogger().info("物品不是附魔书");
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            ProjectE.getInstance().getLogger().info("物品没有元数据");
            return false;
        }
        
        boolean ultraLooseMode = ProjectE.getInstance().getConfig().getBoolean("debug.ultra_loose_tablet_detection", true);
        if (ultraLooseMode) {
            ProjectE.getInstance().getLogger().info("超宽松检测模式已启用");
            
            if (meta.hasDisplayName() || (meta.hasLore() && !meta.getLore().isEmpty())) {
                ProjectE.getInstance().getLogger().info("超宽松匹配成功: 附魔书有名称或描述");
                return true;
            }
        }
        
        if (meta.getPersistentDataContainer().has(projecteIdKey, PersistentDataType.STRING)) {
            String id = meta.getPersistentDataContainer().get(projecteIdKey, PersistentDataType.STRING);
            ProjectE.getInstance().getLogger().info("物品有ProjectE ID: " + id);
            if ("transmutation_tablet_book".equals(id)) {
                return true;
            }
        } else {
            ProjectE.getInstance().getLogger().info("物品没有ProjectE ID");
        }
        
        try {
            if (meta.hasCustomModelData()) {
                int cmdValue = meta.getCustomModelData();
                ProjectE.getInstance().getLogger().info("物品有CustomModelData: " + cmdValue);
                if (cmdValue == 1) {
                    return true;
                }
            } else {
                ProjectE.getInstance().getLogger().info("物品没有CustomModelData");
            }
        } catch (Exception e) {
            ProjectE.getInstance().getLogger().info("检查CustomModelData时发生异常: " + e.getMessage());
        }
        
        try {
            int cmdValue = CustomModelDataUtil.getCustomModelDataInt(item);
            ProjectE.getInstance().getLogger().info("CustomModelDataUtil检测到的值: " + cmdValue);
            if (cmdValue == 1) {
                return true;
            }
        } catch (Exception e) {
            ProjectE.getInstance().getLogger().info("使用CustomModelDataUtil时发生异常: " + e.getMessage());
        }
        
        if (meta.hasDisplayName()) {
            String displayName = meta.getDisplayName();
            String expectedName = ProjectE.getInstance().getLanguageManager().get("item.transmutation_tablet_book.name");
            ProjectE.getInstance().getLogger().info("物品名称: " + displayName + ", 期望名称: " + expectedName);
        
            if (displayName.contains(expectedName) || expectedName.contains(displayName) ||
                displayName.toLowerCase().contains("transmutation") ||
                displayName.contains("转换") ||
                displayName.contains("便携") ||
                displayName.contains("卓")) {
                
                ProjectE.getInstance().getLogger().info("名称宽松匹配成功");
                return true;
            }
        } else {
            ProjectE.getInstance().getLogger().info("物品没有名称");
        }

        if (meta.hasLore() && !meta.getLore().isEmpty()) {
            for (String loreLine : meta.getLore()) {
                if (loreLine.contains("转换") || loreLine.contains("transmutation") ||
                    loreLine.contains("便携") || loreLine.contains("tablet")) {
                    ProjectE.getInstance().getLogger().info("通过Lore关键词匹配成功");
                    return true;
                }
            }
        }
        
        ProjectE.getInstance().getLogger().info("物品不是转换卓");
        return false;
    }
}