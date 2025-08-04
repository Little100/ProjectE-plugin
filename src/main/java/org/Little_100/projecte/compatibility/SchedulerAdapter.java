package org.Little_100.projecte.compatibility;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface SchedulerAdapter {
    void runTask(Runnable task);
    void runTaskAsynchronously(Runnable task);
    void runTaskLater(Runnable task, long delay);
    void runTaskAtLocation(Location location, Runnable task);
    void runTaskOnEntity(Entity entity, Runnable task);
    void runTimer(Runnable task, long delay, long period);
}