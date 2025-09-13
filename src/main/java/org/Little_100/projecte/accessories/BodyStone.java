package org.Little_100.projecte.accessories;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.managers.LanguageManager;
import org.Little_100.projecte.tools.kleinstar.KleinStarManager;
import org.Little_100.projecte.util.CustomModelDataUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BodyStone {
    public static ItemStack createBodyStone() {
        ItemStack item = new ItemStack(Material.GOLDEN_HORSE_ARMOR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            LanguageManager languageManager = ProjectE.getInstance().getLanguageManager();
            meta.setDisplayName(languageManager.get("item.body_stone.name"));
            List<String> loreKeys = Arrays.asList("item.body_stone.lore1", "item.body_stone.lore2");
            List<String> lore = loreKeys.stream().map(languageManager::get).collect(Collectors.toList());
            meta.setLore(lore);
            org.bukkit.persistence.PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(
                    new org.bukkit.NamespacedKey(ProjectE.getInstance(), "projecte_id"),
                    org.bukkit.persistence.PersistentDataType.STRING,
                    "body_stone");
            item.setItemMeta(meta);
            CustomModelDataUtil.setCustomModelDataBoth(item, "body_stone", 1);
        }
        return item;
    }

    public static void activate(Player player, ItemStack item) {
        if (player.getFoodLevel() < 20) {
            if (!KleinStarManager.hasKleinStar(player)) {
                LanguageManager languageManager = ProjectE.getInstance().getLanguageManager();
                player.sendMessage(languageManager.get("serverside.message.klein_star.no_container"));
                return;
            }
            if (KleinStarManager.hasEnoughEMC(player, 64)) {
                KleinStarManager.consumeEMC(player, 64);
                player.setFoodLevel(Math.min(player.getFoodLevel() + 2, 20));
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
                LanguageManager languageManager = ProjectE.getInstance().getLanguageManager();
                player.sendMessage(languageManager
                        .get("serverside.message.accessory.activated")
                        .replace("{item}", item.getItemMeta().getDisplayName()));
            } else {
                LanguageManager languageManager = ProjectE.getInstance().getLanguageManager();
                player.sendMessage(languageManager.get("serverside.message.klein_star.no_emc"));
            }
        }
    }
}
