package org.klyx.flux.sim.emitter;

import org.klyx.flux.expr.Expression;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.sim.Numbers;
import org.klyx.flux.sim.SimVariables;
import org.klyx.flux.sim.spec.EmitterSpec;
import org.klyx.flux.sim.spec.Parametric;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.util.List;
import java.util.random.RandomGenerator;

public final class Spawner {

    private final EmitterSpec emitter;
    private final ParticleSpec particle;
    private final RandomGenerator random;
    private final SimVariables variables;
    private final ParticleStore store;
    private final Appearance appearance;
    private final double[] offset = new double[3];
    private final double[] direction = new double[3];

    public Spawner(EmitterSpec emitter, ParticleSpec particle, RandomGenerator random,
                   SimVariables variables, ParticleStore store, Appearance appearance) {
        this.emitter = emitter;
        this.particle = particle;
        this.random = random;
        this.variables = variables;
        this.store = store;
        this.appearance = appearance;
    }

    public void spawn(int count, Vec3 origin, double emitterAge) {
        for (int i = 0; i < count; i++) {
            if (!spawnOne(origin, emitterAge)) break;
        }

        this.variables.clearParticle();
    }

    private boolean spawnOne(Vec3 origin, double emitterAge) {
        int slot = this.store.allocate();

        if (slot < 0) return false;

        this.store.rollRandoms(slot, this.random);
        this.variables.loadParticle(this.store, slot, 0, 0, emitterAge);
        this.emitter.shape().sample(this.random, this.variables, this.offset);
        this.emitter.direction().resolve(this.random, this.offset, this.variables, this.direction);

        double life = Numbers.finiteOr(this.particle.lifetime().eval(this.variables), 0);

        if (life < Numbers.MIN_LIFETIME) {
            this.store.release(slot);
            return true;
        }

        life = Math.min(life, 3600); // max lifetime
        this.variables.particleLifetime(life);
        this.store.activate(slot, life);

        place(slot, origin);
        launch(slot);

        this.store.roll[slot] = (float) Numbers.finiteOr(this.particle.initialRoll().eval(this.variables), 0);
        this.store.size[slot] = (float) this.appearance.size();
        this.store.rgb[slot] = this.appearance.color(0, life);

        Parametric parametric = this.particle.parametric();

        if (parametric != null) placeParametric(slot, parametric);

        return true;
    }

    private void place(int slot, Vec3 origin) {
        List<Expression> shift = this.emitter.offset();

        this.store.x[slot] = origin.x() + this.offset[0] + eval(shift.get(0));
        this.store.y[slot] = origin.y() + this.offset[1] + eval(shift.get(1));
        this.store.z[slot] = origin.z() + this.offset[2] + eval(shift.get(2));

        this.store.anchorX[slot] = origin.x();
        this.store.anchorY[slot] = origin.y();
        this.store.anchorZ[slot] = origin.z();
        this.store.bornX[slot] = this.store.x[slot];
        this.store.bornY[slot] = this.store.y[slot];
        this.store.bornZ[slot] = this.store.z[slot];
    }

    private void launch(int slot) {
        List<Expression> axisSpeed = this.particle.axisSpeed();

        if (axisSpeed != null) {
            // none direction would never move so replace direction times speed
            this.store.vx[slot] = eval(axisSpeed.get(0));
            this.store.vy[slot] = eval(axisSpeed.get(1));
            this.store.vz[slot] = eval(axisSpeed.get(2));
            return;
        }

        double speed = eval(this.particle.speed());

        this.store.vx[slot] = this.direction[0] * speed;
        this.store.vy[slot] = this.direction[1] * speed;
        this.store.vz[slot] = this.direction[2] * speed;
    }

    private void placeParametric(int slot, Parametric parametric) {
        List<Expression> position = parametric.position();

        if (position != null) {
            this.store.x[slot] = this.store.anchorX[slot] + eval(position.get(0));
            this.store.y[slot] = this.store.anchorY[slot] + eval(position.get(1));
            this.store.z[slot] = this.store.anchorZ[slot] + eval(position.get(2));
            this.store.vx[slot] = 0;
            this.store.vy[slot] = 0;
            this.store.vz[slot] = 0;
        }

        Expression rotation = parametric.rotation();

        if (rotation != null) this.store.roll[slot] = (float) eval(rotation);
    }

    private double eval(Expression expression) {
        return Numbers.finiteOr(expression.eval(this.variables), 0);
    }
}
