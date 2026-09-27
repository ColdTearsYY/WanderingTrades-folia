package xyz.jpenilla.wanderingtrades.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/** Paper/Folia scheduler bridge used by all plugin-owned tasks. */
public final class ServerScheduler {
    private static final boolean FOLIA = detectFolia();

    private final Plugin plugin;

    public ServerScheduler(final Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean isFolia() {
        return FOLIA;
    }

    public void runGlobal(final Runnable task) {
        if (FOLIA) {
            Bukkit.getGlobalRegionScheduler().run(this.plugin, ignored -> task.run());
        } else {
            Bukkit.getScheduler().runTask(this.plugin, task);
        }
    }

    public void runGlobalDelayed(final Runnable task, final long delayTicks) {
        if (FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(this.plugin, ignored -> task.run(), Math.max(1L, delayTicks));
        } else {
            Bukkit.getScheduler().runTaskLater(this.plugin, task, Math.max(0L, delayTicks));
        }
    }

    public TaskHandle runGlobalTimer(final Runnable task, final long delayTicks, final long periodTicks) {
        if (FOLIA) {
            final ScheduledTask scheduled = Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                this.plugin, ignored -> task.run(), Math.max(1L, delayTicks), Math.max(1L, periodTicks)
            );
            return scheduled::cancel;
        }
        final BukkitTask scheduled = Bukkit.getScheduler().runTaskTimer(
            this.plugin, task, Math.max(0L, delayTicks), Math.max(1L, periodTicks)
        );
        return scheduled::cancel;
    }

    public void runAtEntity(final Entity entity, final Runnable task) {
        this.runAtEntity(entity, task, null);
    }

    public void runAtEntity(final Entity entity, final Runnable task, final Runnable retired) {
        if (FOLIA) {
            entity.getScheduler().run(this.plugin, ignored -> task.run(), retired);
        } else {
            Bukkit.getScheduler().runTask(this.plugin, task);
        }
    }

    public void runAtEntityDelayed(final Entity entity, final Runnable task, final long delayTicks) {
        if (FOLIA) {
            entity.getScheduler().runDelayed(this.plugin, ignored -> task.run(), null, Math.max(1L, delayTicks));
        } else {
            Bukkit.getScheduler().runTaskLater(this.plugin, task, Math.max(0L, delayTicks));
        }
    }

    public void runAtLocation(final Location location, final Runnable task) {
        if (FOLIA) {
            Bukkit.getRegionScheduler().run(this.plugin, location, ignored -> task.run());
        } else {
            Bukkit.getScheduler().runTask(this.plugin, task);
        }
    }

    public TaskHandle runAsyncTimer(final Runnable task, final long delayTicks, final long periodTicks) {
        if (FOLIA) {
            final ScheduledTask scheduled = Bukkit.getAsyncScheduler().runAtFixedRate(
                this.plugin, ignored -> task.run(), Math.max(0L, delayTicks) * 50L,
                Math.max(1L, periodTicks) * 50L, TimeUnit.MILLISECONDS
            );
            return scheduled::cancel;
        }
        final BukkitTask scheduled = Bukkit.getScheduler().runTaskTimerAsynchronously(
            this.plugin, task, Math.max(0L, delayTicks), Math.max(1L, periodTicks)
        );
        return scheduled::cancel;
    }

    public void runAsync(final Runnable task) {
        if (FOLIA) {
            Bukkit.getAsyncScheduler().runNow(this.plugin, ignored -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, task);
        }
    }

    public void cancelAll() {
        if (FOLIA) {
            Bukkit.getGlobalRegionScheduler().cancelTasks(this.plugin);
            Bukkit.getAsyncScheduler().cancelTasks(this.plugin);
        } else {
            Bukkit.getScheduler().cancelTasks(this.plugin);
        }
    }

    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (final ClassNotFoundException ignored) {
            return false;
        }
    }

    @FunctionalInterface
    public interface TaskHandle {
        void cancel();
    }
}
