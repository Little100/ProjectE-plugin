package org.Little_100.projecte.compatibility.scheduler;

import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
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
    public void runTaskAt(Location location, Runnable task) {
        runTask(task);
    }

    @Override
    public void runTaskOnEntity(Entity entity, Runnable task) {
        try {
            Method schedulingMethod = Entity.class.getMethod("scheduling");
            Object entityScheduler = schedulingMethod.invoke(entity);
            Method runMethod =
                    entityScheduler.getClass().getMethod("run", Plugin.class, Runnable.class, Runnable.class);
            runMethod.invoke(entityScheduler, plugin, task, null);
        } catch (Exception e) {
            runTask(task);
        }
    }

    @Override
    public void runTimer(Runnable task, long delay, long period) {
        Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
    }

    @Override
    public void runTaskLaterAtLocation(Location location, Runnable task, long delay) {
        Bukkit.getScheduler().runTaskLater(plugin, task, delay);
    }

    @Override
    public void runTaskLaterOnEntity(Entity entity, Runnable task, long delay) {
        Bukkit.getScheduler().runTaskLater(plugin, task, delay);
    }
}
