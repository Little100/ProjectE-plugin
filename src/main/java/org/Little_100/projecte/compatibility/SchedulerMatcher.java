package org.Little_100.projecte.compatibility;

import org.bukkit.plugin.java.JavaPlugin;

public class SchedulerMatcher {

    private static final boolean FOLIA;

    static {
        boolean folia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            folia = true;
        } catch (ClassNotFoundException e) {
            // Not a Folia server
        }
        FOLIA = folia;
    }

    public static SchedulerAdapter getSchedulerAdapter(JavaPlugin plugin) {
        if (FOLIA) {
            return new FoliaSchedulerAdapter(plugin);
        } else {
            return new SpigotSchedulerAdapter(plugin);
        }
    }
}