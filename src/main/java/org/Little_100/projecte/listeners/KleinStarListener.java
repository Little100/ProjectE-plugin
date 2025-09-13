package org.Little_100.projecte.listeners;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.tools.kleinstar.KleinStarManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class KleinStarListener implements Listener {

    private final KleinStarManager kleinStarManager;

    public KleinStarListener(ProjectE plugin) {
        this.kleinStarManager = plugin.getKleinStarManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && kleinStarManager.isKleinStar(item)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (kleinStarManager.isKleinStar(item)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (kleinStarManager.isKleinStar(item)) {
            event.setCancelled(true);
        }
    }
}