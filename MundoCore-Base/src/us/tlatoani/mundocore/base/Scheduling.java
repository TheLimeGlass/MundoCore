package us.tlatoani.mundocore.base;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Tlatoani on 8/10/17.
 */
public class Scheduling {

    public static BukkitScheduler getScheduler() {
        return Bukkit.getScheduler();
    }

    public static void sync(Runnable runnable) {
        getScheduler().runTask(MundoAddon.get(), runnable);
    }

    public static void async(Runnable runnable) {
        getScheduler().runTaskAsynchronously(MundoAddon.get(), runnable);
    }

    public static void syncDelay(int ticks, Runnable runnable) {
        getScheduler().runTaskLater(MundoAddon.get(), runnable, ticks);
    }

    public static void asyncDelay(int ticks, Runnable runnable) {
        getScheduler().runTaskLaterAsynchronously(MundoAddon.get(), runnable, ticks);
    }

    public static void asyncLock(Runnable runnable) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Scheduling.sync(() -> {
            runnable.run();
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Logging.reportException(runnable, e);
        }
    }
}
