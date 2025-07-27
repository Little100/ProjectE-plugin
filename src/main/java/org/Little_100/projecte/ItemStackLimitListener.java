package org.Little_100.projecte;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ItemStackLimitListener implements Listener {

    private final ProjectE plugin;

    public ItemStackLimitListener(ProjectE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        ItemStack cursorItem = event.getCursor();
        ItemStack currentItem = event.getCurrentItem();
        
        // 获取POPPED_CHORUS_FRUIT的Material对象
        Material poppedChorusFruit = plugin.getVersionAdapter().getMaterial("POPPED_CHORUS_FRUIT");
        
        // 检查是否为null（旧版本兼容）
        if (poppedChorusFruit == null) {
            poppedChorusFruit = Material.NETHER_STAR;
        }
        
        // 检查玩家是否试图堆叠贤者之石或任何POPPED_CHORUS_FRUIT物品
        boolean isCursorChorusFruit = cursorItem != null && cursorItem.getType() == poppedChorusFruit;
        boolean isCurrentChorusFruit = currentItem != null && currentItem.getType() == poppedChorusFruit;
        
        // 如果两个都是POPPED_CHORUS_FRUIT，阻止堆叠
        if (isCursorChorusFruit && isCurrentChorusFruit) {
            // 允许玩家移动单个物品，但阻止将它们堆叠在一起
            if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR || event.getClick() == ClickType.RIGHT) {
                event.setCancelled(true);
            }
        }
        
        // 防止POPPED_CHORUS_FRUIT物品堆叠数量超过1
        if ((isCursorChorusFruit && cursorItem.getAmount() > 1) ||
            (isCurrentChorusFruit && currentItem.getAmount() > 1)) {
            event.setCancelled(true);
        }
    }
}