package org.Little_100.projecte.listeners;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.armor.ArmorManager;
import org.Little_100.projecte.armor.GemHelmet;
import org.Little_100.projecte.gui.GemHelmetGUI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class ArmorListener implements Listener {

    private final Map<Location, Material> temporaryBlocks = new HashMap<>();
    private final Map<UUID, Boolean> autoStepEnabled = new HashMap<>();
    private final NamespacedKey gravitySlamKey;
    private final ProjectE plugin;
    private final ArmorManager armorManager;
    private final NamespacedKey internalVelocityKey;

    private static final Set<EntityDamageEvent.DamageCause> UNPROTECTED_CAUSES = EnumSet.of(
            EntityDamageEvent.DamageCause.FALL,
            EntityDamageEvent.DamageCause.FIRE_TICK,
            EntityDamageEvent.DamageCause.MAGIC,
            EntityDamageEvent.DamageCause.POISON,
            EntityDamageEvent.DamageCause.WITHER,
            EntityDamageEvent.DamageCause.STARVATION,
            EntityDamageEvent.DamageCause.SUFFOCATION,
            EntityDamageEvent.DamageCause.DROWNING,
            EntityDamageEvent.DamageCause.VOID,
            EntityDamageEvent.DamageCause.FREEZE);

    public ArmorListener(ProjectE plugin) {
        this.plugin = plugin;
        this.armorManager = plugin.getArmorManager();
        this.gravitySlamKey = new NamespacedKey(plugin, "is-gravity-slamming");
        this.internalVelocityKey = new NamespacedKey(plugin, "internal-velocity-change");
        startArmorEffectTask();
    }

    private void startArmorEffectTask() {
        plugin.getSchedulerAdapter()
                .runTimer(
                        () -> {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                plugin.getSchedulerAdapter().runTaskOnEntity(player, () -> {
                                    checkGemHelmetEffects(player);
                                    checkGemChestplateEffects(player);
                                    checkGemLeggingsEffects(player);
                                    checkGemBootsEffects(player);
                                });
                            }
                        },
                        0L,
                        1L);
    }

    private void checkGemChestplateEffects(Player player) {
        if (armorManager.isGemChestplate(player.getInventory().getChestplate())) {
            if (player.getFoodLevel() < 20) {
                player.setFoodLevel(player.getFoodLevel() + 1);
            }
        }
    }

    private void checkGemHelmetEffects(Player player) {
        if (armorManager.isGemHelmet(player.getInventory().getHelmet())) {
            if (player.getHealth() < player.getMaxHealth()) {
                player.setHealth(Math.min(player.getHealth() + 0.5, player.getMaxHealth()));
            }
            if (player.isInWater()) {
                player.setRemainingAir(player.getMaximumAir());
            }
            GemHelmet.updateNightVision(player);
        } else {
            if (GemHelmet.isNightVisionActive(player)) {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
        }
    }

    private void checkGemLeggingsEffects(Player player) {
        if (!armorManager.isGemLeggings(player.getInventory().getLeggings())) {
            if (player.getPersistentDataContainer().has(gravitySlamKey, PersistentDataType.BYTE)) {
                player.getPersistentDataContainer().remove(gravitySlamKey);
            }
            return;
        }

        if (player.isSneaking()) {
            for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
                if (entity instanceof LivingEntity && !entity.equals(player)) {
                    Vector direction = entity.getLocation()
                            .toVector()
                            .subtract(player.getLocation().toVector());
                    if (direction.lengthSquared() > 0) {
                        direction.normalize();
                        entity.setVelocity(direction.multiply(1.2));
                    }
                }
            }

            boolean isAirborne =
                    player.getLocation().subtract(0, 1, 0).getBlock().getType().isAir();
            if (isAirborne) {
                if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
                    player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                }
                Vector currentVelocity = player.getVelocity();
                currentVelocity.setY(-2.0);
                player.getPersistentDataContainer().set(internalVelocityKey, PersistentDataType.BYTE, (byte) 1);
                player.setVelocity(currentVelocity);
                player.getPersistentDataContainer().set(gravitySlamKey, PersistentDataType.BYTE, (byte) 1);
            }
        } else {
            if (player.getPersistentDataContainer().has(gravitySlamKey, PersistentDataType.BYTE)) {
                player.getPersistentDataContainer().remove(gravitySlamKey);
            }
        }
    }

    private void checkGemBootsEffects(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        if (armorManager.isGemBoots(player.getInventory().getBoots())) {
            if (!player.isFlying()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 10, 0, true, false));
            }
            player.setAllowFlight(true);
            player.setFlySpeed(0.2f);
            
            // 水上行走效果（类似冰霜行者）
            handleWaterWalking(player);
        } else {
            if (player.getAllowFlight()
                    && !player.getGameMode().equals(GameMode.CREATIVE)
                    && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                player.setAllowFlight(false);
                player.setFlying(false);
                player.setFlySpeed(0.1f);
            }
            if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
                PotionEffect slowFall = player.getPotionEffect(PotionEffectType.SLOW_FALLING);
                if (slowFall != null && slowFall.isAmbient()) {
                    player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                }
            }
        }
    }

    private void handleWaterWalking(Player player) {
        if (player.isSneaking()) return; // 潜行时不激活水上行走
        
        Location playerLoc = player.getLocation();
        
        // 检查玩家脚下和周围的水
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                // 检查脚下的位置
                Block blockBelow = playerLoc.clone().add(x, -1, z).getBlock();
                
                // 检查是否为水
                if (blockBelow.getType() == Material.WATER) {
                    Location blockLoc = blockBelow.getLocation();
                    
                    // 避免重复处理同一个方块
                    if (!temporaryBlocks.containsKey(blockLoc)) {
                        // 记录原始方块类型
                        temporaryBlocks.put(blockLoc, blockBelow.getType());
                        
                        // 将水变成冰
                        blockBelow.setType(Material.FROSTED_ICE);
                        
                        // 3秒后恢复成水
                        plugin.getSchedulerAdapter()
                                .runTaskLaterAtLocation(
                                        blockLoc,
                                        () -> {
                                            if (temporaryBlocks.containsKey(blockLoc) && 
                                                blockBelow.getType() == Material.FROSTED_ICE) {
                                                blockBelow.setType(temporaryBlocks.remove(blockLoc));
                                            }
                                        },
                                        60L);
                    }
                }
                
                // 检查玩家当前位置的水
                Block currentBlock = playerLoc.clone().add(x, 0, z).getBlock();
                if (currentBlock.getType() == Material.WATER) {
                    Location blockLoc = currentBlock.getLocation();
                    
                    if (!temporaryBlocks.containsKey(blockLoc)) {
                        temporaryBlocks.put(blockLoc, currentBlock.getType());
                        currentBlock.setType(Material.FROSTED_ICE);
                        
                        plugin.getSchedulerAdapter()
                                .runTaskLaterAtLocation(
                                        blockLoc,
                                        () -> {
                                            if (temporaryBlocks.containsKey(blockLoc) && 
                                                currentBlock.getType() == Material.FROSTED_ICE) {
                                                currentBlock.setType(temporaryBlocks.remove(blockLoc));
                                            }
                                        },
                                        60L);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getPersistentDataContainer().remove(gravitySlamKey);
        player.getPersistentDataContainer().remove(internalVelocityKey);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.getPersistentDataContainer().remove(gravitySlamKey);
        player.getPersistentDataContainer().remove(internalVelocityKey);
        autoStepEnabled.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (isWearingFullSet(player, "red_matter_")) {
            if (UNPROTECTED_CAUSES.contains(event.getCause())) {
                return;
            }
            event.setCancelled(true);

        } else if (isWearingFullSet(player, "dark_matter_")) {
            if (UNPROTECTED_CAUSES.contains(event.getCause())) {
                return;
            }
            if (event.getDamage() <= 20) {
                event.setCancelled(true);
                player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
            }
        }

        if (armorManager.isGemChestplate(player.getInventory().getChestplate())) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FIRE
                    || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK
                    || event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                event.setCancelled(true);
                player.setFireTicks(0);
            }
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (armorManager.isGemBoots(player.getInventory().getBoots())) {
                event.setCancelled(true);
                return;
            }

            if (player.getPersistentDataContainer().has(gravitySlamKey, PersistentDataType.BYTE)) {
                event.setCancelled(true);
                player.getPersistentDataContainer().remove(gravitySlamKey);

                float fallDistance = player.getFallDistance();
                if (fallDistance > 10) {
                    double radius = 10;
                    double damage = Math.min(fallDistance / 2.0, 50.0);

                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);

                    for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                        if (entity instanceof LivingEntity
                                && !entity.equals(player)
                                && !(entity instanceof ArmorStand)) {
                            LivingEntity livingEntity = (LivingEntity) entity;
                            livingEntity.damage(damage, player);
                            Vector knockback = livingEntity
                                    .getLocation()
                                    .toVector()
                                    .subtract(player.getLocation().toVector())
                                    .normalize();
                            knockback.setY(0.8).multiply(Math.min(fallDistance / 15.0, 3.0));
                            livingEntity.setVelocity(knockback);
                        }
                    }
                }
            }
        }
    }

    private boolean isWearingFullSet(Player player, String armorPrefix) {
        PlayerInventory inventory = player.getInventory();
        ItemStack helmet = inventory.getHelmet();
        ItemStack chestplate = inventory.getChestplate();
        ItemStack leggings = inventory.getLeggings();
        ItemStack boots = inventory.getBoots();

        return isArmorPiece(helmet, armorPrefix + "helmet")
                && isArmorPiece(chestplate, armorPrefix + "chestplate")
                && isArmorPiece(leggings, armorPrefix + "leggings")
                && isArmorPiece(boots, armorPrefix + "boots");
    }

    private boolean isArmorPiece(ItemStack item, String expectedId) {
        if (item == null) {
            return false;
        }
        String id = armorManager.getArmorId(item);
        return expectedId.equals(id);
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        if (armorManager.isDarkMatterArmor(event.getItem())
                || armorManager.isRedMatterArmor(event.getItem())
                || armorManager.isGemHelmet(event.getItem())
                || armorManager.isGemChestplate(event.getItem())
                || armorManager.isGemLeggings(event.getItem())
                || armorManager.isGemBoots(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getFrom().getX() == event.getTo().getX()
                && event.getFrom().getZ() == event.getTo().getZ()) {
            // return;
        }

        // 技术有限...
        /*
        if (autoStepEnabled.getOrDefault(player.getUniqueId(), false) &&
            armorManager.isGemBoots(player.getInventory().getBoots()) &&
            !player.isSneaking() &&
            player.isOnGround() &&
            (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) &&
             event.getTo().getY() <= event.getFrom().getY() + 0.001) { // 允许微小的Y轴变化

            Vector direction = player.getLocation().getDirection().setY(0).normalize().multiply(0.9);
            Location front = player.getLocation().add(direction);

            Block blockInFront = front.getBlock();
            Block blockAbove1 = front.clone().add(0, 1, 0).getBlock();
            Block blockAbove2 = front.clone().add(0, 2, 0).getBlock();

            if (blockInFront.getType().isSolid() && !blockInFront.isPassable() &&
                blockAbove1.isPassable() && blockAbove2.isPassable()) {

                plugin.getSchedulerAdapter().runTaskLaterOnEntity(player, () -> {
                    Location teleportLocation = player.getLocation().add(0, 1, 0);
                    plugin.getSchedulerAdapter().runTask(() -> {
                        player.teleport(teleportLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
                        player.getWorld().playSound(teleportLocation, Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f);
                    });
                }, 1L);
            }
        }
        */

        if (player.isSneaking()
                && player.getLocation().getY() < 100
                && armorManager.isGemLeggings(player.getInventory().getLeggings())) {
            return;
        }

        // 岩浆转换为黑曜石
        if (armorManager.isGemChestplate(player.getInventory().getChestplate()) && !player.isSneaking()) {
            Location loc = player.getLocation();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Block block = loc.clone().add(x, -1, z).getBlock();
                    if (block.getType() == Material.LAVA) {
                        Location blockLoc = block.getLocation();
                        if (!temporaryBlocks.containsKey(blockLoc)) {
                            temporaryBlocks.put(blockLoc, block.getType());
                            block.setType(Material.OBSIDIAN);
                            plugin.getSchedulerAdapter()
                                    .runTaskLaterAtLocation(
                                            blockLoc,
                                            () -> {
                                                if (temporaryBlocks.containsKey(blockLoc)) {
                                                    block.setType(temporaryBlocks.remove(blockLoc));
                                                }
                                            },
                                            20L);
                        }
                    }
                }
            }
        }
    }

    public void toggleAutoStep(Player player) {
        boolean enabled = autoStepEnabled.getOrDefault(player.getUniqueId(), false);
        autoStepEnabled.put(player.getUniqueId(), !enabled);
        if (!enabled) {
            player.sendMessage(plugin.getLanguageManager().get("item.gem_boots.autostep_on"));
        } else {
            player.sendMessage(plugin.getLanguageManager().get("item.gem_boots.autostep_off"));
        }
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);

        boolean isFirstItemArmor = armorManager.isDarkMatterArmor(first)
                || armorManager.isRedMatterArmor(first)
                || armorManager.isGemHelmet(first)
                || armorManager.isGemChestplate(first)
                || armorManager.isGemLeggings(first)
                || armorManager.isGemBoots(first);

        if (isFirstItemArmor) {
            if (event.getInventory().getRenameText() != null
                    && !event.getInventory().getRenameText().isEmpty()) {
                event.setResult(null);
                return;
            }

            if (second != null && second.getType() == org.bukkit.Material.ENCHANTED_BOOK) {
                event.setResult(null);
                return;
            }

            if (second != null) {
                event.setResult(null);
            }
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking()
                && armorManager.isGemHelmet(player.getInventory().getHelmet())) {
            if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                event.setCancelled(true);
                GemHelmetGUI.open(player);
            }
        }
    }

    @EventHandler
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().has(internalVelocityKey, PersistentDataType.BYTE)) {
            player.getPersistentDataContainer().remove(internalVelocityKey);
            return;
        }

        if (player.isSneaking()
                && armorManager.isGemLeggings(player.getInventory().getLeggings())) {
            event.setCancelled(true);
        }
    }
}
