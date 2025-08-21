package org.Little_100.projecte.Tome;

import org.Little_100.projecte.LanguageManager;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.util.CustomModelDataUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.stream.Collectors;

public class TransmutationTabletBook {

    public static ItemStack createTransmutationTabletBook() {
        return createTransmutationTabletBook(null);
    }

    public static ItemStack createTransmutationTabletBook(ConfigurationSection config) {
        LanguageManager languageManager = ProjectE.getInstance().getLanguageManager();
        ItemStack tome = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = tome.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(languageManager.get("item.transmutation_tablet_book.name"));
            meta.setLore(Collections.singletonList(languageManager.get("item.transmutation_tablet_book.lore")));
            tome.setItemMeta(meta);
            tome = CustomModelDataUtil.setCustomModelData(tome, 1);
        }
        return tome;
    }
}