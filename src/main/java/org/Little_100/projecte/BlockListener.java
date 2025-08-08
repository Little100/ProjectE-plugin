package org.Little_100.projecte;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.PETRIFIED_OAK_SLAB) {
            return;
        }

        Block block = event.getBlockPlaced();
        if (block.getBlockData() instanceof Slab) {
            Slab slab = (Slab) block.getBlockData();
            if (slab.getType() != Slab.Type.BOTTOM) {
                event.setCancelled(true);
                event.getPlayer()
                        .sendMessage(ChatColor.RED + "The Transmutation Table can only be placed as a bottom slab.");
                return;
            }
        }

        Block blockAgainst = event.getBlockAgainst();
        if (blockAgainst.getType() == Material.PETRIFIED_OAK_SLAB) {
            if (blockAgainst.getBlockData() instanceof Slab) {
                Slab againstSlab = (Slab) blockAgainst.getBlockData();
                if (againstSlab.getType() == Slab.Type.BOTTOM) {
                    event.setCancelled(true);
                    event.getPlayer()
                            .sendMessage(ChatColor.RED + "You cannot place a Transmutation Table on top of another.");
                }
            }
        }
    }
}