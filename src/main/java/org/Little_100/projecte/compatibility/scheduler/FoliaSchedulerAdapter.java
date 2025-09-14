package org.Little_100.projecte.compatibility.scheduler;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public class FoliaSchedulerAdapter implements SchedulerAdapter {

    private final JavaPlugin plugin;

    public FoliaSchedulerAdapter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private Method findMethodByNameAndParamCount(Class<?> clazz, String name, int paramCount)
            throws NoSuchMethodException {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == paramCount) {
                return method;
            }
        }
        throw new NoSuchMethodException(
                "Could not find method " + name + " with " + paramCount + " parameters on class " + clazz.getName());
    }

    @Override
    public void runTask(Runnable task) {
        try {
            Method getGlobalRegionScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler");
            Object scheduler = getGlobalRegionScheduler.invoke(null);
            Method execute = findMethodByNameAndParamCount(scheduler.getClass(), "execute", 2);
            execute.invoke(scheduler, plugin, task);
        } catch (Exception e) {
            throw new RuntimeException("Folia scheduler reflection failed for runTask", e);
        }
    }

    @Override
    public void runTaskAsynchronously(Runnable task) {
        try {
            Method getAsyncScheduler = Bukkit.class.getMethod("getAsyncScheduler");
            Object scheduler = getAsyncScheduler.invoke(null);
            Method runNow = findMethodByNameAndParamCount(scheduler.getClass(), "runNow", 2);
            runNow.invoke(scheduler, plugin, (Consumer<Object>) scheduledTask -> task.run());
        } catch (Exception e) {
            throw new RuntimeException("Folia scheduler reflection failed for runTaskAsynchronously", e);
        }
    }

    @Override
    public void runTaskLater(Runnable task, long delay) {
        try {
            Method getGlobalRegionScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler");
            Object scheduler = getGlobalRegionScheduler.invoke(null);
            Method runDelayed = findMethodByNameAndParamCount(scheduler.getClass(), "runDelayed", 3);
            runDelayed.invoke(scheduler, plugin, (Consumer<Object>) scheduledTask -> task.run(), delay);
        } catch (Exception e) {
            throw new RuntimeException("Folia scheduler reflection failed for runTaskLater", e);
        }
    }

    @Override
    public void runTaskAt(Location location, Runnable task) {
        try {
            Method getRegionScheduler = Bukkit.class.getMethod("getRegionScheduler");
            Object scheduler = getRegionScheduler.invoke(null);
            Method execute = findMethodByNameAndParamCount(scheduler.getClass(), "execute", 3);
            execute.invoke(scheduler, plugin, location, task);
        } catch (Exception e) {
            throw new RuntimeException("Folia scheduler reflection failed for runTaskAt", e);
        }
    }

    @Override
    public void runTaskOnEntity(Entity entity, Runnable task) {
        try {
            Method getScheduler = Entity.class.getMethod("getScheduler");
            Object scheduler = getScheduler.invoke(entity);
            Method run = findMethodByNameAndParamCount(scheduler.getClass(), "run", 3);
            run.invoke(scheduler, plugin, (Consumer<Object>) scheduledTask -> task.run(), null);
        } catch (Exception e) {
            throw new RuntimeException("Folia scheduler reflection failed for runTaskOnEntity", e);
        }
    }

    @Override
    public void runTimer(Runnable task, long delay, long period) {
        try {
            Method getGlobalRegionScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler");
            Object scheduler = getGlobalRegionScheduler.invoke(null);
            Method runAtFixedRate = findMethodByNameAndParamCount(scheduler.getClass(), "runAtFixedRate", 4);
            long foliaDelay = Math.max(1, delay);
            runAtFixedRate.invoke(
                    scheduler, plugin, (Consumer<Object>) scheduledTask -> task.run(), foliaDelay, period);
        } catch (Exception e) {
            throw new RuntimeException("Folia scheduler reflection failed for runTimer", e);
        }
    }

    @Override
    public void runTaskLaterAtLocation(Location location, Runnable task, long delay) {
        try {
            Method getRegionScheduler = Bukkit.class.getMethod("getRegionScheduler");
            Object scheduler = getRegionScheduler.invoke(null);
            Method runDelayed = findMethodByNameAndParamCount(scheduler.getClass(), "runDelayed", 4);
            runDelayed.invoke(scheduler, plugin, location, (Consumer<Object>) scheduledTask -> task.run(), delay);
        } catch (Exception e) {
            throw new RuntimeException("Folia scheduler reflection failed for runTaskLaterAtLocation", e);
        }
    }

    @Override
    public void runTaskLaterOnEntity(Entity entity, Runnable task, long delay) {
        try {
            Method getScheduler = Entity.class.getMethod("getScheduler");
            Object scheduler = getScheduler.invoke(entity);
            Method runDelayed = scheduler
                    .getClass()
                    .getMethod(
                            "runDelayed",
                            org.bukkit.plugin.Plugin.class,
                            java.util.function.Consumer.class,
                            Runnable.class,
                            long.class);
            runDelayed.invoke(scheduler, plugin, (Consumer<Object>) scheduledTask -> task.run(), null, delay);
        } catch (Exception e) {
            throw new RuntimeException("Folia scheduler reflection failed for runTaskLaterOnEntity", e);
        }
    }
}
