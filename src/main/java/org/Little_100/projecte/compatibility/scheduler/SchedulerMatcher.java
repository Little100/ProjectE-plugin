package org.Little_100.projecte.compatibility.scheduler;

import org.Little_100.projecte.ProjectE;

class SchedulerMatcher {
    private static SchedulerAdapter adapter;

    public static void init(ProjectE plugin) {
        if (adapter != null) {
            return;
        }
        boolean folia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            folia = true;
        } catch (ClassNotFoundException e) {
            // Not a Folia server
        }

        if (folia) {
            adapter = new FoliaSchedulerAdapter(plugin);
        } else {
            adapter = new SpigotSchedulerAdapter(plugin);
        }
    }

    public static SchedulerAdapter getSchedulerAdapter() {
        if (adapter == null) {
            throw new IllegalStateException("SchedulerAdapter has not been initialized yet!");
        }
        return adapter;
    }
}
