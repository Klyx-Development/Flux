package org.klyx.flux.paper;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.klyx.flux.dispatch.TickScheduler;

public final class PaperTickScheduler implements TickScheduler {
    private final JavaPlugin plugin;

    public PaperTickScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Handle repeatEveryTick(Runnable task) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(this.plugin, task, 1L, 1L);
        return bukkitTask::cancel;
    }
}
