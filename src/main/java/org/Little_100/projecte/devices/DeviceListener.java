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
                    || entity.getPersistentDataContainer().has(EnergyCondenserMK2.KEY, PersistentDataType.BYTE)) {
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

        if (meta.getPersistentDataContainer().has(DarkMatterFurnace.KEY, PersistentDataType.BYTE)) {
            furnaceType = FurnaceManager.FurnaceType.DARK_MATTER;
        } else if (meta.getPersistentDataContainer().has(RedMatterFurnace.KEY, PersistentDataType.BYTE)) {
            furnaceType = FurnaceManager.FurnaceType.RED_MATTER;
        } else if (meta.getPersistentDataContainer().has(AlchemicalChest.KEY, PersistentDataType.BYTE)) {
        } else if (meta.getPersistentDataContainer().has(EnergyCondenser.KEY, PersistentDataType.BYTE)) {
        } else if (meta.getPersistentDataContainer().has(EnergyCondenserMK2.KEY, PersistentDataType.BYTE)) {
        }

        if (furnaceType != null
                || meta.getPersistentDataContainer().has(AlchemicalChest.KEY, PersistentDataType.BYTE)
                || meta.getPersistentDataContainer().has(EnergyCondenser.KEY, PersistentDataType.BYTE)
                || meta.getPersistentDataContainer().has(EnergyCondenserMK2.KEY, PersistentDataType.BYTE)) {
            Block block = event.getBlock();
            Player player = event.getPlayer();
            Location location = block.getLocation();
            final FurnaceManager.FurnaceType finalFurnaceType = furnaceType;

            block.setType(Material.BEACON);

            plugin.getSchedulerAdapter().runTaskAt(location, () -> {
                ArmorStand armorStand = (ArmorStand)
                        location.getWorld().spawnEntity(location.clone().add(0.5, -0.39, 0.5), EntityType.ARMOR_STAND);
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                armorStand.setSmall(true);
                armorStand.setMarker(true);

                float yaw = player.getLocation().getYaw();
                float roundedYaw = Math.round(yaw / 90.0f) * 90.0f;
                armorStand.setRotation(roundedYaw, 0);

                ItemStack deviceItem = itemInHand.clone();
                deviceItem.setAmount(1);
                armorStand.getEquipment().setHelmet(deviceItem);

                NamespacedKey key = null;
                if (finalFurnaceType == FurnaceManager.FurnaceType.DARK_MATTER) {
                    key = DarkMatterFurnace.KEY;
                } else if (finalFurnaceType == FurnaceManager.FurnaceType.RED_MATTER) {
                    key = RedMatterFurnace.KEY;
                } else if (meta.getPersistentDataContainer().has(AlchemicalChest.KEY, PersistentDataType.BYTE)) {
                    key = AlchemicalChest.KEY;
                } else if (meta.getPersistentDataContainer().has(EnergyCondenser.KEY, PersistentDataType.BYTE)) {
                    key = EnergyCondenser.KEY;
                } else if (meta.getPersistentDataContainer().has(EnergyCondenserMK2.KEY, PersistentDataType.BYTE)) {
                    key = EnergyCondenserMK2.KEY;
                }

                if (key != null) {
                    armorStand.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                }

                if (finalFurnaceType != null) {
                    plugin.getFurnaceManager()
                            .addFurnace(location, player.getUniqueId(), finalFurnaceType, armorStand.getUniqueId());
                } else if (meta.getPersistentDataContainer().has(EnergyCondenser.KEY, PersistentDataType.BYTE)) {
                    plugin.getCondenserManager()
                            .addCondenser(
                                    location,
                                    player.getUniqueId(),
                                    CondenserManager.CondenserType.ENERGY_CONDENSER,
                                    armorStand.getUniqueId());
                } else if (meta.getPersistentDataContainer().has(EnergyCondenserMK2.KEY, PersistentDataType.BYTE)) {
                    plugin.getCondenserManager()
                            .addCondenser(
                                    location,
                                    player.getUniqueId(),
                                    CondenserManager.CondenserType.ENERGY_CONDENSER_MK2,
                                    armorStand.getUniqueId());
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.BEACON) return;

        Collection<Entity> nearbyEntities = block.getWorld()
                .getNearbyEntities(
                        block.getLocation().add(0.5, 0.5, 0.5), 0.5, 1.0, 0.5, entity -> entity instanceof ArmorStand);

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
                            || meta.getPersistentDataContainer().has(EnergyCondenserMK2.KEY, PersistentDataType.BYTE)) {

                        if (meta.getPersistentDataContainer().has(DarkMatterFurnace.KEY, PersistentDataType.BYTE)
                                || meta.getPersistentDataContainer()
                                        .has(RedMatterFurnace.KEY, PersistentDataType.BYTE)) {
                            plugin.getFurnaceManager().removeFurnace(block.getLocation());
                        } else if (meta.getPersistentDataContainer().has(EnergyCondenser.KEY, PersistentDataType.BYTE)
                                || meta.getPersistentDataContainer()
                                        .has(EnergyCondenserMK2.KEY, PersistentDataType.BYTE)) {
                            plugin.getCondenserManager().removeCondenser(block.getLocation());
                        }

                        armorStand.remove();

                        block.getWorld().dropItemNaturally(block.getLocation(), helmet);
                        block.setType(Material.AIR);
                        event.setCancelled(true);
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
                Inventory chestInventory = plugin.getServer().createInventory(null, 54, "Alchemical Chest");
                player.openInventory(chestInventory);
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
