package org.Little_100.projecte.compatibility;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Bukkit;

public class VersionMatcher {

    private static VersionAdapter adapter;

    public static VersionAdapter getAdapter() {
        if (adapter == null) {
            ProjectE plugin = ProjectE.getInstance();
            String version = Bukkit.getServer().getBukkitVersion().split("-")[0];
            plugin.getLogger().info("Detected Server Version: " + version);

            try {
                if (isVersionOrNewer(version, "1.13")) {
                    adapter = new ModernAdapter();
                } else {
                    adapter = new LegacyAdapter();
                }
                plugin.getLogger().info("Loaded Compatibility Adapter: " + adapter.getClass().getSimpleName());
            } catch (Exception e) {
                plugin.getLogger().severe("Could not find a compatible version adapter for your server version!");
                e.printStackTrace();
            }
        }
        return adapter;
    }

    private static boolean isVersionOrNewer(String serverVersion, String targetVersion) {
        String[] serverParts = serverVersion.split("\\.");
        String[] targetParts = targetVersion.split("\\.");

        int length = Math.max(serverParts.length, targetParts.length);
        for (int i = 0; i < length; i++) {
            int serverPart = i < serverParts.length ? Integer.parseInt(serverParts[i]) : 0;
            int targetPart = i < targetParts.length ? Integer.parseInt(targetParts[i]) : 0;
            if (serverPart > targetPart) {
                return true;
            }
            if (serverPart < targetPart) {
                return false;
            }
        }
        return true;
    }
}