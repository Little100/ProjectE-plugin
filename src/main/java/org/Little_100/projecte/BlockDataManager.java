package org.Little_100.projecte;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class BlockDataManager {

    private final ProjectE plugin;
    private final File blockFile;
    private final FileConfiguration blockConfig;

    private final Map<String, ItemStack> customBlocks = new HashMap<>();
    private final Map<String, String> customBlockCommands = new HashMap<>();
    private final Map<String, Material> customBlockBaseBlocks = new HashMap<>();
    private final Map<ItemStack, String> itemToIdMap = new HashMap<>();

    public BlockDataManager(ProjectE plugin) {
        this.plugin = plugin;
        this.blockFile = new File(plugin.getDataFolder(), "Block.yml");
        if (!blockFile.exists()) {
            try {
                if (blockFile.createNewFile()) {
                    plugin.getLogger().info("Created empty Block.yml as it was not found.");
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create Block.yml", e);
            }
        }
        this.blockConfig = YamlConfiguration.loadConfiguration(blockFile);
        loadBlocks();
    }

    private void loadBlocks() {
        ConfigurationSection blocksSection = blockConfig.getConfigurationSection("blocks");
        if (blocksSection != null) {
            for (String id : blocksSection.getKeys(false)) {
                ConfigurationSection blockData = blocksSection.getConfigurationSection(id);
                if (blockData != null) {
                    ItemStack item = blockData.getItemStack("item");
                    if (item != null) {
                        customBlocks.put(id, item);
                        itemToIdMap.put(item.clone(), id); // Use a clone for the map key

                        String command = blockData.getString("command");
                        if (command != null && !command.isEmpty()) {
                            customBlockCommands.put(id, command);
                        }

                        String baseBlockStr = blockData.getString("base_block");
                        if (baseBlockStr != null) {
                            try {
                                Material baseBlock = Material.valueOf(baseBlockStr.toUpperCase());
                                customBlockBaseBlocks.put(id, baseBlock);
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("Invalid base_block material '" + baseBlockStr + "' for custom block '" + id + "'.");
                            }
                        }
                    } else {
                        plugin.getLogger().warning("Item is missing for custom block '" + id + "' in Block.yml.");
                    }
                }
            }
        }
    }

    public void saveBlock(String id, ItemStack item, String command) {
        customBlocks.put(id, item);
        itemToIdMap.put(item.clone(), id);
        blockConfig.set("blocks." + id + ".item", item);

        if (command != null) {
            customBlockCommands.put(id, command);
            blockConfig.set("blocks." + id + ".command", command);
        } else {
            customBlockCommands.remove(id);
            blockConfig.set("blocks." + id + ".command", null);
        }

        try {
            blockConfig.save(blockFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save Block.yml", e);
        }
    }


    public ItemStack getBlock(String id) {
        return customBlocks.get(id);
    }

    public String getBlockIdByItem(ItemStack item) {
        if (item == null) return null;
        for (Map.Entry<ItemStack, String> entry : itemToIdMap.entrySet()) {
            if (item.isSimilar(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    public ItemStack getBlockByItem(ItemStack item) {
        String id = getBlockIdByItem(item);
        return (id != null) ? getBlock(id) : null;
    }

    public String getPlaceCommand(String id) {
        return customBlockCommands.get(id);
    }

    public Material getBaseBlock(String id) {
        return customBlockBaseBlocks.get(id);
    }

    public Set<String> getAllBlockIds() {
        return customBlocks.keySet();
    }
}