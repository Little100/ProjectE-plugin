package org.Little_100.projecte.devices;

import org.Little_100.projecte.ProjectE;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;

public class DeviceManager {

    private final ProjectE plugin;
    private final NamespacedKey darkMatterFurnaceKey;
    private final NamespacedKey redMatterFurnaceKey;
    private final NamespacedKey alchemicalChestKey;
    private final NamespacedKey energyCondenserKey;
    private final NamespacedKey energyCondenserMK2Key;
    private DarkMatterFurnace darkMatterFurnace;
    private RedMatterFurnace redMatterFurnace;
    private AlchemicalChest alchemicalChest;
    private EnergyCondenser energyCondenser;
    private EnergyCondenserMK2 energyCondenserMK2;

    public DeviceManager(ProjectE plugin) {
        this.plugin = plugin;
        this.darkMatterFurnaceKey = new NamespacedKey(plugin, "dark_matter_furnace");
        this.redMatterFurnaceKey = new NamespacedKey(plugin, "red_matter_furnace");
        this.alchemicalChestKey = new NamespacedKey(plugin, "alchemical_chest");
        this.energyCondenserKey = new NamespacedKey(plugin, "energy_condenser");
        this.energyCondenserMK2Key = new NamespacedKey(plugin, "energy_condenser_mk2");
    }

    public boolean isDevice(Block block) {
        if (block.getType() != org.bukkit.Material.BEACON) return false;

        Collection<Entity> nearbyEntities = block.getWorld().getNearbyEntities(
                block.getLocation().add(0.5, 0.5, 0.5), 0.5, 1.0, 0.5,
                entity -> entity instanceof ArmorStand
        );

        for (Entity entity : nearbyEntities) {
            if (entity.getPersistentDataContainer().has(darkMatterFurnaceKey, PersistentDataType.BYTE) ||
                entity.getPersistentDataContainer().has(redMatterFurnaceKey, PersistentDataType.BYTE) ||
                entity.getPersistentDataContainer().has(alchemicalChestKey, PersistentDataType.BYTE) ||
                entity.getPersistentDataContainer().has(energyCondenserKey, PersistentDataType.BYTE) ||
                entity.getPersistentDataContainer().has(energyCondenserMK2Key, PersistentDataType.BYTE)) {
                return true;
            }
        }
        return false;
    }

    public void registerDevices() {
        darkMatterFurnace = new DarkMatterFurnace(plugin);
        redMatterFurnace = new RedMatterFurnace(plugin);
        alchemicalChest = new AlchemicalChest(plugin);
        energyCondenser = new EnergyCondenser(plugin);
        energyCondenserMK2 = new EnergyCondenserMK2(plugin);

        plugin.getServer().getPluginManager().registerEvents(darkMatterFurnace, plugin);
        plugin.getServer().getPluginManager().registerEvents(redMatterFurnace, plugin);
        plugin.getServer().getPluginManager().registerEvents(alchemicalChest, plugin);
        plugin.getServer().getPluginManager().registerEvents(energyCondenser, plugin);
        plugin.getServer().getPluginManager().registerEvents(energyCondenserMK2, plugin);
    }

    public void reloadDeviceItems() {
        if (darkMatterFurnace != null) {
            darkMatterFurnace = new DarkMatterFurnace(plugin);
        }
        if (redMatterFurnace != null) {
            redMatterFurnace = new RedMatterFurnace(plugin);
        }
        if (alchemicalChest != null) {
            alchemicalChest = new AlchemicalChest(plugin);
        }
        if (energyCondenser != null) {
            energyCondenser = new EnergyCondenser(plugin);
        }
        if (energyCondenserMK2 != null) {
            energyCondenserMK2 = new EnergyCondenserMK2(plugin);
        }
    }

    public ItemStack getDarkMatterFurnaceItem() {
        if (darkMatterFurnace == null) {
            return null;
        }
        return darkMatterFurnace.getFurnaceItem();
    }

    public ItemStack getRedMatterFurnaceItem() {
        if (redMatterFurnace == null) {
            return null;
        }
        return redMatterFurnace.getFurnaceItem();
    }

    public ItemStack getAlchemicalChestItem() {
        if (alchemicalChest == null) {
            return null;
        }
        return alchemicalChest.getChestItem();
    }

    public ItemStack getEnergyCondenserItem() {
        if (energyCondenser == null) {
            return null;
        }
        return energyCondenser.getCondenserItem();
    }

    public ItemStack getEnergyCondenserMK2Item() {
        if (energyCondenserMK2 == null) {
            return null;
        }
        return energyCondenserMK2.getCondenserMK2Item();
    }

    public DarkMatterFurnace getDarkMatterFurnace() {
        return darkMatterFurnace;
    }

    public RedMatterFurnace getRedMatterFurnace() {
        return redMatterFurnace;
    }

    public AlchemicalChest getAlchemicalChest() {
        return alchemicalChest;
    }

    public EnergyCondenser getEnergyCondenser() {
        return energyCondenser;
    }

    public EnergyCondenserMK2 getEnergyCondenserMK2() {
        return energyCondenserMK2;
    }
}