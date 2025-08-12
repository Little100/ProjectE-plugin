package org.Little_100.projecte.BlockPackManager;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.FuelManager;
import org.Little_100.projecte.util.MapArtUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import org.bukkit.entity.Entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Fuel implements Listener {

    private final ProjectE plugin;
    private final FuelManager fuelManager;
    private final Map<Integer, Map<BlockFace, ItemStack>> fuelBlockMaps = new HashMap<>();

    public Fuel(ProjectE plugin) {
        this.plugin = plugin;
        this.fuelManager = plugin.getFuelManager();
        loadFuelBlockMaps();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void loadFuelBlockMaps() {
        ItemStack map1 = MapArtUtil.createImageMap("blockpack/Fuelblock/fuels_1.png");
        ItemStack map2 = MapArtUtil.createImageMap("blockpack/Fuelblock/fuels_2.png");
        ItemStack map3 = MapArtUtil.createImageMap("blockpack/Fuelblock/fuels_3.png");

        if (map1 == null || map2 == null || map3 == null) {
            plugin.getLogger().severe("Failed to load one or more fuel maps. Aborting.");
            return;
        }

        Map<BlockFace, ItemStack> alchemicalCoalMaps = new HashMap<>();
        for (BlockFace face : BlockFace.values()) {
            if (face.isCartesian()) {
                alchemicalCoalMaps.put(face, map1);
            }
        }
        fuelBlockMaps.put(1, alchemicalCoalMaps);

        Map<BlockFace, ItemStack> mobiusFuelMaps = new HashMap<>();
        for (BlockFace face : BlockFace.values()) {
            if (face.isCartesian()) {
                mobiusFuelMaps.put(face, map2);
            }
        }
        fuelBlockMaps.put(2, mobiusFuelMaps);

        Map<BlockFace, ItemStack> aeternalisFuelMaps = new HashMap<>();
        for (BlockFace face : BlockFace.values()) {
            if (face.isCartesian()) {
                aeternalisFuelMaps.put(face, map3);
            }
        }
        fuelBlockMaps.put(3, aeternalisFuelMaps);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        int fuelType = 0;
        if (fuelManager.isAlchemicalCoalBlock(itemInHand)) {
            fuelType = 1;
        } else if (fuelManager.isMobiusFuelBlock(itemInHand)) {
            fuelType = 2;
        } else if (fuelManager.isAeternalisFuelBlock(itemInHand)) {
            fuelType = 3;
        }

        if (fuelType > 0) {
            event.setCancelled(true);
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null) {
                Location location = clickedBlock.getRelative(event.getBlockFace()).getLocation();
                Map<BlockFace, ItemStack> maps = fuelBlockMaps.get(fuelType);
                Material centerMaterial;
                if (fuelType <= 2) {
                    centerMaterial = Material.REDSTONE_BLOCK;
                } else {
                    centerMaterial = Material.DIORITE;
                }
                boolean placed = MapArtUtil.placeArtBlock(location, maps, centerMaterial, fuelType, player);
                if (placed && player.getGameMode() != GameMode.CREATIVE) {
                    itemInHand.setAmount(itemInHand.getAmount() - 1);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();
        Location location = brokenBlock.getLocation();

        Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, 1.5, 1.5, 1.5, entity -> entity instanceof ItemFrame);
        for (Entity entity : nearbyEntities) {
            ItemFrame frame = (ItemFrame) entity;
            if (frame.getPersistentDataContainer().has(MapArtUtil.artFrameKey, PersistentDataType.BYTE)) {
                Integer centerX = frame.getPersistentDataContainer().get(new org.bukkit.NamespacedKey(plugin, "center_block_x"), PersistentDataType.INTEGER);
                Integer centerY = frame.getPersistentDataContainer().get(new org.bukkit.NamespacedKey(plugin, "center_block_y"), PersistentDataType.INTEGER);
                Integer centerZ = frame.getPersistentDataContainer().get(new org.bukkit.NamespacedKey(plugin, "center_block_z"), PersistentDataType.INTEGER);

                if (centerX != null && centerX.equals(brokenBlock.getX()) &&
                    centerY != null && centerY.equals(brokenBlock.getY()) &&
                    centerZ != null && centerZ.equals(brokenBlock.getZ())) {

                    event.setDropItems(false);
                    
                    Integer fuelType = frame.getPersistentDataContainer().get(MapArtUtil.fuelTypeKey, PersistentDataType.INTEGER);

                    if (fuelType != null && fuelType > 0) {
                        ItemStack drop;
                        switch (fuelType) {
                            case 1:
                                drop = fuelManager.getAlchemicalCoalBlock();
                                break;
                            case 2:
                                drop = fuelManager.getMobiusFuelBlock();
                                break;
                            case 3:
                                drop = fuelManager.getAeternalisFuelBlock();
                                break;
                            default:
                                return;
                        }
                        brokenBlock.getWorld().dropItemNaturally(location, drop);
                    }
                    break;
                }
            }
        }
    }
}