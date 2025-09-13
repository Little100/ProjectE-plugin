package org.Little_100.projecte.armor;

import org.Little_100.projecte.ProjectE;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GemHelmet {

    protected static final Map<UUID, Boolean> nightVisionState = new HashMap<>();

    public static boolean isNightVisionActive(Player player) {
        return nightVisionState.getOrDefault(player.getUniqueId(), false);
    }

    public static void toggleNightVision(Player player) {
        UUID playerUUID = player.getUniqueId();
        boolean newState = !nightVisionState.getOrDefault(playerUUID, false);
        nightVisionState.put(playerUUID, newState);
        updateNightVision(player);

        ProjectE plugin = ProjectE.getPlugin(ProjectE.class);
        if (newState) {
            player.sendMessage(plugin.getLanguageManager().get("item.gem_helmet.night_vision_on"));
        } else {
            player.sendMessage(plugin.getLanguageManager().get("item.gem_helmet.night_vision_off"));
        }
    }

    public static void updateNightVision(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (nightVisionState.getOrDefault(playerUUID, false)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, true));
        } else {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }

    public static void applyWaterWalking(Player player) {
    }


    public static boolean hasGemHelmet(Player player, ArmorManager armorManager) {
        if (player.getInventory().getHelmet() == null) {
            return false;
        }
        return "gem_helmet".equals(armorManager.getArmorId(player.getInventory().getHelmet()));
    }
}