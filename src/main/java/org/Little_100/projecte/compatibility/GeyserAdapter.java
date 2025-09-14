package org.Little_100.projecte.compatibility;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.Little_100.projecte.ProjectE;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;

public class GeyserAdapter {

    private final ProjectE plugin;
    private final Map<String, Map<String, Object>> customItemMappings = new HashMap<>();
    private boolean geyserApiAvailable = false;

    public GeyserAdapter(ProjectE plugin) {
        this.plugin = plugin;
        try {
            Class.forName("org.geysermc.geyser.api.GeyserApi");
            geyserApiAvailable = true;
            plugin.getLogger().info("Geyser API found, enabling Geyser compatibility.");
            loadCustomItemMappings();
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("Geyser API not found, Geyser compatibility is disabled.");
        }
    }

    private void loadCustomItemMappings() {
        File mappingFile = new File(plugin.getDataFolder(), "mapping.yml");
        if (!mappingFile.exists()) {
            plugin.saveResource("mapping.yml", false);
        }

        FileConfiguration mappingConfig = YamlConfiguration.loadConfiguration(mappingFile);
        ConfigurationSection mappingsSection = mappingConfig.getConfigurationSection("geyser-mappings");
        if (mappingsSection == null) {
            plugin.getLogger()
                    .warning("'geyser-mappings' section not found in mapping.yml. Geyser item mapping will not work.");
            return;
        }

        for (String key : mappingsSection.getKeys(false)) {
            ConfigurationSection itemSection = mappingsSection.getConfigurationSection(key);
            if (itemSection != null) {
                Map<String, Object> mapping = new HashMap<>();
                mapping.put("bedrock_identifier", itemSection.getString("bedrock_identifier"));
                mapping.put("custom_model_data", itemSection.getInt("custom_model_data"));
                customItemMappings.put(key, mapping);
                plugin.getLogger().info("Loaded Geyser mapping for '" + key + "'.");
            }
        }
    }

    public ItemStack convertToJavaStack(ItemStack itemStack, UUID playerUUID) {
        if (!geyserApiAvailable || itemStack == null) {
            return itemStack;
        }

        GeyserConnection connection = GeyserApi.api().connectionByUuid(playerUUID);
        if (connection == null) {
            return itemStack;
        }

        return itemStack;
    }

    public ItemStack convertToBedrockStack(ItemStack itemStack, UUID playerUUID) {
        return itemStack;
    }

    public boolean isGeyserApiAvailable() {
        return geyserApiAvailable;
    }
}
