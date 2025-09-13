package org.Little_100.projecte.tome;

import java.util.Collections;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.managers.LanguageManager;
import org.Little_100.projecte.util.CustomModelDataUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TransmutationTabletBook {

    public static ItemStack createTransmutationTabletBook() {
        return createTransmutationTabletBook(null);
    }

    public static ItemStack createTransmutationTabletBook(ConfigurationSection config) {
        // 记录创建转换卓
        ProjectE.getInstance().getLogger().info("创建便携式转换卓...");

        LanguageManager languageManager = ProjectE.getInstance().getLanguageManager();
        ItemStack tome = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = tome.getItemMeta();
        if (meta != null) {
            String name = languageManager.get("item.transmutation_tablet_book.name");
            String lore = languageManager.get("item.transmutation_tablet_book.lore");

            meta.setDisplayName(name);
            meta.setLore(Collections.singletonList(lore));

            ProjectE.getInstance().getLogger().info("设置名称: " + name);
            ProjectE.getInstance().getLogger().info("设置描述: " + lore);

            try {
                meta.setCustomModelData(1);
                ProjectE.getInstance().getLogger().info("直接设置CustomModelData: 1");
            } catch (Exception e) {
                ProjectE.getInstance().getLogger().warning("设置CustomModelData失败: " + e.getMessage());
            }

            NamespacedKey key = new NamespacedKey(ProjectE.getInstance(), "projecte_id");
            meta.getPersistentDataContainer()
                    .set(key, org.bukkit.persistence.PersistentDataType.STRING, "transmutation_tablet_book");
            ProjectE.getInstance().getLogger().info("设置PersistentDataContainer: transmutation_tablet_book");

            tome.setItemMeta(meta);

            try {
                tome = CustomModelDataUtil.setCustomModelData(tome, 1);
                ProjectE.getInstance().getLogger().info("使用CustomModelDataUtil设置CustomModelData: 1");
            } catch (Exception e) {
                ProjectE.getInstance().getLogger().warning("使用CustomModelDataUtil设置失败: " + e.getMessage());
            }
        }

        return tome;
    }
}
