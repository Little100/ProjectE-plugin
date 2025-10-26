package org.Little_100.projecte.devices;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EnergyCollectorManager {

    private final ProjectE plugin;
    private final Map<String, CollectorData> collectors = new ConcurrentHashMap<>();
    private final Map<UUID, String> openCollectors = new HashMap<>();

    public EnergyCollectorManager(ProjectE plugin) {
        this.plugin = plugin;
        startEmcGeneration();
    }

    public static class CollectorData {
        public UUID ownerUUID;
        public UUID armorStandUUID;
        public int type;
        public long storedEmc;
        public ItemStack[] inventory;

        public CollectorData(UUID ownerUUID, UUID armorStandUUID, int type) {
            this.ownerUUID = ownerUUID;
            this.armorStandUUID = armorStandUUID;
            this.type = type;
            this.storedEmc = 0;
            this.inventory = new ItemStack[EnergyCollector.getInventorySize(type)];
        }
    }

    public void addCollector(Location location, UUID ownerUUID, int type, UUID armorStandUUID) {
        String locationKey = getLocationKey(location);
        CollectorData data = new CollectorData(ownerUUID, armorStandUUID, type);

        long savedEmc = plugin.getDatabaseManager().loadCollectorEmc(locationKey);
        ItemStack[] savedInventory = plugin.getDatabaseManager().loadCollectorInventory(
                locationKey, EnergyCollector.getInventorySize(type));

        data.storedEmc = savedEmc;
        data.inventory = savedInventory;

        collectors.put(locationKey, data);
        
        // 立即保存初始数据到数据库
        plugin.getDatabaseManager().saveCollectorData(
                locationKey, ownerUUID, type, data.storedEmc, data.inventory);
    }

    public void removeCollector(Location location) {
        String locationKey = getLocationKey(location);
        CollectorData data = collectors.remove(locationKey);

        if (data != null) {
            plugin.getDatabaseManager().saveCollectorData(
                    locationKey, data.ownerUUID, data.type, data.storedEmc, data.inventory);
        }

        plugin.getDatabaseManager().deleteCollectorData(locationKey);
    }

    public void openCollectorGUI(Player player, Location location) {
        String locationKey = getLocationKey(location);
        CollectorData data = collectors.get(locationKey);

        if (data == null) {
            int type = plugin.getDatabaseManager().loadCollectorType(locationKey);
            UUID owner = plugin.getDatabaseManager().getCollectorOwner(locationKey);
            if (owner != null) {
                data = new CollectorData(owner, null, type);
                data.storedEmc = plugin.getDatabaseManager().loadCollectorEmc(locationKey);
                data.inventory = plugin.getDatabaseManager().loadCollectorInventory(
                        locationKey, EnergyCollector.getInventorySize(type));
                collectors.put(locationKey, data);
            } else {
                return;
            }
        }

        openCollectors.put(player.getUniqueId(), locationKey);

        EnergyCollectorGUI gui = new EnergyCollectorGUI(plugin, player, location, data);
        gui.open();
    }

    public void closeCollectorGUI(Player player, long storedEmc, ItemStack[] inventory) {
        UUID playerUUID = player.getUniqueId();
        String locationKey = openCollectors.remove(playerUUID);

        if (locationKey != null) {
            CollectorData data = collectors.get(locationKey);
            if (data != null) {
                data.storedEmc = storedEmc;
                data.inventory = inventory;

                plugin.getDatabaseManager().saveCollectorData(
                        locationKey, data.ownerUUID, data.type, storedEmc, inventory);
            }
        }
    }

    public boolean isCollectorOpen(Player player) {
        return openCollectors.containsKey(player.getUniqueId());
    }

    private void startEmcGeneration() {
        plugin.getSchedulerAdapter().runTimer(() -> {
            for (Map.Entry<String, CollectorData> entry : collectors.entrySet()) {
                String locationKey = entry.getKey();
                CollectorData data = entry.getValue();

                try {
                    Location location = parseLocationKey(locationKey);
                    if (location == null || location.getWorld() == null) {
                        continue;
                    }

                    if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                        continue;
                    }
                    plugin.getSchedulerAdapter().runTaskAt(location, () -> {
                        try {
                            Block aboveBlock = location.getBlock().getRelative(0, 1, 0);
                            int lightLevel = aboveBlock.getLightLevel();

                            long baseEmcRate = EnergyCollector.getEmcRate(data.type);
                            double lightMultiplier = Math.max(0.1, lightLevel / 15.0);
                            long emcGenerated = (long) (baseEmcRate * lightMultiplier);
                            data.storedEmc += emcGenerated;

                            transferEmcToAdjacentCondensers(location, data);

                            processItemUpgrades(data);
                        } catch (Exception e) {
                            plugin.getLogger().warning(
                                    "Error generating EMC for collector at " + locationKey + ": " + e.getMessage());
                        }
                    });

                } catch (Exception e) {
                    plugin.getLogger().warning("Error processing collector at " + locationKey + ": " + e.getMessage());
                }
            }

            if (System.currentTimeMillis() % 60000 < 1000) {
                saveAllCollectors();
            }
        }, 20L, 20L);
    }

    private void processItemUpgrades(CollectorData data) {
    }

    private void transferEmcToAdjacentCondensers(Location location, CollectorData data) {
        if (data.storedEmc <= 0) {
            return;
        }

        Block block = location.getBlock();

        BlockFace[] faces = {
                BlockFace.NORTH, BlockFace.SOUTH,
                BlockFace.EAST, BlockFace.WEST,
                BlockFace.UP, BlockFace.DOWN
        };

        for (BlockFace face : faces) {
            Block adjacent = block.getRelative(face);

            if (adjacent.getType() == Material.BEACON) {
                Location adjacentLocation = adjacent.getLocation();

                CondenserManager.CondenserState condenserState = plugin.getCondenserManager()
                        .getCondenserState(adjacentLocation);

                if (condenserState != null) {
                    condenserState.addEmc(data.storedEmc);
                    data.storedEmc = 0;
                    return;
                }
            }
        }
    }

    private void saveAllCollectors() {
        for (Map.Entry<String, CollectorData> entry : collectors.entrySet()) {
            String locationKey = entry.getKey();
            CollectorData data = entry.getValue();

            plugin.getDatabaseManager().saveCollectorData(
                    locationKey, data.ownerUUID, data.type, data.storedEmc, data.inventory);
        }
    }

    public void shutdown() {
        saveAllCollectors();
        plugin.getLogger()
                .info("Energy Collector Manager shutdown complete. Saved " + collectors.size() + " collectors.");
    }

    public static String getLocationKey(Location location) {
        return location.getWorld().getName() + "," +
                location.getBlockX() + "," +
                location.getBlockY() + "," +
                location.getBlockZ();
    }

    private Location parseLocationKey(String locationKey) {
        try {
            String[] parts = locationKey.split(",");
            if (parts.length != 4) {
                return null;
            }
            return new Location(
                    plugin.getServer().getWorld(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]));
        } catch (Exception e) {
            return null;
        }
    }
}
