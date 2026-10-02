package org.klyx.flux.sim;

import org.klyx.flux.expr.Script;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.sim.collision.Collider;
import org.klyx.flux.sim.emitter.Appearance;
import org.klyx.flux.sim.emitter.CollisionStage;
import org.klyx.flux.sim.emitter.EmissionCycle;
import org.klyx.flux.sim.emitter.Motion;
import org.klyx.flux.sim.emitter.ParticleStore;
import org.klyx.flux.sim.emitter.Spawner;
import org.klyx.flux.sim.spec.EmitterSpec;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.util.random.RandomGenerator;

/**
 * A particle emitter. Not thread-safe!
 */
public final class Emitter {
    private final ParticleSpec particle;
    private final SimVariables variables = new SimVariables();
    private final Script perUpdate;
    private final ParticleStore store;
    private final CollisionStage collisions;
    private final Motion motion;
    private final EmissionCycle cycle;

    public Emitter(EmitterSpec emitter, ParticleSpec particle, RandomGenerator random) {
        this.particle = particle;
        this.variables.curves(emitter.curves());
        this.variables.declare(emitter.creation(), emitter.perUpdate());
        this.perUpdate = emitter.perUpdate();
        this.store = new ParticleStore(emitter.maxParticles());

        Appearance appearance = new Appearance(particle, this.variables);
        this.collisions = new CollisionStage(particle, this.variables);
        this.motion = new Motion(particle, this.variables, this.store, appearance, this.collisions);

        Spawner spawner = new Spawner(emitter, particle, random, this.variables, this.store, appearance);
        this.cycle = new EmissionCycle(emitter, random, this.variables, spawner, this.store.capacity);

        // roll randoms first, so the setup and the first cycle can use them
        this.variables.rollEmitterRandoms(random);
        this.variables.run(emitter.creation());
        this.cycle.begin(false);
    }

    public void collider(Collider collider) {
        this.collisions.collider(collider);
    }

    public int capacity() {
        return this.store.capacity;
    }

    public int highWater() {
        return this.store.highWater();
    }

    public int aliveCount() {
        return this.store.aliveCount();
    }

    public boolean isAlive(int slot) {
        return this.store.alive[slot];
    }

    public double x(int slot) {
        return this.store.x[slot];
    }

    public double y(int slot) {
        return this.store.y[slot];
    }

    public double z(int slot) {
        return this.store.z[slot];
    }

    public double vx(int slot) {
        return this.store.vx[slot];
    }

    public double vy(int slot) {
        return this.store.vy[slot];
    }

    public double vz(int slot) {
        return this.store.vz[slot];
    }

    public float size(int slot) {
        return this.store.size[slot];
    }

    public float roll(int slot) {
        return this.store.roll[slot];
    }

    public int rgb(int slot) {
        return this.store.rgb[slot];
    }

    public int generation(int slot) {
        return this.store.generation[slot];
    }

    public ParticleSpec particle() {
        return this.particle;
    }

    public boolean hasStoppedEmitting() {
        return this.cycle.finished();
    }

    public boolean isFinished() {
        return this.cycle.finished() && this.store.aliveCount() == 0;
    }

    public void step(double deltaSeconds, Vec3 origin) {
        if (!(deltaSeconds > 0) || !Double.isFinite(deltaSeconds)) return;

        this.variables.emitterAge(this.cycle.time());
        this.variables.run(this.perUpdate);

        for (int slot = 0; slot < this.store.highWater(); slot++) {
            if (this.store.alive[slot]) this.motion.advance(slot, deltaSeconds, origin, this.cycle.time());
        }

        this.variables.clearParticle();
        this.cycle.run(deltaSeconds, origin);
    }
}
