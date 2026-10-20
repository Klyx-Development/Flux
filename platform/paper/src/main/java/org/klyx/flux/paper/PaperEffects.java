package org.klyx.flux.paper;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.klyx.flux.effect.Effect;
import org.klyx.flux.effect.EffectPlayer;
import org.klyx.flux.effect.Layer;
import org.klyx.flux.effect.anchor.Anchor;
import org.klyx.flux.effect.anchor.DynamicAnchor;
import org.klyx.flux.effect.playback.EffectInstance;
import org.klyx.flux.effect.playback.EffectSink;
import org.klyx.flux.math.Vec3;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class PaperEffects {

    private final EntityFollowers followers;
    private final Logger logger;
    private final ParticleRenderer renderer;
    private final EffectPlayer player;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "flux-effects");
        thread.setDaemon(true);
        return thread;
    });

    private long lastTick = System.nanoTime();

    public PaperEffects(JavaPlugin plugin, ParticleRenderer renderer, EntityFollowers followers) {
        this.followers = followers;
        this.logger = plugin.getSLF4JLogger();
        this.renderer = renderer;
        this.player = new EffectPlayer(this.logger);

        this.executor.scheduleAtFixedRate(this::tick, 50, 50, TimeUnit.MILLISECONDS);
    }

    public EffectInstance play(Effect effect, Location location, Collection<? extends Player> viewers) {
        World world = requireWorld(location);
        Anchor anchor = Anchor.fixed(new Vec3(location.getX(), location.getY(), location.getZ()));

        check(effect);
        return this.player.play(effect, anchor, sink(world, viewers));
    }

    public EffectInstance follow(Effect effect, Entity entity, double heightOffset, Collection<? extends Player> viewers) {
        check(effect);

        DynamicAnchor anchor = new DynamicAnchor(positionOf(entity, heightOffset));
        EffectInstance instance = this.player.play(effect, anchor, sink(entity.getWorld(), viewers));

        this.followers.follow(entity, heightOffset, anchor, instance::isStopped);
        return instance;
    }

    public void stopAll() {
        this.player.stopAll();
    }

    public void shutdown() {
        this.executor.shutdownNow();
        this.player.stopAll();
    }

    private void check(Effect effect) {
        for (Layer layer : effect.layers()) {
            this.renderer.check(layer.particle());
        }

        effect.cueEffects().values().forEach(this::check);
    }

    private EffectSink sink(World world, Collection<? extends Player> viewers) {
        List<Player> targets = List.copyOf(viewers);

        return (layers, origin) -> this.renderer.render(
                layers, new Location(world, origin.x(), origin.y(), origin.z()), targets);
    }

    private void tick() {
        try {
            long now = System.nanoTime();
            double deltaSeconds = Math.min(
                    (now - this.lastTick) / (double) TimeUnit.SECONDS.toNanos(1), 0.25);
            this.lastTick = now;

            this.player.tick(deltaSeconds);
        } catch (RuntimeException e) {
            this.logger.error("effect tick failed", e);
        }
    }

    private static Vec3 positionOf(Entity entity, double heightOffset) {
        Location location = entity.getLocation();
        return new Vec3(location.getX(), location.getY() + heightOffset, location.getZ());
    }

    private static World requireWorld(Location location) {
        World world = location.getWorld();
        if (world == null) throw new IllegalArgumentException("location has no world");

        return world;
    }
}
