package org.Little_100.projecte.util;

import org.Little_100.projecte.managers.BlockDataManager;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.compatibility.SchedulerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;

public class CustomBlockArtUtil implements Listener {
    private static double yOffset = -0.39;
    private static ProjectE plugin;
    private static SchedulerAdapter scheduler;
    private static BlockDataManager blockDataManager;
    private final NamespacedKey blockDataKey;

    public static double getYOffset() {
        return yOffset;
    }

    public static void addToYOffset(double value) {
        yOffset += value;
    }

    public CustomBlockArtUtil(ProjectE plugin, SchedulerAdapter scheduler) {
        CustomBlockArtUtil.plugin = plugin;
        CustomBlockArtUtil.scheduler = scheduler;
        CustomBlockArtUtil.blockDataManager = plugin.getBlockDataManager();
        this.blockDataKey = new NamespacedKey(plugin, "custom_block_data");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static ArmorStand placeArtBlock(Location location, ItemStack item) {
        return placeArtBlock(location, item, Material.COBBLESTONE_STAIRS, Bisected.Half.TOP);
    }

    public static ArmorStand placeArtBlock(Location location, ItemStack item, Material baseBlock) {
        return placeArtBlock(location, item, baseBlock, Bisected.Half.TOP);
    }

    public static ArmorStand placeArtBlock(Location location, ItemStack item, Material baseBlock, Bisected.Half half) {
        Location adjustedLocation = location.clone().add(0.5, yOffset, 0.5);
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(adjustedLocation, EntityType.ARMOR_STAND);

        armorStand.setVisible(false);
        armorStand.setInvulnerable(true);
        armorStand.setGravity(false);
        armorStand.setBasePlate(false);
        armorStand.setArms(false);
        armorStand.setSmall(true);
        armorStand.setMarker(true);

        armorStand.getEquipment().setHelmet(item.clone());

        PersistentDataContainer pdc = armorStand.getPersistentDataContainer();
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasCustomModelData()) {
                pdc.set(Constants.MODEL_KEY, PersistentDataType.INTEGER, meta.getCustomModelData());
            }

            if (meta.hasDisplayName()) {
                pdc.set(Constants.NAME_KEY, PersistentDataType.STRING, meta.getDisplayName());
            }
        }
        pdc.set(Constants.MATERIAL_KEY, PersistentDataType.STRING, item.getType().toString());

        return armorStand;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemInHand = event.getItemInHand();
        String blockId = blockDataManager.getBlockIdByItem(itemInHand);

        if (blockId != null) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            Block block = event.getBlock();
            String command = blockDataManager.getPlaceCommand(blockId);

            if (command != null) {
                String processedCommand = command
                        .replace("{x}", String.valueOf(block.getX()))
                        .replace("{y}", String.valueOf(block.getY()))
                        .replace("{z}", String.valueOf(block.getZ()))
                        .replace("{world}", block.getWorld().getName())
                        .replace("{player}", player.getName());
                Bukkit.dispatchCommand(player, processedCommand);
            } else {
                ItemStack customBlockItem = blockDataManager.getBlock(blockId);
                Material baseMaterial = blockDataManager.getBaseBlock(blockId);

                if (baseMaterial == null) {
                    baseMaterial = Material.COBBLESTONE_STAIRS;
                }

                final Material finalBaseMaterial = baseMaterial;
                scheduler.runTaskAt(block.getLocation(), () -> {
                    block.setType(finalBaseMaterial, false);
                    if (block.getBlockData() instanceof Stairs) {
                        Stairs stairsData = (Stairs) block.getBlockData();
                        stairsData.setHalf(Bisected.Half.TOP);
                        block.setBlockData(stairsData, true);
                    }
                });

                placeArtBlock(
                        block.getLocation(),
                        customBlockItem,
                        finalBaseMaterial,
                        Bisected.Half.TOP
                );
            }

            if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getBlockData() instanceof Stairs) {
            Collection<Entity> nearbyEntities = block.getWorld().getNearbyEntities(
                    block.getLocation().add(0.5, 0, 0.5), 0.5, 0.5, 0.5,
                    entity -> entity.getType() == EntityType.ARMOR_STAND);

            for (Entity entity : nearbyEntities) {
                if (entity instanceof ArmorStand) {
                    ArmorStand stand = (ArmorStand) entity;
                    PersistentDataContainer pdc = stand.getPersistentDataContainer();
                    if (pdc.has(Constants.MATERIAL_KEY, PersistentDataType.STRING)) {
                        event.setCancelled(true);
                        ItemStack dropItem = createDropItem(stand);
                        stand.remove();
                        String originalBlockStr = pdc.getOrDefault(
                                new NamespacedKey(plugin, "original_block"),
                                PersistentDataType.STRING, "AIR");
                        Material originalMaterial = Material.getMaterial(originalBlockStr);
                        block.setType(originalMaterial != null ? originalMaterial : Material.AIR);

                        ItemStack helmet = stand.getEquipment().getHelmet();
                        ItemStack fullDrop = blockDataManager.getBlockByItem(helmet);
                        if (fullDrop != null) {
                            block.getWorld().dropItemNaturally(block.getLocation(), fullDrop.clone());
                        } else if (dropItem != null) {
                            block.getWorld().dropItemNaturally(block.getLocation(), dropItem);
                        }
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onArmorStandInteract(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) event.getRightClicked();
            PersistentDataContainer pdc = stand.getPersistentDataContainer();
            if (pdc.has(Constants.MATERIAL_KEY, PersistentDataType.STRING)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onArmorStandDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) event.getEntity();
            PersistentDataContainer pdc = stand.getPersistentDataContainer();
            if (pdc.has(Constants.MATERIAL_KEY, PersistentDataType.STRING)) {
                event.setCancelled(true);
            }
        }
    }

    private ItemStack createDropItem(ArmorStand stand) {
        PersistentDataContainer pdc = stand.getPersistentDataContainer();
        String materialStr = pdc.get(Constants.MATERIAL_KEY, PersistentDataType.STRING);
        if (materialStr == null) return null;
        Material material = Material.getMaterial(materialStr);
        if (material == null) return null;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (pdc.has(Constants.MODEL_KEY, PersistentDataType.INTEGER)) {
            int cmd = pdc.get(Constants.MODEL_KEY, PersistentDataType.INTEGER);
            meta.setCustomModelData(cmd);
        }

        if (pdc.has(Constants.NAME_KEY, PersistentDataType.STRING)) {
            String name = pdc.get(Constants.NAME_KEY, PersistentDataType.STRING);
            meta.setDisplayName(name);
        }

        item.setItemMeta(meta);
        return item;
    }
}