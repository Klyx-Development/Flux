package org.klyx.flux.paper.display;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.effect.anchor.Anchor;
import org.klyx.flux.effect.playback.Playback;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.paper.display.simulation.MoveBatch;
import org.klyx.flux.paper.display.simulation.SlotSync;
import org.klyx.flux.paper.display.viewer.ViewerLedger;
import org.klyx.flux.paper.display.viewer.ViewerPositions;
import org.klyx.flux.paper.display.viewer.ViewerSelector;
import org.klyx.flux.sim.Emitter;
import org.klyx.flux.sim.EmitterView;
import org.klyx.flux.sim.collision.Collider;
import org.klyx.flux.sim.spec.EmitterSpec;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public final class DisplayEmitter implements Playback {

    private final Emitter emitter;
    private final Anchor anchor;
    private final DisplayStyle style;
    private final DisplayParticle[] particles;
    private final EmitterView view;
    private final ViewerLedger ledger;
    private final ViewerSelector selector;
    private final ViewerPositions positions;

    private volatile boolean paused;
    private volatile boolean stopped;

    public DisplayEmitter(
            EmitterSpec emitterSpec, ParticleSpec particle, Anchor anchor, ViewerPositions positions,
            double maxViewDistance, int glideTicks, @Nullable RandomGenerator random
    ) {
        RandomGenerator generator = random != null ? random : RandomGeneratorFactory.of("L64X128MixRandom").create();

        this.emitter = new Emitter(emitterSpec, particle, generator);
        this.anchor = anchor;
        this.positions = positions;
        this.style = new DisplayStyle(particle, glideTicks);
        this.particles = new DisplayParticle[this.emitter.capacity()];
        this.view = new EmitterView(this.emitter.capacity());
        this.ledger = new ViewerLedger(this.particles.length);
        this.selector = new ViewerSelector(
                this.ledger, positions, anchor, maxViewDistance * maxViewDistance, this.particles);
    }

    public void collider(Collider collider) {
        this.emitter.collider(collider);
    }

    public void addViewer(UUID viewer) {
        if (!this.stopped) this.ledger.add(viewer);
    }

    public void removeViewer(UUID viewer) {
        BitSet shown = this.ledger.remove(viewer);
        if (shown != null) ViewerLedger.despawnAll(viewer, shown, this.particles);
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
        List<UUID> active = this.selector.select();
        MoveBatch moves = new MoveBatch();
        SlotSync sync = new SlotSync(this.emitter, this.particles, this.style, this.ledger, active, moves);

        this.view.sync(this.emitter, sync);
        moves.send();
        sync.spawnMissing();
    }

    private void finish() {
        this.stopped = true;

        for (Map.Entry<UUID, BitSet> entry : this.ledger.entries()) {
            if (!this.positions.isOffline(entry.getKey())) {
                ViewerLedger.despawnAll(entry.getKey(), entry.getValue(), this.particles);
            }
        }

        this.ledger.clear();
    }
}
