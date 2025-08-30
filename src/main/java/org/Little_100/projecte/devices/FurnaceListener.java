package org.Little_100.projecte.devices;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class FurnaceListener implements Listener {

    private final ProjectE plugin;
    private final FurnaceManager furnaceManager;

    public FurnaceListener(ProjectE plugin, FurnaceManager furnaceManager) {
        this.plugin = plugin;
        this.furnaceManager = furnaceManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || (clickedBlock.getType() != Material.BARRIER && clickedBlock.getType() != Material.BEACON)) {
            return;
        }

        Location loc = clickedBlock.getLocation();
        if (furnaceManager.isFurnace(loc)) {
            Player player = event.getPlayer();
            FurnaceManager.FurnaceState state = furnaceManager.getFurnaceState(loc);
            if (state != null) {
                player.openInventory(state.getInventory());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || !(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (!isCustomFurnaceGui(event.getView().getTitle())) {
            return;
        }

        FurnaceManager.FurnaceState state = null;
        for (FurnaceManager.FurnaceState furnaceState : furnaceManager.getActiveFurnaces().values()) {
            if (furnaceState.getInventory().equals(clickedInventory)) {
                state = furnaceState;
                break;
            }
        }

        if (state == null) {
            return;
        }

        int slot = event.getRawSlot();

        if (slot >= state.getInventory().getSize()) {
            return;
        }

        FurnaceManager.FurnaceType type = state.getType();

        if (furnaceManager.isNonInteractive(type, slot) || furnaceManager.isArrowSlot(type, slot) || furnaceManager.isFuelIndicatorSlot(type, slot)) {
            event.setCancelled(true);
            return;
        }

        if (furnaceManager.isOutputSlot(type, slot)) {
            if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isCustomFurnaceGui(String title) {
        String darkMatterTitle = plugin.getLanguageManager().get("gui.furnace.dark_matter_title");
        String redMatterTitle = plugin.getLanguageManager().get("gui.furnace.red_matter_title");
        return title.equals(darkMatterTitle) || title.equals(redMatterTitle);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();

        if (furnaceManager.isFurnace(loc)) {
            FurnaceManager.FurnaceState state = furnaceManager.getFurnaceState(loc);
            if (state == null) return;

            event.setCancelled(true);
            block.setType(Material.AIR);

            ItemStack drop;
            if (state.getType() == FurnaceManager.FurnaceType.DARK_MATTER) {
                drop = plugin.getDeviceManager().getDarkMatterFurnaceItem();
            } else {
                drop = plugin.getDeviceManager().getRedMatterFurnaceItem();
            }
            loc.getWorld().dropItemNaturally(loc, drop);

            furnaceManager.removeFurnace(loc);
        }
    }
}