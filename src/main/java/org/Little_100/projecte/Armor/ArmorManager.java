package org.Little_100.projecte.Armor;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.util.CustomModelDataUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ArmorManager {

    private final ProjectE plugin;

    private ItemStack darkMatterHelmet;
    private ItemStack darkMatterChestplate;
    private ItemStack darkMatterLeggings;
    private ItemStack darkMatterBoots;

    private ItemStack redMatterHelmet;
    private ItemStack redMatterChestplate;
    private ItemStack redMatterLeggings;
    private ItemStack redMatterBoots;

    public ArmorManager(ProjectE plugin) {
        this.plugin = plugin;
        createDarkMatterArmor();
        createRedMatterArmor();
        registerArmorEmcValues();
    }

    private void createDarkMatterArmor() {
        darkMatterHelmet = createArmorItem(Material.DIAMOND_HELMET, "dark_matter_helmet", 1, "item.dark_matter_helmet.name", EquipmentSlot.HEAD, 7.0, 2.0);
        darkMatterChestplate = createArmorItem(Material.DIAMOND_CHESTPLATE, "dark_matter_chestplate", 1, "item.dark_matter_chestplate.name", EquipmentSlot.CHEST, 14.0, 2.0);
        darkMatterLeggings = createArmorItem(Material.DIAMOND_LEGGINGS, "dark_matter_leggings", 1, "item.dark_matter_leggings.name", EquipmentSlot.LEGS, 12.0, 2.0);
        darkMatterBoots = createArmorItem(Material.DIAMOND_BOOTS, "dark_matter_boots", 1, "item.dark_matter_boots.name", EquipmentSlot.FEET, 7.0, 2.0);
    }

    private void createRedMatterArmor() {
        redMatterHelmet = createArmorItem(Material.DIAMOND_HELMET, "red_matter_helmet", 2, "item.red_matter_helmet.name", EquipmentSlot.HEAD, 7.0, 2.0);
        redMatterChestplate = createArmorItem(Material.DIAMOND_CHESTPLATE, "red_matter_chestplate", 2, "item.red_matter_chestplate.name", EquipmentSlot.CHEST, 14.0, 2.0);
        redMatterLeggings = createArmorItem(Material.DIAMOND_LEGGINGS, "red_matter_leggings", 2, "item.red_matter_leggings.name", EquipmentSlot.LEGS, 12.0, 2.0);
        redMatterBoots = createArmorItem(Material.DIAMOND_BOOTS, "red_matter_boots", 2, "item.red_matter_boots.name", EquipmentSlot.FEET, 7.0, 2.0);
    }

    private void registerArmorEmcValues() { // EMC设置
        plugin.getEmcManager().setEmcValue(darkMatterHelmet, 696320);
        plugin.getEmcManager().setEmcValue(darkMatterChestplate, 1114112);
        plugin.getEmcManager().setEmcValue(darkMatterLeggings, 974848);
        plugin.getEmcManager().setEmcValue(darkMatterBoots, 557056);

        plugin.getEmcManager().setEmcValue(redMatterHelmet, 3031040);
        plugin.getEmcManager().setEmcValue(redMatterChestplate, 4849664);
        plugin.getEmcManager().setEmcValue(redMatterLeggings, 4243456);
        plugin.getEmcManager().setEmcValue(redMatterBoots, 2424832);
    }

    private ItemStack createArmorItem(Material baseMaterial, String id, int customModelData, String displayNameKey, EquipmentSlot slot, double armor, double armorToughness) {
        ItemStack item = new ItemStack(baseMaterial);

        CustomModelDataUtil.registerMapping(id, customModelData);
        item = CustomModelDataUtil.setCustomModelData(item, id);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getLanguageManager().get(displayNameKey));
            meta.setUnbreakable(true);

            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(plugin, "projecte_id"), PersistentDataType.STRING, id);

            if (armor > 0) {
                AttributeModifier armorModifier = new AttributeModifier(UUID.randomUUID(), "generic.armor", armor, AttributeModifier.Operation.ADD_NUMBER, slot);
                meta.addAttributeModifier(Attribute.valueOf("GENERIC_ARMOR"), armorModifier);
            }
            if (armorToughness > 0) {
                AttributeModifier toughnessModifier = new AttributeModifier(UUID.randomUUID(), "generic.armor_toughness", armorToughness, AttributeModifier.Operation.ADD_NUMBER, slot);
                meta.addAttributeModifier(Attribute.valueOf("GENERIC_ARMOR_TOUGHNESS"), toughnessModifier);
            }

            List<String> lore = new ArrayList<>();
            if (id.startsWith("dark_matter_")) {
                lore.add(plugin.getLanguageManager().get("item.dark_matter_armor.lore1"));
            } else if (id.startsWith("red_matter_")) {
                lore.add(plugin.getLanguageManager().get("item.red_matter_armor.lore1"));
            }
            meta.setLore(lore);

            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack getDarkMatterHelmet() { return darkMatterHelmet.clone(); }
    public ItemStack getDarkMatterChestplate() { return darkMatterChestplate.clone(); }
    public ItemStack getDarkMatterLeggings() { return darkMatterLeggings.clone(); }
    public ItemStack getDarkMatterBoots() { return darkMatterBoots.clone(); }
    public ItemStack getRedMatterHelmet() { return redMatterHelmet.clone(); }
    public ItemStack getRedMatterChestplate() { return redMatterChestplate.clone(); }
    public ItemStack getRedMatterLeggings() { return redMatterLeggings.clone(); }
    public ItemStack getRedMatterBoots() { return redMatterBoots.clone(); }

    public boolean isDarkMatterArmor(ItemStack item) {
        String id = getArmorId(item);
        return id != null && id.startsWith("dark_matter_") && (id.endsWith("_helmet") || id.endsWith("_chestplate") || id.endsWith("_leggings") || id.endsWith("_boots"));
    }

    public boolean isRedMatterArmor(ItemStack item) {
        String id = getArmorId(item);
        return id != null && id.startsWith("red_matter_") && (id.endsWith("_helmet") || id.endsWith("_chestplate") || id.endsWith("_leggings") || id.endsWith("_boots"));
    }

    public String getArmorId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(new NamespacedKey(plugin, "projecte_id"), PersistentDataType.STRING);
    }
    public boolean isArmor(String id) {
        if (id == null) return false;
        switch (id) {
            case "dark_matter_helmet":
            case "dark_matter_chestplate":
            case "dark_matter_leggings":
            case "dark_matter_boots":
            case "red_matter_helmet":
            case "red_matter_chestplate":
            case "red_matter_leggings":
            case "red_matter_boots":
                return true;
            default:
                return false;
        }
    }

    public ItemStack getArmor(String id) {
        if (id == null) return null;
        switch (id) {
            case "dark_matter_helmet": return getDarkMatterHelmet();
            case "dark_matter_chestplate": return getDarkMatterChestplate();
            case "dark_matter_leggings": return getDarkMatterLeggings();
            case "dark_matter_boots": return getDarkMatterBoots();
            case "red_matter_helmet": return getRedMatterHelmet();
            case "red_matter_chestplate": return getRedMatterChestplate();
            case "red_matter_leggings": return getRedMatterLeggings();
            case "red_matter_boots": return getRedMatterBoots();
            default: return null;
        }
    }
}