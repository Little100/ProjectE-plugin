package org.Little_100.projecte.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotSchedulerAdapter implements SchedulerAdapter {

    private final JavaPlugin plugin;

    public SpigotSchedulerAdapter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runTask(Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public void runTaskAsynchronously(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public void runTaskLater(Runnable task, long delay) {
        Bukkit.getScheduler().runTaskLater(plugin, task, delay);
    }

    @Override
    public void runTaskAtLocation(Location location, Runnable task) {
        runTask(task);
    }

    @Override
    public void runTaskOnEntity(Entity entity, Runnable task) {
        runTask(task);
    }

    @Override
    public void runTimer(Runnable task, long delay, long period) {
        Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
    }
}