package org.klyx.flux.paper.display;

import net.minecraft.network.protocol.Packet;
import org.jspecify.annotations.Nullable;
import org.klyx.flux.dispatch.PacketDispatcher;
import org.klyx.flux.effect.anchor.Anchor;
import org.klyx.flux.effect.playback.Playback;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.paper.display.simulation.PacketOutbox;
import org.klyx.flux.paper.display.simulation.SlotSync;
import org.klyx.flux.paper.display.viewer.ViewerLedger;
import org.klyx.flux.paper.display.viewer.ViewerPositions;
import org.klyx.flux.paper.display.viewer.ViewerSent;
import org.klyx.flux.paper.display.viewer.ViewerSelector;
import org.klyx.flux.sim.Emitter;
import org.klyx.flux.sim.EmitterView;
import org.klyx.flux.sim.collision.Collider;
import org.klyx.flux.sim.spec.EmitterSpec;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public final class DisplayEmitter implements Playback {
    public static final int UPDATE_INTERVAL = 2;
    public static final int INTERPOLATION_TICKS = UPDATE_INTERVAL + 1;

    private final Emitter emitter;
    private final Anchor anchor;
    private final DisplayStyle style;
    private final DisplayParticle[] particles;
    private final EmitterView view;
    private final ViewerLedger ledger;
    private final ViewerSent sent;
    private final ViewerSelector selector;
    private final ViewerPositions positions;
    private final PacketDispatcher<Packet<?>> dispatcher;

    private volatile boolean paused;
    private volatile boolean stopped;

    public DisplayEmitter(
            EmitterSpec emitterSpec, ParticleSpec particle, Anchor anchor, ViewerPositions positions,
            PacketDispatcher<Packet<?>> dispatcher, double maxViewDistance, int interpolationTicks,
            @Nullable RandomGenerator random
    ) {
        RandomGenerator generator = random != null ? random : RandomGeneratorFactory.of("L64X128MixRandom").create();

        this.emitter = new Emitter(emitterSpec, particle, generator);
        this.anchor = anchor;
        this.positions = positions;
        this.dispatcher = dispatcher;
        this.style = new DisplayStyle(particle, interpolationTicks);
        this.particles = new DisplayParticle[this.emitter.capacity()];
        this.view = new EmitterView(this.emitter.capacity(), UPDATE_INTERVAL);
        this.ledger = new ViewerLedger(this.particles.length);
        this.sent = new ViewerSent(this.particles.length);
        this.selector = new ViewerSelector(
                this.ledger, positions, anchor, maxViewDistance * maxViewDistance, this.particles, dispatcher,
                this.sent);
    }

    public void collider(Collider collider) {
        this.emitter.collider(collider);
    }

    public void addViewer(UUID viewer) {
        if (!this.stopped) this.ledger.add(viewer);
    }

    public void removeViewer(UUID viewer) {
        BitSet shown = this.ledger.remove(viewer);
        this.sent.forget(viewer);
        if (shown != null) ViewerLedger.despawnAll(viewer, shown, this.particles, this.dispatcher);
    }

    public Collection<UUID> watching() {
        return this.ledger.ids();
    }

    public int particleCount() {
        return this.emitter.aliveCount();
    }

    @Override
    public void pause() {
        this.paused = true;
    }

    @Override
    public void resume() {
        this.paused = false;
    }

    @Override
    public void stop() {
        this.stopped = true;
    }

    @Override
    public boolean isPaused() {
        return this.paused;
    }

    @Override
    public boolean isStopped() {
        return this.stopped;
    }

    public boolean tick(double deltaSeconds) {
        if (this.stopped || !this.anchor.isValid()) {
            finish();
            return false;
        }

        if (!this.paused) advance(deltaSeconds);

        publish();

        if (this.emitter.isFinished()) {
            finish();
            return false;
        }

        return true;
    }

    private void advance(double deltaSeconds) {
        Vec3 origin = this.anchor.position();
        double remaining = deltaSeconds;

        while (remaining > 1.0e-9) {
            double step = Math.min(remaining, 0.05);
            this.emitter.step(step, origin);
            remaining -= step;
        }
    }

    private void publish() {
        ViewerSelector.Selection selection = this.selector.select();
        PacketOutbox outbox = new PacketOutbox(this.dispatcher);
        SlotSync sync = new SlotSync(
                this.emitter, this.particles, this.style, this.ledger, selection, this.sent, outbox);

        this.view.sync(this.emitter, sync);
        sync.spawnMissing();
        outbox.flush();
    }

    private void finish() {
        this.stopped = true;

        for (Map.Entry<UUID, BitSet> entry : this.ledger.entries()) {
            if (!this.positions.isOffline(entry.getKey())) {
                ViewerLedger.despawnAll(entry.getKey(), entry.getValue(), this.particles, this.dispatcher);
            }
        }

        this.ledger.clear();
        this.sent.clear();
    }
}
