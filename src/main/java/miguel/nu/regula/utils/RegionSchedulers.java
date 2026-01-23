package miguel.nu.regula.utils;

import miguel.nu.regula.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class RegionSchedulers {

    private RegionSchedulers() {}

    private static Plugin plugin() {
        return Objects.requireNonNull(Main.plugin, "Main.plugin not initialized");
    }

    public static void runGlobal(Runnable r) {
        Objects.requireNonNull(r, "r");
        Plugin p = plugin();

        try {
            Object global = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            Method run = global.getClass().getMethod("run", Plugin.class, java.util.function.Consumer.class);
            run.invoke(global, p, (java.util.function.Consumer<Object>) task -> r.run());
            return;
        } catch (Throwable ignored) {}

        Bukkit.getScheduler().runTask(p, r);
    }

    public static void runAsync(Runnable r) {
        Objects.requireNonNull(r, "r");
        Plugin p = plugin();

        try {
            Object async = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
            Method runNow = async.getClass().getMethod("runNow", Plugin.class, java.util.function.Consumer.class);
            runNow.invoke(async, p, (java.util.function.Consumer<Object>) task -> r.run());
            return;
        } catch (Throwable ignored) {}

        Bukkit.getScheduler().runTaskAsynchronously(p, r);
    }

    public static void runOnEntity(Entity entity, Runnable r) {
        runOnEntityDelayed(entity, r, 1L);
    }

    public static void runOnEntityDelayed(Entity entity, Runnable r, long delayTicks) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(r, "r");
        Plugin p = plugin();

        long safeDelay = Math.max(1L, delayTicks);

        try {
            Method getScheduler = entity.getClass().getMethod("getScheduler");
            Object scheduler = getScheduler.invoke(entity);
            Method execute = scheduler.getClass().getMethod(
                    "execute",
                    Plugin.class,
                    Runnable.class,
                    Runnable.class,
                    long.class
            );
            execute.invoke(scheduler, p, r, (Runnable) null, safeDelay);
            return;
        } catch (Throwable ignored) {}

        Bukkit.getScheduler().runTaskLater(p, r, safeDelay);
    }

    public static void runOnRegion(Location loc, Runnable r) {
        Objects.requireNonNull(loc, "loc");
        Objects.requireNonNull(r, "r");
        Plugin p = plugin();

        try {
            World w = Objects.requireNonNull(loc.getWorld(), "loc.world");
            int chunkX = loc.getBlockX() >> 4;
            int chunkZ = loc.getBlockZ() >> 4;

            Object region = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
            Method execute = region.getClass().getMethod(
                    "execute",
                    Plugin.class,
                    World.class,
                    int.class,
                    int.class,
                    Runnable.class
            );
            execute.invoke(region, p, w, chunkX, chunkZ, r);
            return;
        } catch (Throwable ignored) {}

        Bukkit.getScheduler().runTask(p, r);
    }

    public static Object runAsyncAtFixedRate(Runnable r, long initialDelayTicks, long periodTicks) {
        Objects.requireNonNull(r, "r");
        Plugin p = plugin();

        long initialMs = Math.max(0L, initialDelayTicks) * 50L;
        long periodMs = Math.max(1L, periodTicks) * 50L;

        try {
            Object async = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
            Method runAtFixedRate = async.getClass().getMethod(
                    "runAtFixedRate",
                    Plugin.class,
                    java.util.function.Consumer.class,
                    long.class,
                    long.class,
                    TimeUnit.class
            );
            return runAtFixedRate.invoke(async, p, (java.util.function.Consumer<Object>) task -> r.run(), initialMs, periodMs, TimeUnit.MILLISECONDS);
        } catch (Throwable ignored) {}

        return Bukkit.getScheduler().scheduleSyncRepeatingTask(p, r, initialDelayTicks, periodTicks);
    }

    public static Object runGlobalAtFixedRate(Runnable r, long initialDelayTicks, long periodTicks) {
        Objects.requireNonNull(r, "r");
        Plugin p = plugin();

        try {
            Object global = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            Method runAtFixedRate = global.getClass().getMethod(
                    "runAtFixedRate",
                    Plugin.class,
                    java.util.function.Consumer.class,
                    long.class,
                    long.class
            );

            return runAtFixedRate.invoke(global, p, (java.util.function.Consumer<Object>) task -> r.run(), initialDelayTicks, periodTicks);
        } catch (Throwable ignored) {}

        return Bukkit.getScheduler().scheduleSyncRepeatingTask(p, r, initialDelayTicks, periodTicks);
    }


    public static void cancelRepeating(Object handle) {
        if (handle == null) return;

        if (handle instanceof Integer id) {
            Bukkit.getScheduler().cancelTask(id);
            return;
        }

        try {
            Method cancel = handle.getClass().getMethod("cancel");
            cancel.invoke(handle);
        } catch (Throwable ignored) {}
    }
}