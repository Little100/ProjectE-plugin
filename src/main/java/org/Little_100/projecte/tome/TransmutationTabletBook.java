package org.Little_100.projecte.tome;

import org.Little_100.projecte.Debug;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.managers.LanguageManager;
import org.Little_100.projecte.util.Constants;
import org.Little_100.projecte.util.CustomModelDataUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;

public class TransmutationTabletBook {

    public static ItemStack createTransmutationTabletBook() {
        return createTransmutationTabletBook(null);
    }

    public static ItemStack createTransmutationTabletBook(@SuppressWarnings("unused") ConfigurationSection config) {
        // 记录创建转换卓
        Debug.log("创建便携式转换卓...");

        LanguageManager languageManager = ProjectE.getInstance().getLanguageManager();
        ItemStack tome = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = tome.getItemMeta();
        if (meta != null) {
            String name = languageManager.get("item.transmutation_tablet_book.name");
            String lore = languageManager.get("item.transmutation_tablet_book.lore");

            meta.setDisplayName(name);
            meta.setLore(Collections.singletonList(lore));

            Debug.log("设置名称: " + name);
            Debug.log("设置描述: " + lore);

            try {
                meta.setCustomModelData(1);
                Debug.log("直接设置CustomModelData: 1");
            } catch (Exception e) {
                ProjectE.getInstance().getLogger().warning("设置CustomModelData失败: " + e.getMessage());
            }

            meta.getPersistentDataContainer()
                    .set(
                            Constants.ID_KEY,
                            PersistentDataType.STRING,
                            "transmutation_tablet_book");
            Debug.log("设置PersistentDataContainer: transmutation_tablet_book");

            tome.setItemMeta(meta);

            try {
                tome = CustomModelDataUtil.setCustomModelData(tome, 1);
                Debug.log("使用CustomModelDataUtil设置CustomModelData: 1");
            } catch (Exception e) {
                ProjectE.getInstance().getLogger().warning("使用CustomModelDataUtil设置失败: " + e.getMessage());
            }
        }

        return tome;
    }
}
