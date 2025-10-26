package org.Little_100.projecte.devices;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.List;

public class DeviceListener implements Listener {

    private final ProjectE plugin;

    public DeviceListener(ProjectE plugin) {
        this.plugin = plugin;
    }

    private boolean isCustomDevice(Block block) {
        Collection<Entity> nearbyEntities = block.getWorld()
                .getNearbyEntities(
                        block.getLocation().add(0.5, 0.5, 0.5), 0.5, 1.0, 0.5, entity -> entity instanceof ArmorStand);

        for (Entity entity : nearbyEntities) {
            if (entity.getPersistentDataContainer().has(DarkMatterFurnace.KEY, PersistentDataType.BYTE)
                    || entity.getPersistentDataContainer().has(RedMatterFurnace.KEY, PersistentDataType.BYTE)
                    || entity.getPersistentDataContainer().has(AlchemicalChest.KEY, PersistentDataType.BYTE)
                    || entity.getPersistentDataContainer().has(EnergyCondenser.KEY, PersistentDataType.BYTE)
                    || entity.getPersistentDataContainer().has(EnergyCondenserMK2.KEY, PersistentDataType.BYTE)
                    || entity.getPersistentDataContainer().has(EnergyCollector.KEY_MK1, PersistentDataType.BYTE)
                    || entity.getPersistentDataContainer().has(EnergyCollector.KEY_MK2, PersistentDataType.BYTE)
                    || entity.getPersistentDataContainer().has(EnergyCollector.KEY_MK3, PersistentDataType.BYTE)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        List<Block> blocks = event.getBlocks();
        for (Block block : blocks) {
            if (isCustomDevice(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        List<Block> blocks = event.getBlocks();
        for (Block block : blocks) {
            if (isCustomDevice(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemInHand = event.getItemInHand();
        if (itemInHand.getItemMeta() == null) return;

        ItemMeta meta = itemInHand.getItemMeta();
        FurnaceManager.FurnaceType furnaceType = null;
        Integer collectorType = null;

        if (meta.getPersistentDataContainer().has(DarkMatterFurnace.KEY, PersistentDataType.BYTE)) {
            furnaceType = FurnaceManager.FurnaceType.DARK_MATTER;
        } else if (meta.getPersistentDataContainer().has(RedMatterFurnace.KEY, PersistentDataType.BYTE)) {
            furnaceType = FurnaceManager.FurnaceType.RED_MATTER;
        } else if (meta.getPersistentDataContainer().has(AlchemicalChest.KEY, PersistentDataType.BYTE)) {
        } else if (meta.getPersistentDataContainer().has(EnergyCondenser.KEY, PersistentDataType.BYTE)) {
        } else if (meta.getPersistentDataContainer().has(EnergyCondenserMK2.KEY, PersistentDataType.BYTE)) {
        } else if (meta.getPersistentDataContainer().has(EnergyCollector.KEY_MK1, PersistentDataType.BYTE)) {
            collectorType = EnergyCollector.TYPE_MK1;
        } else if (meta.getPersistentDataContainer().has(EnergyCollector.KEY_MK2, PersistentDataType.BYTE)) {
            collectorType = EnergyCollector.TYPE_MK2;
        } else if (meta.getPersistentDataContainer().has(EnergyCollector.KEY_MK3, PersistentDataType.BYTE)) {
            collectorType = EnergyCollector.TYPE_MK3;
        }

        // 提前检查设备类型
        boolean isAlchemicalChest = meta.getPersistentDataContainer().has(AlchemicalChest.KEY, PersistentDataType.BYTE);
        boolean isEnergyCondenser = meta.getPersistentDataContainer().has(EnergyCondenser.KEY, PersistentDataType.BYTE);
        boolean isEnergyCondenserMK2 = meta.getPersistentDataContainer().has(EnergyCondenserMK2.KEY, PersistentDataType.BYTE);

        if (furnaceType != null
                || collectorType != null
                || isAlchemicalChest
                || isEnergyCondenser
                || isEnergyCondenserMK2) {
            Block block = event.getBlock();
            Player player = event.getPlayer();
            Location location = block.getLocation();
            final FurnaceManager.FurnaceType finalFurnaceType = furnaceType;
            final Integer finalCollectorType = collectorType;
            final boolean finalIsAlchemicalChest = isAlchemicalChest;
            final boolean finalIsEnergyCondenser = isEnergyCondenser;
            final boolean finalIsEnergyCondenserMK2 = isEnergyCondenserMK2;

            final ItemStack deviceItemCopy = itemInHand.clone();
            deviceItemCopy.setAmount(1);

            block.setType(Material.BEACON);

            plugin.getSchedulerAdapter().runTaskAt(location, () -> {
                try {
                    Location armorStandLoc = location.clone().add(0.5, -0.39, 0.5);
                    ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(armorStandLoc, EntityType.ARMOR_STAND);
                    
                    armorStand.setSmall(true);
                    armorStand.setVisible(false);
                    armorStand.setGravity(false);
                    armorStand.setMarker(true);
                    armorStand.setInvulnerable(true);
                    armorStand.setBasePlate(false);
                    armorStand.setArms(false);
                    armorStand.setCanPickupItems(false);
                    armorStand.setPersistent(true);

                    // 设置旋转
                    float yaw = player.getLocation().getYaw();
                    float roundedYaw = Math.round(yaw / 90.0f) * 90.0f;
                    armorStand.setRotation(roundedYaw, 0);

                    // 设置头盔物品
                    armorStand.getEquipment().setHelmet(deviceItemCopy);
                    
                    plugin.getLogger().info("成功生成盔甲架: " + armorStand.getUniqueId() + " 在 " + armorStandLoc);
                    plugin.getLogger().info("  - 头盔物品: " + deviceItemCopy.getType() + " (CMD: " + 
                        (deviceItemCopy.hasItemMeta() && deviceItemCopy.getItemMeta().hasCustomModelData() 
                            ? deviceItemCopy.getItemMeta().getCustomModelData() 
                            : "无") + ")");
                    plugin.getLogger().info("  - 盔甲架设置: Small=" + armorStand.isSmall() + ", Visible=" + armorStand.isVisible() + ", Marker=" + armorStand.isMarker());

                NamespacedKey key = null;
                if (finalFurnaceType == FurnaceManager.FurnaceType.DARK_MATTER) {
                    key = DarkMatterFurnace.KEY;
                } else if (finalFurnaceType == FurnaceManager.FurnaceType.RED_MATTER) {
                    key = RedMatterFurnace.KEY;
                } else if (finalIsAlchemicalChest) {
                    key = AlchemicalChest.KEY;
                } else if (finalIsEnergyCondenser) {
                    key = EnergyCondenser.KEY;
                } else if (finalIsEnergyCondenserMK2) {
                    key = EnergyCondenserMK2.KEY;
                } else if (finalCollectorType != null) {
                    key = EnergyCollector.getKey(finalCollectorType);
                }

                if (key != null) {
                    armorStand.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                }

                if (finalFurnaceType != null) {
                    plugin.getFurnaceManager()
                            .addFurnace(location, player.getUniqueId(), finalFurnaceType, armorStand.getUniqueId());
                } else if (finalIsAlchemicalChest) {
                    plugin.getAlchemicalChestManager().addChest(location, player.getUniqueId());
                } else if (finalCollectorType != null) {
                    plugin.getEnergyCollectorManager().addCollector(location, player.getUniqueId(), finalCollectorType, armorStand.getUniqueId());
                } else if (finalIsEnergyCondenser) {
                    plugin.getCondenserManager()
                            .addCondenser(
                                    location,
                                    player.getUniqueId(),
                                    CondenserManager.CondenserType.ENERGY_CONDENSER,
                                    armorStand.getUniqueId());
                } else if (finalIsEnergyCondenserMK2) {
                    plugin.getCondenserManager()
                            .addCondenser(
                                    location,
                                    player.getUniqueId(),
                                    CondenserManager.CondenserType.ENERGY_CONDENSER_MK2,
                                    armorStand.getUniqueId());
                }
                } catch (Exception e) {
                    plugin.getLogger().severe("生成盔甲架时出错: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.BEACON) return;

        Location blockLoc = block.getLocation();
        
        // 扩大检测范围，确保能找到
        Collection<Entity> nearbyEntities = block.getWorld()
                .getNearbyEntities(
                        blockLoc.clone().add(0.5, 0.0, 0.5), 1.0, 1.5, 1.0, entity -> entity instanceof ArmorStand);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof ArmorStand) {
                ArmorStand armorStand = (ArmorStand) entity;
                ItemStack helmet = armorStand.getEquipment().getHelmet();
                
                if (helmet != null && helmet.getItemMeta() != null) {
                    ItemMeta meta = helmet.getItemMeta();
                    
                    if (meta.getPersistentDataContainer().has(DarkMatterFurnace.KEY, PersistentDataType.BYTE)
                            || meta.getPersistentDataContainer().has(RedMatterFurnace.KEY, PersistentDataType.BYTE)
                            || meta.getPersistentDataContainer().has(AlchemicalChest.KEY, PersistentDataType.BYTE)
                            || meta.getPersistentDataContainer().has(EnergyCondenser.KEY, PersistentDataType.BYTE)
                            || meta.getPersistentDataContainer().has(EnergyCondenserMK2.KEY, PersistentDataType.BYTE)
                            || meta.getPersistentDataContainer().has(EnergyCollector.KEY_MK1, PersistentDataType.BYTE)
                            || meta.getPersistentDataContainer().has(EnergyCollector.KEY_MK2, PersistentDataType.BYTE)
                            || meta.getPersistentDataContainer().has(EnergyCollector.KEY_MK3, PersistentDataType.BYTE)) {

                        // 移除管理器中的数据
                        if (meta.getPersistentDataContainer().has(DarkMatterFurnace.KEY, PersistentDataType.BYTE)
                                || meta.getPersistentDataContainer()
                                        .has(RedMatterFurnace.KEY, PersistentDataType.BYTE)) {
                            plugin.getFurnaceManager().removeFurnace(blockLoc);
                        } else if (meta.getPersistentDataContainer().has(AlchemicalChest.KEY, PersistentDataType.BYTE)) {
                            plugin.getAlchemicalChestManager().removeChest(blockLoc);
                        } else if (meta.getPersistentDataContainer().has(EnergyCollector.KEY_MK1, PersistentDataType.BYTE)
                                || meta.getPersistentDataContainer().has(EnergyCollector.KEY_MK2, PersistentDataType.BYTE)
                                || meta.getPersistentDataContainer().has(EnergyCollector.KEY_MK3, PersistentDataType.BYTE)) {
                            plugin.getEnergyCollectorManager().removeCollector(blockLoc);
                        } else if (meta.getPersistentDataContainer().has(EnergyCondenser.KEY, PersistentDataType.BYTE)
                                || meta.getPersistentDataContainer()
                                        .has(EnergyCondenserMK2.KEY, PersistentDataType.BYTE)) {
                            plugin.getCondenserManager().removeCondenser(blockLoc);
                        }

                        // 移除
                        armorStand.remove();

                        // 掉落物品
                        ItemStack dropItem = helmet.clone();
                        dropItem.setAmount(1);
                        block.getWorld().dropItemNaturally(blockLoc.clone().add(0.5, 0.5, 0.5), dropItem);
                        
                        // 移除方块并取消事件
                        block.setType(Material.AIR);
                        event.setDropItems(false);
                        event.setCancelled(true);
                        
                        plugin.getLogger().info("设备已被破坏并掉落: " + helmet.getType());
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.BEACON) return;

        Collection<Entity> nearbyEntities = clickedBlock
                .getWorld()
                .getNearbyEntities(
                        clickedBlock.getLocation().add(0.5, 0.5, 0.5),
                        0.5,
                        1.0,
                        0.5,
                        entity -> entity instanceof ArmorStand);

        for (Entity entity : nearbyEntities) {
            Player player = event.getPlayer();
            if (entity.getPersistentDataContainer().has(AlchemicalChest.KEY, PersistentDataType.BYTE)) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                plugin.getAlchemicalChestManager().openChestGUI(player, clickedBlock.getLocation());
                return;
            } else if (entity.getPersistentDataContainer().has(EnergyCollector.KEY_MK1, PersistentDataType.BYTE)
                    || entity.getPersistentDataContainer().has(EnergyCollector.KEY_MK2, PersistentDataType.BYTE)
                    || entity.getPersistentDataContainer().has(EnergyCollector.KEY_MK3, PersistentDataType.BYTE)) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
                plugin.getEnergyCollectorManager().openCollectorGUI(player, clickedBlock.getLocation());
                return;
            } else if (entity.getPersistentDataContainer().has(EnergyCondenser.KEY, PersistentDataType.BYTE)) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                plugin.getCondenserManager().openCondenserGUI(player, clickedBlock.getLocation());
                return;
            } else if (entity.getPersistentDataContainer().has(EnergyCondenserMK2.KEY, PersistentDataType.BYTE)) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                plugin.getCondenserManager().openCondenserGUI(player, clickedBlock.getLocation());
                return;
            }
        }
    }
}
