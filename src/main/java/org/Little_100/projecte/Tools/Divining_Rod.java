package org.Little_100.projecte.Tools;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class Divining_Rod {
    private final ProjectE plugin;

    private final NamespacedKey lowKey;
    private final NamespacedKey mediumKey;
    private final NamespacedKey highKey;

    private ItemStack lowDiviningRod;
    private ItemStack mediumDiviningRod;
    private ItemStack highDiviningRod;

    public Divining_Rod(ProjectE plugin) {
        this.plugin = plugin;

        this.lowKey = new NamespacedKey(plugin, "low_divining_rod");
        this.mediumKey = new NamespacedKey(plugin, "medium_divining_rod");
        this.highKey = new NamespacedKey(plugin, "high_divining_rod");

        createDiviningRodItems();
    }

    private void createDiviningRodItems() {
        lowDiviningRod = createDiviningRodItem(
                "item.low_divining_rod.name",
                1,
                List.of("item.low_divining_rod.lore1", "item.low_divining_rod.lore2"),
                lowKey,
                "low_divining_rod"
        );
        mediumDiviningRod = createDiviningRodItem(
                "item.medium_divining_rod.name",
                2,
                List.of("item.medium_divining_rod.lore1", "item.medium_divining_rod.lore2"),
                mediumKey,
                "medium_divining_rod"
        );
        highDiviningRod = createDiviningRodItem(
                "item.high_divining_rod.name",
                3,
                List.of("item.high_divining_rod.lore1", "item.high_divining_rod.lore2"),
                highKey,
                "high_divining_rod"
        );
    }

    private ItemStack createDiviningRodItem(String displayNameKey, int customModelData,
                                              List<String> loreKeys, NamespacedKey key, String id) {
        ItemStack item = new ItemStack(Material.STICK);

        item = org.Little_100.projecte.util.CustomModelDataUtil.setCustomModelDataBoth(item, id, customModelData);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getLanguageManager().get(displayNameKey));
            List<String> translatedLore = loreKeys.stream()
                    .map(plugin.getLanguageManager()::get)
                    .toList();
            meta.setLore(translatedLore);

            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(plugin, "projecte_id"), PersistentDataType.STRING, id);
            container.set(key, PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack getLowDiviningRod() {
        return lowDiviningRod.clone();
    }

    public ItemStack getMediumDiviningRod() {
        return mediumDiviningRod.clone();
    }

    public ItemStack getHighDiviningRod() {
        return highDiviningRod.clone();
    }

    public void setDiviningRodEmcValues() {
        var emcManager = plugin.getEmcManager();
        java.io.File configFile = new java.io.File(plugin.getDataFolder(), "custommoditememc.yml");
        if (!configFile.exists()) {
            plugin.getLogger().warning("custommoditememc.yml not found, custom divining rod EMC values will not be loaded.");
            return;
        }
        org.bukkit.configuration.file.YamlConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(configFile);

        String lowKey = emcManager.getItemKey(getLowDiviningRod());
        long lowEmc = config.getLong("low_divining_rod", 12);
        emcManager.registerEmc(lowKey, lowEmc);

        String mediumKey = emcManager.getItemKey(getMediumDiviningRod());
        long mediumEmc = config.getLong("medium_divining_rod", 68);
        emcManager.registerEmc(mediumKey, mediumEmc);

        String highKey = emcManager.getItemKey(getHighDiviningRod());
        long highEmc = config.getLong("high_divining_rod", 1668);
        emcManager.registerEmc(highKey, highEmc);
    }

    public boolean isLowDiviningRod(ItemStack item) {
        return isDiviningRod(item, lowKey, "low_divining_rod");
    }

    public boolean isMediumDiviningRod(ItemStack item) {
        return isDiviningRod(item, mediumKey, "medium_divining_rod");
    }

    public boolean isHighDiviningRod(ItemStack item) {
        return isDiviningRod(item, highKey, "high_divining_rod");
    }

    private boolean isDiviningRod(ItemStack item, NamespacedKey key, String id) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String foundId = container.get(new NamespacedKey(plugin, "projecte_id"), PersistentDataType.STRING);
        return id.equals(foundId);
    }
}
