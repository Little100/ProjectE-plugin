package org.Little_100.projecte.accessories;

import org.Little_100.projecte.LanguageManager;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.Tools.KleinStar.KleinStarManager;
import org.Little_100.projecte.util.CustomModelDataUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SoulStone {
    public static ItemStack createSoulStone() {
        ItemStack item = new ItemStack(Material.GOLDEN_HORSE_ARMOR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            LanguageManager languageManager = ProjectE.getInstance().getLanguageManager();
            meta.setDisplayName(languageManager.get("item.soul_stone.name"));
            List<String> loreKeys = Arrays.asList("item.soul_stone.lore1", "item.soul_stone.lore2");
            List<String> lore = loreKeys.stream().map(languageManager::get).collect(Collectors.toList());
            meta.setLore(lore);
            org.bukkit.persistence.PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(new org.bukkit.NamespacedKey(ProjectE.getInstance(), "projecte_id"), org.bukkit.persistence.PersistentDataType.STRING, "soul_stone");
            item.setItemMeta(meta);
            CustomModelDataUtil.setCustomModelDataBoth(item, "soul_stone", 2);
        }
        return item;
    }

    public static void activate(Player player, ItemStack item) {
        if (player.getHealth() < player.getMaxHealth()) {
            if (!KleinStarManager.hasKleinStar(player)) {
                LanguageManager languageManager = ProjectE.getInstance().getLanguageManager();
                player.sendMessage(languageManager.get("serverside.message.klein_star.no_container"));
                return;
            }
            if (KleinStarManager.hasEnoughEMC(player, 64)) {
                KleinStarManager.consumeEMC(player, 64);
                player.setHealth(Math.min(player.getHealth() + 1, player.getMaxHealth()));
                LanguageManager languageManager = ProjectE.getInstance().getLanguageManager();
                player.sendMessage(languageManager.get("serverside.message.accessory.activated").replace("{item}", item.getItemMeta().getDisplayName()));
            } else {
                LanguageManager languageManager = ProjectE.getInstance().getLanguageManager();
                player.sendMessage(languageManager.get("serverside.message.klein_star.no_emc"));
            }
        }
    }
}