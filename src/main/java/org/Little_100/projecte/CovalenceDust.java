package org.Little_100.projecte;

import org.Little_100.projecte.util.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class CovalenceDust {
    private final ProjectE plugin;
    private final boolean isModernVersion;

    private final NamespacedKey lowKey;
    private final NamespacedKey mediumKey;
    private final NamespacedKey highKey;

    private ItemStack lowCovalenceDust;
    private ItemStack mediumCovalenceDust;
    private ItemStack highCovalenceDust;

    public CovalenceDust(ProjectE plugin) {
        this.plugin = plugin;
        this.isModernVersion = isVersion1_21_4OrNewer();

        this.lowKey = new NamespacedKey(plugin, "low_covalence_dust");
        this.mediumKey = new NamespacedKey(plugin, "medium_covalence_dust");
        this.highKey = new NamespacedKey(plugin, "high_covalence_dust");

        createCovalenceDustItems();
    }


    private boolean isVersion1_21_4OrNewer() {
        try {
            String version = Bukkit.getServer().getBukkitVersion().split("-")[0];
            String[] versionParts = version.split("\\.");
            if (versionParts.length >= 2) {
                int major = Integer.parseInt(versionParts[0]);
                int minor = Integer.parseInt(versionParts[1]);
                if (major > 1)
                    return true;
                if (major == 1 && minor > 21)
                    return true;
                if (major == 1 && minor == 21 && versionParts.length >= 3) {
                    int patch = Integer.parseInt(versionParts[2]);
                    return patch >= 4;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not parse version number, using legacy mode: " + e.getMessage());
        }
        return false;
    }

    private void createCovalenceDustItems() {
        lowCovalenceDust = createCovalenceDustItem(
                Material.GLOWSTONE_DUST,
                "item.low_covalence_dust.name",
                1,
                List.of("item.low_covalence_dust.lore1", "item.low_covalence_dust.lore2"),
                lowKey,
                "low_covalence_dust"
        );
        mediumCovalenceDust = createCovalenceDustItem(
                Material.GLOWSTONE_DUST,
                "item.medium_covalence_dust.name",
                2,
                List.of("item.medium_covalence_dust.lore1", "item.medium_covalence_dust.lore2"),
                mediumKey,
                "medium_covalence_dust"
        );
        highCovalenceDust = createCovalenceDustItem(
                Material.GLOWSTONE_DUST,
                "item.high_covalence_dust.name",
                3,
                List.of("item.high_covalence_dust.lore1", "item.high_covalence_dust.lore2"),
                highKey,
                "high_covalence_dust"
        );
    }

    private ItemStack createCovalenceDustItem(Material material, String displayNameKey, int customModelData,
                                              List<String> loreKeys, NamespacedKey key, String id) {
        ItemStack item = new ItemStack(material);

        // 统一用CustomModelDataUtil设置cmd，传数字字符串
        item = org.Little_100.projecte.util.CustomModelDataUtil.setCustomModelData(item, customModelData);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // 设置显示名称和Lore
            meta.setDisplayName(plugin.getLanguageManager().get(displayNameKey));
            List<String> translatedLore = loreKeys.stream()
                    .map(plugin.getLanguageManager()::get)
                    .toList();
            meta.setLore(translatedLore);

            // 设置PDC
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(key, PersistentDataType.BYTE, (byte) 1);
            container.set(new NamespacedKey(plugin, "projecte_id"), PersistentDataType.STRING, id);

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
        var db = plugin.getDatabaseManager();
        java.io.File configFile = new java.io.File(plugin.getDataFolder(), "custommoditememc.yml");
        org.bukkit.configuration.file.YamlConfiguration config = null;
        if (configFile.exists()) {
            config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(configFile);
            plugin.getLogger().info("Loaded custom EMC values from custommoditememc.yml");
        } else {
            plugin.getLogger().info("custommoditememc.yml not found, using default EMC values");
        }

        long lowEmc = (config != null) ? config.getLong("low_covalence_dust", 1) : 1;
        db.setEmc(plugin.getEmcManager().getItemKey(getLowCovalenceDust()), lowEmc);

        long mediumEmc = (config != null) ? config.getLong("medium_covalence_dust", 8) : 8;
        db.setEmc(plugin.getEmcManager().getItemKey(getMediumCovalenceDust()), mediumEmc);

        long highEmc = (config != null) ? config.getLong("high_covalence_dust", 208) : 208;
        db.setEmc(plugin.getEmcManager().getItemKey(getHighCovalenceDust()), highEmc);
    }

    public boolean isLowCovalenceDust(ItemStack item) {
        return isCovalenceDust(item, lowKey, "low_covalence_dust");
    }

    public boolean isMediumCovalenceDust(ItemStack item) {
        return isCovalenceDust(item, mediumKey, "medium_covalence_dust");
    }

    public boolean isHighCovalenceDust(ItemStack item) {
        return isCovalenceDust(item, highKey, "high_covalence_dust");
    }

    private boolean isCovalenceDust(ItemStack item, NamespacedKey key, String id) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        boolean hasKey = container.has(key, PersistentDataType.BYTE);
        boolean hasId = container.has(new NamespacedKey(plugin, "projecte_id"), PersistentDataType.STRING);
        String foundId = container.get(new NamespacedKey(plugin, "projecte_id"), PersistentDataType.STRING);

        return hasKey && hasId && id.equals(foundId);
    }
}