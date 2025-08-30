package org.Little_100.projecte.Armor;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.EnumSet;
import java.util.Set;

public class ArmorListener implements Listener {

    private final ProjectE plugin;
    private final ArmorManager armorManager;

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
            EntityDamageEvent.DamageCause.FREEZE
    );

    public ArmorListener(ProjectE plugin) {
        this.plugin = plugin;
        this.armorManager = plugin.getArmorManager();
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
    }

    private boolean isWearingFullSet(Player player, String armorPrefix) {
        PlayerInventory inventory = player.getInventory();
        ItemStack helmet = inventory.getHelmet();
        ItemStack chestplate = inventory.getChestplate();
        ItemStack leggings = inventory.getLeggings();
        ItemStack boots = inventory.getBoots();

        return isArmorPiece(helmet, armorPrefix + "helmet") &&
               isArmorPiece(chestplate, armorPrefix + "chestplate") &&
               isArmorPiece(leggings, armorPrefix + "leggings") &&
               isArmorPiece(boots, armorPrefix + "boots");
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
        if (armorManager.isDarkMatterArmor(event.getItem()) || armorManager.isRedMatterArmor(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);
        
        boolean isFirstItemArmor = armorManager.isDarkMatterArmor(first) || armorManager.isRedMatterArmor(first);
        
        if (isFirstItemArmor) {
            if (event.getInventory().getRenameText() != null && !event.getInventory().getRenameText().isEmpty()) {
                 event.setResult(null);
                 return;
            }
            if (second != null && second.getType() == org.bukkit.Material.ENCHANTED_BOOK) {
                 event.setResult(null);
                 return;
            }
             if (second != null) {
                event.setResult(null);
                return;
            }
        }
    }
}