package org.Little_100.projecte.compatibility;

import java.util.logging.Level;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.util.VersionUtils;

public class VersionMatcher {

    private static VersionAdapter adapter;

    public static VersionAdapter getAdapter() {
        if (adapter == null) {
            ProjectE plugin = ProjectE.getInstance();
            plugin.getLogger().info("Detected Server Version: " + VersionUtils.getMCVersion());

            try {
                if (VersionUtils.isVersionOrNewer("1.13")) {
                    adapter = new ModernAdapter();
                } else {
                    adapter = new LegacyAdapter(); // 为什么一个1.14+的插件要考虑这个？
                }
                plugin.getLogger()
                        .info("Loaded Compatibility Adapter: "
                                + adapter.getClass().getSimpleName());
            } catch (Exception e) {
                plugin.getLogger()
                        .log(Level.SEVERE, "Could not find a compatible version adapter for your server version!", e);
                e.printStackTrace();
            }
        }
        return adapter;
    }
}
