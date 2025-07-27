package org.Little_100.projecte;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageManager {

    private final ProjectE plugin;
    private final List<FileConfiguration> langConfigs = new ArrayList<>();
    private final Pattern placeholderPattern = Pattern.compile("\\{([^}]+)}");

    public LanguageManager(ProjectE plugin) {
        this.plugin = plugin;
        loadLanguageFiles();
    }

    public void loadLanguageFiles() {
        langConfigs.clear();
        List<String> langNames = plugin.getConfig().getStringList("language");
        if (langNames.isEmpty()) {
            langNames.add("zh_cn"); // 默认中文
        }

        Collections.reverse(langNames);

        for (String lang : langNames) {
            File langFile = new File(plugin.getDataFolder(), lang + ".yml");
            if (!langFile.exists()) {
                plugin.saveResource(lang + ".yml", false);
            }
            FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);

            try (InputStream defLangStream = plugin.getResource(lang + ".yml")) {
                if (defLangStream != null) {
                    config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defLangStream, StandardCharsets.UTF_8)));
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Could not load language file: " + lang + ".yml");
                e.printStackTrace();
            }
            langConfigs.add(config);
        }
        Collections.reverse(langConfigs);
    }

    public String get(String key) {
        return get(key, new HashMap<>());
    }

    public String get(String key, Map<String, String> placeholders) {
        String message = null;
        for (FileConfiguration config : langConfigs) {
            message = config.getString(key);
            if (message != null) {
                break;
            }
        }

        if (message == null) {
            plugin.getLogger().warning("Missing translation for key: " + key);
            return key;
        }

        Matcher matcher = placeholderPattern.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String placeholderKey = matcher.group(1);
            String value = placeholders.getOrDefault(placeholderKey, matcher.group(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);

        return ChatColor.translateAlternateColorCodes('&', sb.toString().replace("§", "&"));
    }
}