package org.Little_100.projecte.devices;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AlchemicalChestManager {

    private final ProjectE plugin;
    private final Map<String, UUID> chestOwners = new HashMap<>();
    private final Map<UUID, String> openChests = new HashMap<>();

    public AlchemicalChestManager(ProjectE plugin) {
        this.plugin = plugin;
    }

    public void addChest(Location location, UUID ownerUUID) {
        String locationKey = getLocationKey(location);
        chestOwners.put(locationKey, ownerUUID);
    }

    public void removeChest(Location location) {
        String locationKey = getLocationKey(location);
        chestOwners.remove(locationKey);
        plugin.getDatabaseManager().deleteChestInventory(locationKey);
    }

    public void openChestGUI(Player player, Location location) {
        String locationKey = getLocationKey(location);
        openChests.put(player.getUniqueId(), locationKey);

        ItemStack[] contents = plugin.getDatabaseManager().loadChestInventory(locationKey);

        AlchemicalChestGUI gui = new AlchemicalChestGUI(plugin, player, location, contents);
        gui.open();
    }

    public void closeChestGUI(Player player, ItemStack[] contents) {
        UUID playerUUID = player.getUniqueId();
        String locationKey = openChests.remove(playerUUID);

        if (locationKey != null) {
            UUID ownerUUID = chestOwners.get(locationKey);
            if (ownerUUID == null) {
                ownerUUID = plugin.getDatabaseManager().getChestOwner(locationKey);
                if (ownerUUID == null) {
                    ownerUUID = playerUUID;
                }
            }
            plugin.getDatabaseManager().saveChestInventory(locationKey, ownerUUID, contents);
        }
    }

    public boolean isChestOpen(Player player) {
        return openChests.containsKey(player.getUniqueId());
    }

    public String getOpenChestLocation(Player player) {
        return openChests.get(player.getUniqueId());
    }

    public static String getLocationKey(Location location) {
        return location.getWorld().getName() + "," +
                location.getBlockX() + "," +
                location.getBlockY() + "," +
                location.getBlockZ();
    }

    public void loadChestData() {
    }

    public void saveAllChests() {
    }
}
