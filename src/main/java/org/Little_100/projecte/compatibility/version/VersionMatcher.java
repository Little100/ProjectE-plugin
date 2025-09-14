package org.Little_100.projecte.compatibility.version;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.util.VersionUtils;

class VersionMatcher {
    private static final VersionAdapter adapter;

    static {
        if (VersionUtils.isVersionOrNewer("1.13")) {
            adapter = new ModernAdapter();
        } else {
            adapter = new LegacyAdapter(); // 为什么一个1.14+的插件要考虑这个？
        }

        ProjectE.getInstance()
                .getLogger()
                .info("Loaded Compatibility Adapter: " + adapter.getClass().getSimpleName() + " in minecraft version"
                        + VersionUtils.getMCVersion());
    }

    public static VersionAdapter getAdapter() {
        return adapter;
    }
}
