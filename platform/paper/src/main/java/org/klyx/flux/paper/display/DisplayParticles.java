package org.klyx.flux.paper.display;

import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.Nullable;
import org.klyx.flux.dispatch.PacketDispatcher;
import org.klyx.flux.effect.anchor.Anchor;
import org.klyx.flux.effect.anchor.DynamicAnchor;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.paper.EntityFollowers;
import org.klyx.flux.paper.display.simulation.SimulationRunner;
import org.klyx.flux.paper.display.viewer.PlayerPositions;
import org.klyx.flux.paper.display.viewer.WorldViewerPositions;
import org.klyx.flux.paper.display.world.WorldBlockCache;
import org.klyx.flux.sim.spec.EmitterSpec;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Plays display entity particle emitters asynchronously
 */
public final class DisplayParticles {
    public static final double DEFAULT_VIEW_DISTANCE = 64;

    private final JavaPlugin plugin;
    private final SimulationRunner runner;
    private final WorldBlockCache blocks = new WorldBlockCache();
    private final PlayerPositions playerPositions = new PlayerPositions();
    private final EntityFollowers followers;
    private final PacketDispatcher<Packet<?>> dispatcher;
    private final Object taskLock = new Object();
    private @Nullable BukkitTask positionTask;

    private volatile boolean closed;

    public DisplayParticles(
            JavaPlugin plugin, EntityFollowers followers, PacketDispatcher<Packet<?>> dispatcher
    ) {
        this.plugin = plugin;
        this.followers = followers;
        this.dispatcher = dispatcher;
        this.runner = new SimulationRunner(plugin.getSLF4JLogger());
    }

    public DisplayEmitter play(
            EmitterSpec emitter, ParticleSpec particle, Location location, Collection<? extends Player> viewers
    ) {
        World world = location.getWorld();
        if (world == null) throw new IllegalArgumentException("location has no world");

        Anchor anchor = Anchor.fixed(new Vec3(location.getX(), location.getY(), location.getZ()));
        return start(emitter, particle, anchor, world.getName(), viewers);
    }

    public DisplayEmitter follow(
            EmitterSpec emitter, ParticleSpec particle, Entity entity, double heightOffset, Collection<? extends Player> viewers
    ) {
        DynamicAnchor anchor = new DynamicAnchor(new Vec3(entity.getX(), entity.getY() + heightOffset, entity.getZ()));
        DisplayEmitter running = start(emitter, particle, anchor, entity.getWorld().getName(), viewers);

        this.followers.follow(entity, heightOffset, anchor, running::isStopped);
        return running;
    }

    public int activeCount() {
        return this.runner.size();
    }

    public void stopAll() {
        this.runner.stopAll();
    }

    public void close() {
        this.closed = true;

        synchronized (this.taskLock) {
            if (this.positionTask != null) {
                this.positionTask.cancel();
                this.positionTask = null;
            }
        }

        this.runner.shutdown();
    }

    private DisplayEmitter start(
            EmitterSpec emitter, ParticleSpec particle, Anchor anchor, String world, Collection<? extends Player> viewers
    ) {
        if (this.closed) throw new IllegalStateException("display particles have already been stopped");

        DisplayEmitter running = new DisplayEmitter(
                emitter, particle, anchor,
                new WorldViewerPositions(this.playerPositions, world),
                this.dispatcher, DEFAULT_VIEW_DISTANCE, DisplayEmitter.INTERPOLATION_TICKS, null
        );

        if (particle.collision() != null) running.collider(this.blocks.colliderFor(world));

        for (Player viewer : viewers) {
            running.addViewer(viewer.getUniqueId());
        }

        this.runner.add(running);
        ensurePositionTask();
        return running;
    }

    private void ensurePositionTask() {
        synchronized (this.taskLock) {
            if (this.positionTask == null && !this.closed) {
                this.positionTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this::snapshotPlayers, 1L, 1L);
            }
        }
    }

    private static boolean readPlayer(UUID id, PlayerPositions.Reading reading) {
        Player player = Bukkit.getPlayer(id);
        if (player == null) return false;

        reading.world = player.getWorld().getName();
        reading.x = player.getX();
        reading.y = player.getY();
        reading.z = player.getZ();
        return true;
    }

    private void snapshotPlayers() {
        this.blocks.refresh();

        synchronized (this.taskLock) {
            if (this.runner.isEmpty() && this.positionTask != null) {
                this.positionTask.cancel();
                this.positionTask = null;
                this.playerPositions.refresh(List.of(), DisplayParticles::readPlayer);
                return;
            }
        }

        this.playerPositions.refresh(this.runner.watchers(), DisplayParticles::readPlayer);
    }
}
