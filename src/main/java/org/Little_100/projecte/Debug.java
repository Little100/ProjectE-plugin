package org.Little_100.projecte;

import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Debug {

    private static ProjectE plugin;

    public static void init(ProjectE plugin) {
        Debug.plugin = plugin;
    }

    public static void log(String message) {
        if (plugin.getConfig().getBoolean("debug")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[ProjectE Debug] " + message);
        }
    }

    public static void log(String key, Map<String, String> placeholders) {
        if (plugin.getConfig().getBoolean("debug")) {
            String message = plugin.getLanguageManager().get(key, placeholders);
            log(message);
        }
    }
}
