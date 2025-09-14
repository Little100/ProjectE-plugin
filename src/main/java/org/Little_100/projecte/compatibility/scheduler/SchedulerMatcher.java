package org.Little_100.projecte.compatibility.scheduler;

import org.Little_100.projecte.ProjectE;

class SchedulerMatcher {
    private static final SchedulerAdapter adapter;

    static {
        boolean folia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            folia = true;
        } catch (ClassNotFoundException e) {
            // Not a Folia server
        }

        if (folia) {
            adapter = new FoliaSchedulerAdapter(ProjectE.getInstance());
        } else {
            adapter = new SpigotSchedulerAdapter(ProjectE.getInstance());
        }
    }

    public static SchedulerAdapter getSchedulerAdapter() {
        return adapter;
    }
}
