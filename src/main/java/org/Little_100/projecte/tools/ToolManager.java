package org.Little_100.projecte.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.util.Constants;
import org.Little_100.projecte.util.CustomModelDataUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ToolManager {
    private final ProjectE plugin;

    private ItemStack darkMatterPickaxe;
    private ItemStack darkMatterAxe;
    private ItemStack darkMatterShovel;
    private ItemStack darkMatterHoe;
    private ItemStack darkMatterSword;
    private ItemStack darkMatterShears;
    private ItemStack darkMatterHammer;

    private ItemStack redMatterPickaxe;
    private ItemStack redMatterAxe;
    private ItemStack redMatterShovel;
    private ItemStack redMatterHoe;
    private ItemStack redMatterSword;
    private ItemStack redMatterShears;
    private ItemStack redMatterHammer;
    private ItemStack redMatterKatar;
    private ItemStack redMatterMorningstar;

    public ToolManager(ProjectE plugin) {
        this.plugin = plugin;
        createDarkMatterTools();
        createRedMatterTools();
        registerToolEmcValues();
    }

    private void registerToolEmcValues() {
        plugin.getEmcManager().setEmcValue(darkMatterPickaxe, 434176);
        plugin.getEmcManager().setEmcValue(darkMatterAxe, 434176);
        plugin.getEmcManager().setEmcValue(darkMatterShovel, 155648);
        plugin.getEmcManager().setEmcValue(darkMatterHoe, 294912);
        plugin.getEmcManager().setEmcValue(darkMatterSword, 286720);
        plugin.getEmcManager().setEmcValue(darkMatterShears, 147456);
        plugin.getEmcManager().setEmcValue(darkMatterHammer, 303104);

        plugin.getEmcManager().setEmcValue(redMatterPickaxe, 1974272);
        plugin.getEmcManager().setEmcValue(redMatterAxe, 1974272);
        plugin.getEmcManager().setEmcValue(redMatterShovel, 761856);
        plugin.getEmcManager().setEmcValue(redMatterHoe, 1368064);
        plugin.getEmcManager().setEmcValue(redMatterSword, 892928);
        plugin.getEmcManager().setEmcValue(redMatterShears, 614400);
        plugin.getEmcManager().setEmcValue(redMatterHammer, 1515520);
        plugin.getEmcManager().setEmcValue(redMatterKatar, 7512064);
        plugin.getEmcManager().setEmcValue(redMatterMorningstar, 7055312);
    }

    private void createDarkMatterTools() {
        darkMatterPickaxe = createToolItem(
                Material.DIAMOND_PICKAXE, "dark_matter_pickaxe", 1, "item.dark_matter_pickaxe.name", 8, 1.2);
        darkMatterAxe = createToolItem(Material.DIAMOND_AXE, "dark_matter_axe", 1, "item.dark_matter_axe.name", 9, 1.0);
        darkMatterShovel = createToolItem(
                Material.DIAMOND_SHOVEL, "dark_matter_shovel", 1, "item.dark_matter_shovel.name", 6, 1.0);
        darkMatterHoe = createToolItem(Material.DIAMOND_HOE, "dark_matter_hoe", 7, "item.dark_matter_hoe.name", 1, 1.0);
        darkMatterSword =
                createToolItem(Material.DIAMOND_SWORD, "dark_matter_sword", 1, "item.dark_matter_sword.name", 13, 1.6);
        darkMatterShears =
                createToolItem(Material.SHEARS, "dark_matter_shears", 1, "item.dark_matter_shears.name", 1, 1.0);
        darkMatterHammer = createToolItem(
                Material.DIAMOND_PICKAXE, "dark_matter_hammer", 3, "item.dark_matter_hammer.name", 14, 1.2);
    }

    private void createRedMatterTools() {
        redMatterPickaxe = createToolItem(
                Material.DIAMOND_PICKAXE, "red_matter_pickaxe", 2, "item.red_matter_pickaxe.name", 9, 1.2);
        redMatterAxe = createToolItem(Material.DIAMOND_AXE, "red_matter_axe", 2, "item.red_matter_axe.name", 10, 1.0);
        redMatterShovel =
                createToolItem(Material.DIAMOND_SHOVEL, "red_matter_shovel", 2, "item.red_matter_shovel.name", 7, 1.0);
        redMatterHoe = createToolItem(Material.DIAMOND_HOE, "red_matter_hoe", 8, "item.red_matter_hoe.name", 1, 4.0);
        redMatterSword =
                createToolItem(Material.DIAMOND_SWORD, "red_matter_sword", 2, "item.red_matter_sword.name", 14, 1.6);
        redMatterShears =
                createToolItem(Material.SHEARS, "red_matter_shears", 2, "item.red_matter_shears.name", 1, 1.0);
        redMatterHammer = createToolItem(
                Material.DIAMOND_PICKAXE, "red_matter_hammer", 4, "item.red_matter_hammer.name", 15, 1.2);
        redMatterKatar =
                createToolItem(Material.DIAMOND_AXE, "red_matter_katar", 10, "item.red_matter_katar.name", 24, 1.6);
        redMatterMorningstar = createToolItem(
                Material.DIAMOND_PICKAXE, "red_matter_morningstar", 11, "item.red_matter_morningstar.name", 24, 1.2);
    }

    private ItemStack createToolItem(
            Material baseMaterial,
            String id,
            int customModelData,
            String displayNameKey,
            double attackDamage,
            double attackSpeed) {
        ItemStack item = new ItemStack(baseMaterial);

        CustomModelDataUtil.registerMapping(id, customModelData);
        item = CustomModelDataUtil.setCustomModelData(item, id);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getLanguageManager().get(displayNameKey));

            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(plugin, "projecte_id"), PersistentDataType.STRING, id);
            container.set(Constants.CHARGE_KEY, PersistentDataType.INTEGER, 0);
            container.set(new NamespacedKey(plugin, "projecte_mode"), PersistentDataType.STRING, "tall");

            switch (id) {
                case "red_matter_sword":
                    container.set(new NamespacedKey(plugin, "projecte_sword_mode"), PersistentDataType.INTEGER, 0);
                    break;
                case "red_matter_katar":
                    container.set(Constants.KATAR_MODE_KEY, PersistentDataType.INTEGER, 0);
                    break;
                case "red_matter_morningstar":
                    container.set(
                            new NamespacedKey(plugin, "projecte_morningstar_mode"),
                            PersistentDataType.STRING,
                            "normal");
                    break;
            }

            if (attackDamage > 0) {
                AttributeModifier damageModifier = new AttributeModifier(
                        UUID.randomUUID(),
                        "generic.attack_damage",
                        attackDamage - 1,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HAND);
                meta.addAttributeModifier(Attribute.valueOf("GENERIC_ATTACK_DAMAGE"), damageModifier);
            }
            if (attackSpeed > 0) {
                AttributeModifier speedModifier = new AttributeModifier(
                        UUID.randomUUID(),
                        "generic.attack_speed",
                        attackSpeed - 4,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HAND);
                meta.addAttributeModifier(Attribute.valueOf("GENERIC_ATTACK_SPEED"), speedModifier);
            }

            item.setItemMeta(meta);
            updateLore(item);
        }
        return item;
    }

    public void updateLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String id = container.get(new NamespacedKey(plugin, "projecte_id"), PersistentDataType.STRING);
        if (id == null) return;

        List<String> loreKeys;
        if (id.startsWith("dark_matter_")) {
            loreKeys = List.of("item.dark_matter_tool.lore1");
        } else if (id.startsWith("red_matter_")) {
            loreKeys = List.of("item.red_matter_tool.lore1");
        } else {
            return;
        }

        List<String> newLore = new ArrayList<>();
        for (String key : loreKeys) {
            newLore.add(plugin.getLanguageManager().get(key));
        }

        Integer charge = container.get(Constants.CHARGE_KEY, PersistentDataType.INTEGER);
        if (charge != null) {
            newLore.add(plugin.getLanguageManager()
                    .get("clientside.dark_matter_tool.charge")
                    .replace("{level}", charge.toString()));
        }

        if (isRedMatterSword(item)) {
            Integer mode = container.get(new NamespacedKey(plugin, "projecte_sword_mode"), PersistentDataType.INTEGER);
            if (mode != null) {
                String modeKey = (mode == 0)
                        ? "clientside.red_matter_sword.mode_hostile"
                        : "clientside.red_matter_sword.mode_all";
                newLore.add(plugin.getLanguageManager().get("clientside.red_matter_sword.mode_prefix") + " "
                        + plugin.getLanguageManager().get(modeKey));
            }
        } else if (isRedMatterKatar(item)) {
            Integer mode = container.get(Constants.KATAR_MODE_KEY, PersistentDataType.INTEGER);
            if (mode != null) {
                String modeKey = (mode == 0)
                        ? "clientside.red_matter_katar.mode_all"
                        : "clientside.red_matter_katar.mode_hostile";
                newLore.add(plugin.getLanguageManager().get("clientside.red_matter_katar.mode_prefix") + " "
                        + plugin.getLanguageManager().get(modeKey));
            }
        } else if (isRedMatterMorningstar(item)) {
            String mode =
                    container.get(new NamespacedKey(plugin, "projecte_morningstar_mode"), PersistentDataType.STRING);
            if (mode != null) {
                String modeKey = "clientside.red_matter_morningstar.mode_" + mode;
                newLore.add(plugin.getLanguageManager().get("clientside.red_matter_morningstar.mode_prefix") + " "
                        + plugin.getLanguageManager().get(modeKey));
            }
        }

        meta.setLore(newLore);
        item.setItemMeta(meta);
    }

    public ItemStack getDarkMatterPickaxe() {
        ItemStack item = darkMatterPickaxe.clone();
        updateLore(item);
        return item;
    }

    public ItemStack getDarkMatterAxe() {
        ItemStack item = darkMatterAxe.clone();
        updateLore(item);
        return item;
    }

    public ItemStack getDarkMatterShovel() {
        ItemStack item = darkMatterShovel.clone();
        updateLore(item);
        return item;
    }

    public ItemStack getDarkMatterHoe() {
        ItemStack item = darkMatterHoe.clone();
        updateLore(item);
        return item;
    }

    public ItemStack getDarkMatterSword() {
        ItemStack item = darkMatterSword.clone();
        updateLore(item);
        return item;
    }

    public ItemStack getDarkMatterShears() {
        return darkMatterShears.clone();
    }

    public ItemStack getDarkMatterHammer() {
        return darkMatterHammer.clone();
    }

    public ItemStack getRedMatterPickaxe() {
        ItemStack item = redMatterPickaxe.clone();
        updateLore(item);
        return item;
    }

    public ItemStack getRedMatterAxe() {
        ItemStack item = redMatterAxe.clone();
        updateLore(item);
        return item;
    }

    public ItemStack getRedMatterShovel() {
        ItemStack item = redMatterShovel.clone();
        updateLore(item);
        return item;
    }

    public ItemStack getRedMatterHoe() {
        ItemStack item = redMatterHoe.clone();
        updateLore(item);
        return item;
    }

    public ItemStack getRedMatterSword() {
        ItemStack item = redMatterSword.clone();
        updateLore(item);
        return item;
    }

    public ItemStack getRedMatterShears() {
        return redMatterShears.clone();
    }

    public ItemStack getRedMatterHammer() {
        return redMatterHammer.clone();
    }

    public ItemStack getRedMatterKatar() {
        ItemStack item = redMatterKatar.clone();
        updateLore(item);
        return item;
    }

    public ItemStack getRedMatterMorningstar() {
        ItemStack item = redMatterMorningstar.clone();
        updateLore(item);
        return item;
    }

    public boolean isDarkMatterPickaxe(ItemStack item) {
        return isTool(item, "dark_matter_pickaxe");
    }

    public boolean isDarkMatterAxe(ItemStack item) {
        return isTool(item, "dark_matter_axe");
    }

    public boolean isDarkMatterShovel(ItemStack item) {
        return isTool(item, "dark_matter_shovel");
    }

    public boolean isDarkMatterHoe(ItemStack item) {
        return isTool(item, "dark_matter_hoe");
    }

    public boolean isDarkMatterSword(ItemStack item) {
        return isTool(item, "dark_matter_sword");
    }

    public boolean isDarkMatterShears(ItemStack item) {
        return isTool(item, "dark_matter_shears");
    }

    public boolean isDarkMatterHammer(ItemStack item) {
        return isTool(item, "dark_matter_hammer");
    }

    public boolean isDarkMatterTool(ItemStack item) {
        String id = getToolId(item);
        return id != null && id.startsWith("dark_matter_");
    }

    public boolean isRedMatterPickaxe(ItemStack item) {
        return isTool(item, "red_matter_pickaxe");
    }

    public boolean isRedMatterAxe(ItemStack item) {
        return isTool(item, "red_matter_axe");
    }

    public boolean isRedMatterShovel(ItemStack item) {
        return isTool(item, "red_matter_shovel");
    }

    public boolean isRedMatterHoe(ItemStack item) {
        return isTool(item, "red_matter_hoe");
    }

    public boolean isRedMatterSword(ItemStack item) {
        return isTool(item, "red_matter_sword");
    }

    public boolean isRedMatterShears(ItemStack item) {
        return isTool(item, "red_matter_shears");
    }

    public boolean isRedMatterHammer(ItemStack item) {
        return isTool(item, "red_matter_hammer");
    }

    public boolean isRedMatterKatar(ItemStack item) {
        return isTool(item, "red_matter_katar");
    }

    public boolean isRedMatterMorningstar(ItemStack item) {
        return isTool(item, "red_matter_morningstar");
    }

    public boolean isRedMatterTool(ItemStack item) {
        String id = getToolId(item);
        return id != null && id.startsWith("red_matter_");
    }

    public boolean isProjectETool(ItemStack item) {
        String id = getToolId(item);
        return id != null && (id.startsWith("dark_matter_") || id.startsWith("red_matter_"));
    }

    private boolean isTool(ItemStack item, String id) {
        if (item == null || !item.hasItemMeta()) return false;
        String foundId = getToolId(item);
        return id.equals(foundId);
    }

    public String getToolId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(new NamespacedKey(plugin, "projecte_id"), PersistentDataType.STRING);
    }

    public int getCharge(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.getOrDefault(Constants.CHARGE_KEY, PersistentDataType.INTEGER, 0);
    }

    public int getSwordMode(ItemStack sword) {
        if (sword == null || !sword.hasItemMeta()) return 0;
        ItemMeta meta = sword.getItemMeta();
        if (meta == null) return 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.getOrDefault(new NamespacedKey(plugin, "projecte_sword_mode"), PersistentDataType.INTEGER, 0);
    }

    public boolean isValidMaterialForTool(Material blockMaterial, ItemStack tool) {
        String id = getToolId(tool);
        if (id == null) return false;

        if (id.endsWith("_sword") || id.endsWith("_hoe") || id.endsWith("_shears")) {
            return false;
        }

        if (id.endsWith("_pickaxe") || id.endsWith("_hammer")) {
            return Tag.MINEABLE_PICKAXE.isTagged(blockMaterial);
        }

        if (id.endsWith("_axe")) {
            return Tag.MINEABLE_AXE.isTagged(blockMaterial);
        }

        if (id.endsWith("_shovel")) {
            return Tag.MINEABLE_SHOVEL.isTagged(blockMaterial);
        }

        return false;
    }

    public void updateToolEfficiency(ItemStack tool) {
        if (tool == null || !tool.hasItemMeta()) {
            return;
        }

        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            return;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        int charge = container.getOrDefault(Constants.CHARGE_KEY, PersistentDataType.INTEGER, 0);

        int efficiencyLevel;
        if (isRedMatterTool(tool)) {
            efficiencyLevel = 7 + (charge * 2);
        } else {
            efficiencyLevel = 5 + (charge * 2);
        }

        Enchantment efficiencyEnchantment = Enchantment.getByKey(NamespacedKey.minecraft("efficiency"));
        if (efficiencyEnchantment == null) {
            efficiencyEnchantment = Enchantment.getByName("EFFICIENCY");
            if (efficiencyEnchantment == null) {
                plugin.getLogger().warning("Could not find the Efficiency enchantment.");
                return;
            }
        }

        if (meta.hasEnchant(efficiencyEnchantment)) {
            meta.removeEnchant(efficiencyEnchantment);
        }

        String id = getToolId(tool);
        if (id != null && (id.endsWith("_sword") || id.endsWith("_hoe") || id.endsWith("_shears"))) {
            tool.setItemMeta(meta);
            return;
        }

        if (id != null && id.endsWith("_hammer")) {
            Enchantment silkTouchEnchant = Enchantment.getByKey(NamespacedKey.minecraft("silk_touch"));
            if (silkTouchEnchant == null) {
                silkTouchEnchant = Enchantment.getByName("SILK_TOUCH");
            }
            if (silkTouchEnchant != null && meta.hasEnchant(silkTouchEnchant)) {
                meta.removeEnchant(silkTouchEnchant);
            }
        }

        meta.addEnchant(efficiencyEnchantment, efficiencyLevel, true);
        tool.setItemMeta(meta);
    }

    public void updateHammerAttackDamage(ItemStack hammer) {
        if (!isRedMatterHammer(hammer)) return;

        ItemMeta meta = hammer.getItemMeta();
        if (meta == null) return;

        meta.removeAttributeModifier(Attribute.valueOf("GENERIC_ATTACK_DAMAGE"));

        int charge = getCharge(hammer);
        double baseDamage = 15;
        double newDamage = baseDamage + charge;

        AttributeModifier damageModifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.attack_damage",
                newDamage - 1,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND);
        meta.addAttributeModifier(Attribute.valueOf("GENERIC_ATTACK_DAMAGE"), damageModifier);

        hammer.setItemMeta(meta);
    }

    public void updateSwordAttackDamage(ItemStack sword) {
        if (!isDarkMatterSword(sword) && !isRedMatterSword(sword)) return;

        ItemMeta meta = sword.getItemMeta();
        if (meta == null) return;

        meta.removeAttributeModifier(Attribute.valueOf("GENERIC_ATTACK_DAMAGE"));

        int charge = getCharge(sword);
        double baseDamage;
        if (isDarkMatterSword(sword)) {
            baseDamage = 13;
        } else {
            baseDamage = 14;
        }
        double newDamage = baseDamage + charge;

        AttributeModifier damageModifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.attack_damage",
                newDamage - 1,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND);
        meta.addAttributeModifier(Attribute.valueOf("GENERIC_ATTACK_DAMAGE"), damageModifier);

        sword.setItemMeta(meta);
    }

    public void updateKatarAttackDamage(ItemStack katar) {
        if (!isRedMatterKatar(katar)) return;

        ItemMeta meta = katar.getItemMeta();
        if (meta == null) return;

        meta.removeAttributeModifier(Attribute.valueOf("GENERIC_ATTACK_DAMAGE"));

        int charge = getCharge(katar);
        double baseDamage = 24;
        double newDamage = baseDamage + charge;

        AttributeModifier damageModifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.attack_damage",
                newDamage - 1,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND);
        meta.addAttributeModifier(Attribute.valueOf("GENERIC_ATTACK_DAMAGE"), damageModifier);

        katar.setItemMeta(meta);
    }

    public boolean isTool(String id) {
        if (id == null) return false;
        switch (id) {
            case "dark_matter_pickaxe":
            case "dark_matter_axe":
            case "dark_matter_shovel":
            case "dark_matter_hoe":
            case "dark_matter_sword":
            case "dark_matter_shears":
            case "dark_matter_hammer":
            case "red_matter_pickaxe":
            case "red_matter_axe":
            case "red_matter_shovel":
            case "red_matter_hoe":
            case "red_matter_sword":
            case "red_matter_shears":
            case "red_matter_hammer":
            case "red_matter_katar":
            case "red_matter_morningstar":
                return true;
            default:
                return false;
        }
    }

    public ItemStack getTool(String id) {
        if (id == null) return null;
        switch (id) {
            case "dark_matter_pickaxe":
                return getDarkMatterPickaxe();
            case "dark_matter_axe":
                return getDarkMatterAxe();
            case "dark_matter_shovel":
                return getDarkMatterShovel();
            case "dark_matter_hoe":
                return getDarkMatterHoe();
            case "dark_matter_sword":
                return getDarkMatterSword();
            case "dark_matter_shears":
                return getDarkMatterShears();
            case "dark_matter_hammer":
                return getDarkMatterHammer();
            case "red_matter_pickaxe":
                return getRedMatterPickaxe();
            case "red_matter_axe":
                return getRedMatterAxe();
            case "red_matter_shovel":
                return getRedMatterShovel();
            case "red_matter_hoe":
                return getRedMatterHoe();
            case "red_matter_sword":
                return getRedMatterSword();
            case "red_matter_shears":
                return getRedMatterShears();
            case "red_matter_hammer":
                return getRedMatterHammer();
            case "red_matter_katar":
                return getRedMatterKatar();
            case "red_matter_morningstar":
                return getRedMatterMorningstar();
            default:
                return null;
        }
    }

    public List<String> getToolIds() {
        return new ArrayList<>(List.of(
                "dark_matter_pickaxe",
                "dark_matter_axe",
                "dark_matter_shovel",
                "dark_matter_hoe",
                "dark_matter_sword",
                "dark_matter_shears",
                "dark_matter_hammer",
                "red_matter_pickaxe",
                "red_matter_axe",
                "red_matter_shovel",
                "red_matter_hoe",
                "red_matter_sword",
                "red_matter_shears",
                "red_matter_hammer",
                "red_matter_katar",
                "red_matter_morningstar"));
    }
}
