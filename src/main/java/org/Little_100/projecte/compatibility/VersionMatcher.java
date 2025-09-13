package org.Little_100.projecte.compatibility;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.util.VersionUtils;
import org.bukkit.Bukkit;

public class VersionMatcher {

    private static VersionAdapter adapter;

    public static VersionAdapter getAdapter() {
        if (adapter == null) {
            ProjectE plugin = ProjectE.getInstance();
            String version = Bukkit.getServer().getBukkitVersion().split("-")[0];
            plugin.getLogger().info("Detected Server Version: " + version);

            try {
                if (VersionUtils.isVersionOrNewer(version, "1.13")) {
                    adapter = new ModernAdapter();
                } else {
                    adapter = new LegacyAdapter(); //为什么一个1.14+的插件要考虑这个？
                }
                plugin.getLogger().info("Loaded Compatibility Adapter: " + adapter.getClass().getSimpleName());
            } catch (Exception e) {
                plugin.getLogger().severe("Could not find a compatible version adapter for your server version!");
                e.printStackTrace();
            }
        }
        return adapter;
    }
}