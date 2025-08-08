package org.Little_100.projecte.Tools;

import org.Little_100.projecte.ProjectE;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DiviningRodListener implements Listener {

    private final ProjectE plugin;
    private final Divining_Rod diviningRod;
    private final Map<UUID, Integer> playerModes = new HashMap<>();

    public DiviningRodListener(ProjectE plugin) {
        this.plugin = plugin;
        this.diviningRod = plugin.getDiviningRod();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_BLOCK && !player.isSneaking()) {
            if (diviningRod.isLowDiviningRod(item) || diviningRod.isMediumDiviningRod(item) || diviningRod.isHighDiviningRod(item)) {
                event.setCancelled(true);
                scanBlocks(player, item, event.getClickedBlock().getLocation());
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking()) {
            // Since we can't detect key presses directly, we'll use a placeholder for 'F' key.
            // The actual implementation would require a custom resource pack and client-side mods.
            // For now, we'll cycle modes on sneak.
            ItemStack item = player.getInventory().getItemInMainHand();
            if (diviningRod.isMediumDiviningRod(item) || diviningRod.isHighDiviningRod(item)) {
                cycleScanMode(player, item);
            }
        }
    }

    private void cycleScanMode(Player player, ItemStack item) {
        UUID playerUUID = player.getUniqueId();
        int currentMode = playerModes.getOrDefault(playerUUID, 0);
        int maxModes = 1;
        if (diviningRod.isMediumDiviningRod(item)) {
            maxModes = 2;
        } else if (diviningRod.isHighDiviningRod(item)) {
            maxModes = 3;
        }

        int nextMode = (currentMode + 1) % maxModes;
        playerModes.put(playerUUID, nextMode);

        String modeName = getModeName(nextMode);
        player.sendMessage(ChatColor.GREEN + "Scan mode changed to: " + modeName);
    }

    private String getModeName(int mode) {
        switch (mode) {
            case 0:
                return "3x3x3";
            case 1:
                return "16x3x3";
            case 2:
                return "64x3x3";
            default:
                return "Unknown";
        }
    }

    private void scanBlocks(Player player, ItemStack item, Location center) {
        int mode = 0;
        if (diviningRod.isLowDiviningRod(item)) {
            mode = 0;
        } else {
            mode = playerModes.getOrDefault(player.getUniqueId(), 0);
        }

        int[] dimensions = getScanDimensions(mode);
        int halfX = dimensions[0] / 2;
        int halfY = dimensions[1] / 2;
        int halfZ = dimensions[2] / 2;

        long totalEmc = 0;
        int blockCount = 0;

        for (int x = -halfX; x <= halfX; x++) {
            for (int y = -halfY; y <= halfY; y++) {
                for (int z = -halfZ; z <= halfZ; z++) {
                    Block block = center.clone().add(x, y, z).getBlock();
                    if (block.getType() != Material.AIR) {
                        ItemStack blockStack = new ItemStack(block.getType());
                        String itemKey = plugin.getEmcManager().getItemKey(blockStack);
                        long emc = plugin.getEmcManager().getEmc(itemKey);
                        if (emc > 0) {
                            totalEmc += emc;
                            blockCount++;
                        }
                    }
                }
            }
        }

        if (blockCount > 0) {
            long averageEmc = totalEmc / blockCount;
            player.sendMessage(ChatColor.AQUA + "Scan Results:");
            player.sendMessage(ChatColor.GOLD + "Total Blocks: " + blockCount);
            player.sendMessage(ChatColor.GOLD + "Average EMC: " + averageEmc);
        } else {
            player.sendMessage(ChatColor.RED + "No blocks with EMC found in the area.");
        }
    }

    private int[] getScanDimensions(int mode) {
        switch (mode) {
            case 0: // 3x3x3
                return new int[]{3, 3, 3};
            case 1: // 16x3x3
                return new int[]{16, 3, 3};
            case 2: // 64x3x3
                return new int[]{64, 3, 3};
            default:
                return new int[]{3, 3, 3};
        }
    }
}