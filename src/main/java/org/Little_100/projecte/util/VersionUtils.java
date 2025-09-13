package org.Little_100.projecte.util;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Bukkit;

public class VersionUtils {
    public static boolean is1214OrNewer() {
        String version = Bukkit.getServer().getBukkitVersion().split("-")[0];
        String[] versionParts = version.split("\\.");
        if (versionParts.length >= 2) {
            int major = Integer.parseInt(versionParts[0]);
            int minor = Integer.parseInt(versionParts[1]);
            if (major > 1)
                return true;
            if (major == 1 && minor > 21)
                return true;
            if (major == 1 && minor == 21 && versionParts.length >= 3) {
                int patch = Integer.parseInt(versionParts[2]);
                return patch >= 4;
            }
        }
        return false;
    }

    public static boolean isVersionOrNewer(String serverVersion, String targetVersion) {
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
