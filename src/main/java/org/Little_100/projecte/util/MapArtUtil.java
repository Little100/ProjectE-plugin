package org.Little_100.projecte.util;

import org.Little_100.projecte.DebugManager;
import org.Little_100.projecte.ProjectE;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.Map;

public class MapArtUtil implements Listener {

    private static ProjectE plugin;
    public static NamespacedKey artFrameKey;
    public static NamespacedKey blockPackKey;
    public static NamespacedKey fuelTypeKey;
    
    // 用于存储中心方块坐标的键
    private final NamespacedKey centerBlockXKey;
    private final NamespacedKey centerBlockYKey;
    private final NamespacedKey centerBlockZKey;

    public MapArtUtil(ProjectE plugin) {
        MapArtUtil.plugin = plugin;
        MapArtUtil.artFrameKey = new NamespacedKey(plugin, "map_art_frame");
        MapArtUtil.blockPackKey = new NamespacedKey(plugin, "block_pack_type");
        MapArtUtil.fuelTypeKey = new NamespacedKey(plugin, "fuel_type");
        this.centerBlockXKey = new NamespacedKey(plugin, "center_block_x");
        this.centerBlockYKey = new NamespacedKey(plugin, "center_block_y");
        this.centerBlockZKey = new NamespacedKey(plugin, "center_block_z");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static ItemStack createImageMap(String imagePath) {
        File imageFile = new File(plugin.getDataFolder(), imagePath);
        if (!imageFile.exists()) {
            plugin.saveResource(imagePath, false);
        }

        if (!imageFile.exists()) {
            DebugManager.log("Image file not found: " + imagePath);
            return null;
        }

        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image != null) {
                MapView mapView = Bukkit.createMap(Bukkit.getWorlds().get(0));
                mapView.getRenderers().forEach(mapView::removeRenderer);
                mapView.addRenderer(new ImageRenderer(image));

                ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
                MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();
                mapMeta.setMapView(mapView);
                mapItem.setItemMeta(mapMeta);
                return mapItem;
            }
        } catch (IOException e) {
            DebugManager.log("Failed to load image: " + imagePath);
            e.printStackTrace();
        }
        return null;
    }

    public static boolean placeArtBlock(Location location, Map<BlockFace, ItemStack> maps, Material centerMaterial, Integer fuelType, Player player) {
        if (maps == null || maps.isEmpty()) {
            return false;
        }

        // 自定义方块限制逻辑
        boolean limitEnabled = plugin.getConfig().getBoolean("CustomBlockPack.limit.enabled", true);
        if (limitEnabled) {
            int maxBlocks = plugin.getConfig().getInt("CustomBlockPack.limit.max", 5);
            int currentBlocks = 0;
            org.bukkit.Chunk chunk = location.getChunk();
            for (Entity entity : chunk.getEntities()) {
                if (entity instanceof ItemFrame && entity.getPersistentDataContainer().has(artFrameKey, PersistentDataType.BYTE)) {
                    ItemFrame frame = (ItemFrame) entity;
                    if (frame.getFacing() == BlockFace.UP) {
                        currentBlocks++;
                    }
                }
            }
            if (currentBlocks >= maxBlocks) {
                if(player != null) {
                    Map<String, String> placeholders = new java.util.HashMap<>();
                    placeholders.put("max", String.valueOf(maxBlocks));
                    player.sendMessage(plugin.getLanguageManager().get("serverside.blockpack.limit_exceeded", placeholders));
                }
                return false; // 取消放置
            }
        }

        Block centerBlock = location.getBlock();
        Map<Location, org.bukkit.block.BlockState> originalStates = new java.util.HashMap<>();

        try {
            // 存储5x5x5区域内所有方块的状态
            for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -2; z <= 2; z++) {
                        Block block = centerBlock.getRelative(x, y, z);
                        if (!block.getType().isAir()) {
                            originalStates.put(block.getLocation(), block.getState());
                        }
                    }
                }
            }

            for (org.bukkit.block.BlockState state : originalStates.values()) {
                state.getBlock().setType(Material.AIR, false);
            }

            centerBlock.setType(centerMaterial, false);

            BlockFace[] faces = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
            for (BlockFace face : faces) {
                Block adjacentBlock = centerBlock.getRelative(face);
                ItemStack mapItem = maps.get(face);
                if (mapItem == null) continue;

                ItemFrame frame = adjacentBlock.getWorld().spawn(adjacentBlock.getLocation(), ItemFrame.class, (spawnedFrame) -> {
                    spawnedFrame.setSilent(true);
                    spawnedFrame.setFacingDirection(face.getOppositeFace());
                    spawnedFrame.setItem(mapItem.clone());
                    spawnedFrame.setVisible(false);
                    spawnedFrame.setInvulnerable(true);
                    spawnedFrame.setFixed(true);
                    spawnedFrame.getPersistentDataContainer().set(artFrameKey, PersistentDataType.BYTE, (byte) 1);
                    spawnedFrame.getPersistentDataContainer().set(new NamespacedKey(plugin, "center_block_x"), PersistentDataType.INTEGER, centerBlock.getX());
                    spawnedFrame.getPersistentDataContainer().set(new NamespacedKey(plugin, "center_block_y"), PersistentDataType.INTEGER, centerBlock.getY());
                    spawnedFrame.getPersistentDataContainer().set(new NamespacedKey(plugin, "center_block_z"), PersistentDataType.INTEGER, centerBlock.getZ());
                    if (fuelType != null) {
                        spawnedFrame.getPersistentDataContainer().set(fuelTypeKey, PersistentDataType.INTEGER, fuelType);
                    }
                });
            }
        } finally {
            for (Map.Entry<Location, org.bukkit.block.BlockState> entry : originalStates.entrySet()) {
                entry.getValue().update(true, false);
            }
        }
        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (event.getEntity() instanceof ItemFrame && event.getEntity().getPersistentDataContainer().has(artFrameKey, PersistentDataType.BYTE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame)) return;
        
        ItemFrame itemFrame = (ItemFrame) event.getRightClicked();
        if (!itemFrame.getPersistentDataContainer().has(artFrameKey, PersistentDataType.BYTE)) return;
        
        Player player = event.getPlayer();
        if (event.getHand() == EquipmentSlot.HAND) {
            player.sendMessage(plugin.getLanguageManager().get("serverside.command.generic.interact_with_map_art_fail"));
        }
        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();
        int brokenX = brokenBlock.getX();
        int brokenY = brokenBlock.getY();
        int brokenZ = brokenBlock.getZ();
        
        plugin.getSchedulerAdapter().runTaskLaterAtLocation(brokenBlock.getLocation(), () -> {
            DebugManager.log("开始清理坐标 " + brokenBlock.getLocation() + " 周围的展示框");
            int removed = 0;

            for (BlockFace face : new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
                Block adjacentBlock = brokenBlock.getRelative(face);
                Location checkLocation = adjacentBlock.getLocation();

                Collection<Entity> framesOnFace = checkLocation.getWorld().getNearbyEntities(checkLocation, 0.5, 0.5, 0.5, entity -> entity instanceof ItemFrame);

                for (Entity entity : framesOnFace) {
                    ItemFrame frame = (ItemFrame) entity;
                    if (!frame.getPersistentDataContainer().has(artFrameKey, PersistentDataType.BYTE)) {
                        continue;
                    }

                    Integer centerX = frame.getPersistentDataContainer().get(centerBlockXKey, PersistentDataType.INTEGER);
                    Integer centerY = frame.getPersistentDataContainer().get(centerBlockYKey, PersistentDataType.INTEGER);
                    Integer centerZ = frame.getPersistentDataContainer().get(centerBlockZKey, PersistentDataType.INTEGER);

                    if (centerX != null && centerY != null && centerZ != null &&
                        centerX == brokenX && centerY == brokenY && centerZ == brokenZ) {
                        
                        frame.remove();
                        removed++;
                    }
                }
            }

            if (removed > 0) {
                DebugManager.log("移除了 " + removed + " 个相关展示框");
            }
        }, 1L);
    }


    private static class ImageRenderer extends MapRenderer {
        private final BufferedImage image;
        private boolean rendered = false;

        public ImageRenderer(BufferedImage image) {
            this.image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = this.image.createGraphics();
            g2d.setComposite(java.awt.AlphaComposite.Clear);
            g2d.fillRect(0, 0, 128, 128);
            g2d.setComposite(java.awt.AlphaComposite.SrcOver);
            g2d.drawImage(image, 0, 0, 128, 128, null);
            g2d.dispose();
        }

        @Override
        public void render(MapView map, MapCanvas canvas, Player player) {
            if (!rendered) {
                canvas.drawImage(0, 0, image);
                rendered = true;
            }
        }
    }
}