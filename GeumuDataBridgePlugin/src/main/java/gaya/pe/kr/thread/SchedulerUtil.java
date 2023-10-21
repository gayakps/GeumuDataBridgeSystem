package gaya.pe.kr.thread;

import gaya.pe.kr.core.GeumuDataBridgePlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class SchedulerUtil {

    private static final BukkitScheduler BUKKIT_SCHEDULER = Bukkit.getScheduler();

    public static void cancel(int taskId) {
        BUKKIT_SCHEDULER.cancelTask(taskId);
    }

    static Plugin plugin = GeumuDataBridgePlugin.getPlugin();

    public static int scheduleRepeatingTask(final Runnable task, int delay, int interval) {

        return BUKKIT_SCHEDULER.scheduleSyncRepeatingTask(plugin, task, delay, interval);
    }

    public static void runLaterTask(final Runnable task, int delay) {
        BUKKIT_SCHEDULER.runTaskLater(plugin, task, delay);
    }

    public static void runWaitTask(final Runnable task) {
        Thread thread = new Thread(task);
        thread.start();
    }

    public static void runTaskAsync(final Runnable task) {
        BUKKIT_SCHEDULER.runTaskAsynchronously(plugin, task);
    }



}
