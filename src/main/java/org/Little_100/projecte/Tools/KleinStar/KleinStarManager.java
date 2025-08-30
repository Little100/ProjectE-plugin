package org.Little_100.projecte.Tools.KleinStar;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.util.CustomModelDataUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KleinStarManager {

    private final ProjectE plugin;
    private final Map<Integer, ItemStack> kleinStars = new HashMap<>();
    private final Map<Integer, Long> kleinStarCapacities = new HashMap<>();
    private final NamespacedKey kleinStarKey;
    private final NamespacedKey storedEmcKey;
    private final NamespacedKey capacityKey;

    public KleinStarManager(ProjectE plugin) {
        this.plugin = plugin;
        this.kleinStarKey = new NamespacedKey(plugin, "klein_star_level");
        this.storedEmcKey = new NamespacedKey(plugin, "stored_emc");
        this.capacityKey = new NamespacedKey(plugin, "emc_capacity");
        initializeCapacities();
        initializeKleinStars();
    }

    private void initializeCapacities() {
        kleinStarCapacities.put(1, 50000L);
        kleinStarCapacities.put(2, 200000L);
        kleinStarCapacities.put(3, 800000L);
        kleinStarCapacities.put(4, 3200000L);
        kleinStarCapacities.put(5, 12800000L);
        kleinStarCapacities.put(6, 51200000L);
    }

    private void initializeKleinStars() {
        kleinStars.put(1, createKleinStar(1, "klein_star_ein"));
        kleinStars.put(2, createKleinStar(2, "klein_star_zwei"));
        kleinStars.put(3, createKleinStar(3, "klein_star_drei"));
        kleinStars.put(4, createKleinStar(4, "klein_star_vier"));
        kleinStars.put(5, createKleinStar(5, "klein_star_sphere"));
        kleinStars.put(6, createKleinStar(6, "klein_star_omega"));
    }

    private ItemStack createKleinStar(int level, String nameKey) {
        ItemStack kleinStar = new ItemStack(Material.DIAMOND_HOE);

        ItemMeta meta = kleinStar.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getLanguageManager().get("item." + nameKey + ".name"));
            List<String> lore = new ArrayList<>();
            lore.add(plugin.getLanguageManager().get("item." + nameKey + ".lore1"));
            lore.add(plugin.getLanguageManager().get("item." + nameKey + ".lore2").replace("{staremc}", "0"));
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            if (meta instanceof Damageable) {
                ((Damageable) meta).setDamage(kleinStar.getType().getMaxDurability() - 2);
            }
            kleinStar.setItemMeta(meta);
        }

        kleinStar = CustomModelDataUtil.setCustomModelDataBoth(kleinStar, "klein_star_" + level, level);

        ItemMeta finalMeta = kleinStar.getItemMeta();
        if (finalMeta != null) {
            PersistentDataContainer container = finalMeta.getPersistentDataContainer();
            long capacity = kleinStarCapacities.getOrDefault(level, 0L);
            container.set(kleinStarKey, PersistentDataType.INTEGER, level);
            container.set(storedEmcKey, PersistentDataType.LONG, 0L);
            container.set(capacityKey, PersistentDataType.LONG, capacity);
            kleinStar.setItemMeta(finalMeta);
        }

        return kleinStar;
    }

    public ItemStack getKleinStar(int level) {
        ItemStack original = kleinStars.get(level);
        if (original == null) return null;
        return new ItemStack(original);
    }

    public boolean isKleinStar(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(kleinStarKey, PersistentDataType.INTEGER);
    }

    public long getStoredEmc(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.getOrDefault(storedEmcKey, PersistentDataType.LONG, 0L);
    }

    public long getCapacity(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.getOrDefault(capacityKey, PersistentDataType.LONG, 0L);
    }

    public ItemStack setStoredEmc(ItemStack item, long emc) {
        if (item == null || !item.hasItemMeta()) return item;
        ItemStack newItem = new ItemStack(item);
        ItemMeta meta = newItem.getItemMeta();
        if (meta == null) return newItem;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(storedEmcKey, PersistentDataType.LONG, emc);
        newItem.setItemMeta(meta);
        updateLoreAndDurability(newItem);
        return newItem;
    }

    public void updateLoreAndDurability(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !isKleinStar(item)) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        long storedEmc = getStoredEmc(item);
        int level = getKleinStarLevel(item);
        if (level <= 0) return;

        List<String> lore = meta.getLore();
        if (lore != null && lore.size() >= 2) {
            Map<Integer, String> levelToNameKey = new HashMap<>();
            levelToNameKey.put(1, "klein_star_ein");
            levelToNameKey.put(2, "klein_star_zwei");
            levelToNameKey.put(3, "klein_star_drei");
            levelToNameKey.put(4, "klein_star_vier");
            levelToNameKey.put(5, "klein_star_sphere");
            levelToNameKey.put(6, "klein_star_omega");
            String nameKey = levelToNameKey.get(level);

            if (nameKey != null) {
                String lore2Template = plugin.getLanguageManager().get("item." + nameKey + ".lore2");
                if (lore2Template != null) {
                    lore.set(1, lore2Template.replace("{staremc}", String.format("%,d", storedEmc)));
                    meta.setLore(lore);
                }
            }
        }
        if (meta instanceof Damageable) {
            long capacity = getCapacity(item);
            int maxDurability = item.getType().getMaxDurability();
            int newDamage;

            if (storedEmc >= capacity) {
                newDamage = 1;
            } else {
                if (capacity > 0) {
                    double emcRatio = (double) storedEmc / capacity;
                    int variableDurability = maxDurability - 3;
                    int damageOffset = (int) Math.round(variableDurability * (1 - emcRatio));
                    newDamage = 2 + damageOffset;
                } else {
                    newDamage = maxDurability - 2;
                }
            }
            newDamage = Math.max(1, Math.min(newDamage, maxDurability - 1));
            ((Damageable) meta).setDamage(newDamage);
        }

        item.setItemMeta(meta);
    }

    public int getKleinStarLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return -1;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return -1;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(kleinStarKey, PersistentDataType.INTEGER)) {
            return -1;
        }
        return container.get(kleinStarKey, PersistentDataType.INTEGER);
    }

    public static boolean hasEnoughEMC(Player player, long amount) {
        long totalEmc = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && getInstance().isKleinStar(item)) {
                totalEmc += getInstance().getStoredEmc(item);
            }
        }
        return totalEmc >= amount;
    }

    public static void consumeEMC(Player player, long amount) {
        long remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && getInstance().isKleinStar(item)) {
                long stored = getInstance().getStoredEmc(item);
                if (stored >= remaining) {
                    player.getInventory().setItem(i, getInstance().setStoredEmc(item, stored - remaining));
                    remaining = 0;
                    break;
                } else {
                    player.getInventory().setItem(i, getInstance().setStoredEmc(item, 0));
                    remaining -= stored;
                }
            }
        }
    }

    public static boolean hasKleinStar(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && getInstance().isKleinStar(item)) {
                return true;
            }
        }
        return false;
    }

    private static KleinStarManager getInstance() {
        return ProjectE.getInstance().getKleinStarManager();
    }
}