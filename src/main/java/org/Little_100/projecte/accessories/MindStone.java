package org.Little_100.projecte.accessories;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.managers.LanguageManager;
import org.Little_100.projecte.util.Constants;
import org.Little_100.projecte.util.CustomModelDataUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class MindStone {
    private static final NamespacedKey STORED_XP_KEY = new NamespacedKey(ProjectE.getInstance(), "stored_xp");

    public static ItemStack createMindStone() {
        ItemStack item = new ItemStack(Material.GOLDEN_HORSE_ARMOR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            LanguageManager languageManager = ProjectE.getInstance().getLanguageManager();
            meta.setDisplayName(languageManager.get("item.mind_stone.name"));
            List<String> loreKeys = Arrays.asList(
                    "item.mind_stone.lore1", "item.mind_stone.lore2", "item.mind_stone.lore3", "item.mind_stone.lore4");
            List<String> lore = loreKeys.stream().map(languageManager::get).collect(Collectors.toList());
            meta.setLore(lore);
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(Constants.ID_KEY, PersistentDataType.STRING, "mind_stone");
            data.set(STORED_XP_KEY, PersistentDataType.INTEGER, 0);
            item.setItemMeta(meta);
            CustomModelDataUtil.setCustomModelDataBoth(item, "mind_stone", 4);
            updateLore(item);
        }

        return item;
    }

    public static void activate(Player player, ItemStack item) {
        if (player.isSneaking()) {
            // Absorb XP
            if (player.getTotalExperience() > 0) {
                int xpToStore = player.getTotalExperience();
                player.setTotalExperience(0);
                player.setLevel(0);
                player.setExp(0f);
                storeXp(item, getStoredXp(item) + xpToStore);
                LanguageManager languageManager = ProjectE.getInstance().getLanguageManager();
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("xp", String.valueOf(xpToStore));
                player.sendMessage(languageManager.get("serverside.message.mind_stone.absorbed_xp", placeholders));
            }
        } else {
            // Retrieve XP
            // Retrieve XP
            int storedXp = getStoredXp(item);
            if (storedXp > 0) {
                int amountToGive = Math.min(50, storedXp);
                player.giveExp(amountToGive);
                storeXp(item, storedXp - amountToGive);
                LanguageManager languageManager = ProjectE.getInstance().getLanguageManager();
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("xp", String.valueOf(amountToGive));
                player.sendMessage(languageManager.get("serverside.message.mind_stone.retrieved_xp", placeholders));
            }
        }
    }

    private static int getStoredXp(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.getPersistentDataContainer().has(STORED_XP_KEY, PersistentDataType.INTEGER)) {
                return meta.getPersistentDataContainer().get(STORED_XP_KEY, PersistentDataType.INTEGER);
            }
        }
        return 0;
    }

    private static void storeXp(ItemStack item, int amount) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(STORED_XP_KEY, PersistentDataType.INTEGER, amount);
            item.setItemMeta(meta);
            updateLore(item);
        }
    }

    public static void updateLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        List<String> lore = meta.getLore();
        if (lore == null || lore.size() < 4) {
            return;
        }

        int storedXp = getStoredXp(item);
        LanguageManager languageManager = ProjectE.getInstance().getLanguageManager();
        String lore4Template = languageManager.get("item.mind_stone.lore4");
        lore.set(3, lore4Template.replace("{xp}", String.valueOf(storedXp)));
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
}
