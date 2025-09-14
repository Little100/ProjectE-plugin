package org.Little_100.projecte;

import java.io.File;
import java.util.List;
import org.Little_100.projecte.util.Constants;
import org.Little_100.projecte.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CovalenceDust {
    private final ProjectE plugin;

    private final NamespacedKey lowKey;
    private final NamespacedKey mediumKey;
    private final NamespacedKey highKey;

    private ItemStack lowCovalenceDust;
    private ItemStack mediumCovalenceDust;
    private ItemStack highCovalenceDust;

    public CovalenceDust(ProjectE plugin) {
        this.plugin = plugin;

        this.lowKey = new NamespacedKey(plugin, "low_covalence_dust");
        this.mediumKey = new NamespacedKey(plugin, "medium_covalence_dust");
        this.highKey = new NamespacedKey(plugin, "high_covalence_dust");

        createCovalenceDustItems();
    }

    private void createCovalenceDustItems() {
        lowCovalenceDust = createCovalenceDustItem(
                Material.GLOWSTONE_DUST,
                "item.low_covalence_dust.name",
                1,
                List.of("item.low_covalence_dust.lore1", "item.low_covalence_dust.lore2"),
                lowKey,
                "low_covalence_dust");
        mediumCovalenceDust = createCovalenceDustItem(
                Material.GLOWSTONE_DUST,
                "item.medium_covalence_dust.name",
                2,
                List.of("item.medium_covalence_dust.lore1", "item.medium_covalence_dust.lore2"),
                mediumKey,
                "medium_covalence_dust");
        highCovalenceDust = createCovalenceDustItem(
                Material.GLOWSTONE_DUST,
                "item.high_covalence_dust.name",
                3,
                List.of("item.high_covalence_dust.lore1", "item.high_covalence_dust.lore2"),
                highKey,
                "high_covalence_dust");
    }

    private ItemStack createCovalenceDustItem(
            Material material,
            String displayNameKey,
            int customModelData,
            List<String> loreKeys,
            NamespacedKey key,
            String id) {
        ItemStack item = new ItemStack(material);

        // 统一用CustomModelDataUtil设置cmd，传数字字符串
        item = org.Little_100.projecte.util.CustomModelDataUtil.setCustomModelDataBoth(item, id, customModelData);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // 设置显示名称和Lore
            meta.setDisplayName(plugin.getLanguageManager().get(displayNameKey));
            List<String> translatedLore =
                    loreKeys.stream().map(plugin.getLanguageManager()::get).toList();
            meta.setLore(translatedLore);

            // 设置PDC
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(Constants.ID_KEY, PersistentDataType.STRING, id);
            container.set(key, PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack getLowCovalenceDust() {
        return lowCovalenceDust.clone();
    }

    public ItemStack getMediumCovalenceDust() {
        return mediumCovalenceDust.clone();
    }

    public ItemStack getHighCovalenceDust() {
        return highCovalenceDust.clone();
    }

    public void setCovalenceDustEmcValues() {
        var emcManager = plugin.getEmcManager();
        File configFile = new File(plugin.getDataFolder(), "custommoditememc.yml");
        if (!configFile.exists()) {
            plugin.getLogger()
                    .warning("custommoditememc.yml not found, custom covalence dust EMC values will not be loaded.");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        String lowKey = emcManager.getItemKey(getLowCovalenceDust());
        long lowEmc = config.getLong("low_covalence_dust", 1);
        emcManager.registerEmc(lowKey, lowEmc);

        String mediumKey = emcManager.getItemKey(getMediumCovalenceDust());
        long mediumEmc = config.getLong("medium_covalence_dust", 8);
        emcManager.registerEmc(mediumKey, mediumEmc);

        String highKey = emcManager.getItemKey(getHighCovalenceDust());
        long highEmc = config.getLong("high_covalence_dust", 208);
        emcManager.registerEmc(highKey, highEmc);
    }

    public boolean isLowCovalenceDust(ItemStack item) {
        return isCovalenceDust(item, "low_covalence_dust");
    }

    public boolean isMediumCovalenceDust(ItemStack item) {
        return isCovalenceDust(item, "medium_covalence_dust");
    }

    public boolean isHighCovalenceDust(ItemStack item) {
        return isCovalenceDust(item, "high_covalence_dust");
    }

    private boolean isCovalenceDust(ItemStack item, String id) {
        return ItemUtils.isProjectEItem(item, id);
    }
}
