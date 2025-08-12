package org.Little_100.projecte;

import org.bukkit.ChatColor;

import java.util.Map;

public class DebugManager {

    private static ProjectE plugin;

    public static void init(ProjectE plugin) {
        DebugManager.plugin = plugin;
    }

    public static void log(String message) {
        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[ProjectE Debug] " + message);
        }
    }

    public static void log(String key, Map<String, String> placeholders) {
        if (plugin.getConfig().getBoolean("debug")) {
            String message = plugin.getLanguageManager().get(key, placeholders);
            log(message);
        }
    }
}