package org.klyx.flux.paper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.Nullable;
import org.klyx.flux.effect.anchor.DynamicAnchor;
import org.klyx.flux.effect.anchor.Followers;

import java.util.function.BooleanSupplier;

public final class EntityFollowers {
    private final JavaPlugin plugin;
    private final Followers followers = new Followers();
    private @Nullable BukkitTask task;

    public EntityFollowers(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void follow(Entity entity, double heightOffset, DynamicAnchor anchor, BooleanSupplier ended) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("reading an entity's position is not thread-safe! Please use the main thread for following :)");
        }

        this.followers.add(new Followers.Source() {
            @Override
            public boolean isValid() {
                return entity.isValid();
            }

            @Override
            public double x() {
                return entity.getX();
            }

            @Override
            public double y() {
                return entity.getY();
            }

            @Override
            public double z() {
                return entity.getZ();
            }

            @Override
            public double width() {
                return entity.getWidth();
            }

            @Override
            public double height() {
                return entity.getHeight();
            }
        }, heightOffset, anchor, ended);

        if (this.task == null) {
            this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::tick, 1L, 1L);
        }
    }

    public void close() {
        if (this.task == null) return;

        this.task.cancel();
        this.task = null;
    }

    private void tick() {
        if (this.followers.tick() == 0 && this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }
}
