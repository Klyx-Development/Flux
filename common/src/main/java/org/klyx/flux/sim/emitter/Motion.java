package org.klyx.flux.sim.emitter;

import org.klyx.flux.expr.Expression;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.sim.Numbers;
import org.klyx.flux.sim.SimVariables;
import org.klyx.flux.sim.force.ForceEffect;
import org.klyx.flux.sim.force.ForceEffects;
import org.klyx.flux.sim.spec.Parametric;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.util.List;

public final class Motion {
    private final ParticleSpec particle;
    private final SimVariables variables;
    private final ParticleStore store;
    private final Appearance appearance;
    private final CollisionStage collisions;
    private final List<ForceEffect> forces;
    private final double[] acceleration = new double[3];

    public Motion(ParticleSpec particle, SimVariables variables,
                  ParticleStore store, Appearance appearance, CollisionStage collisions) {
        this.particle = particle;
        this.variables = variables;
        this.store = store;
        this.appearance = appearance;
        this.collisions = collisions;
        this.forces = ForceEffects.allOf(particle.forces());
    }

    public void advance(int slot, double deltaSeconds, Vec3 origin, double emitterAge) {
        double now = this.store.age[slot] + deltaSeconds;

        if (now >= this.store.lifetime[slot]) {
            this.store.kill(slot);
            return;
        }

        this.store.age[slot] = now;
        this.variables.loadParticle(this.store, slot, now, this.store.lifetime[slot], emitterAge);

        Parametric parametric = this.particle.parametric();

        if (parametric != null) {
            followExpressions(slot, parametric, deltaSeconds, origin);
        } else if (!move(slot, deltaSeconds)) {
            this.store.kill(slot);
            return;
        }

        this.store.size[slot] = (float) this.appearance.size();
        if (parametric == null || parametric.rotation() == null) {
            this.store.roll[slot] += (float) (this.appearance.spin() * deltaSeconds);
        }

        this.store.rgb[slot] = this.appearance.color(now, this.store.lifetime[slot]);
    }

    private void followExpressions(int slot, Parametric parametric, double deltaSeconds, Vec3 origin) {
        List<Expression> position = parametric.position();

        if (position != null) {
            double nx = origin.x() + eval(position.get(0));
            double ny = origin.y() + eval(position.get(1));
            double nz = origin.z() + eval(position.get(2));

            this.store.vx[slot] = (nx - this.store.x[slot]) / deltaSeconds;
            this.store.vy[slot] = (ny - this.store.y[slot]) / deltaSeconds;
            this.store.vz[slot] = (nz - this.store.z[slot]) / deltaSeconds;
            this.store.x[slot] = nx;
            this.store.y[slot] = ny;
            this.store.z[slot] = nz;
        }

        Expression rotation = parametric.rotation();

        if (rotation != null) this.store.roll[slot] = (float) eval(rotation);
    }

    private boolean move(int slot, double deltaSeconds) {
        List<Expression> base = this.particle.acceleration();

        this.acceleration[0] = eval(base.get(0));
        this.acceleration[1] = eval(base.get(1));
        this.acceleration[2] = eval(base.get(2));

        for (ForceEffect force : this.forces) {
            force.accelerate(this.store, slot, this.variables, this.acceleration);
        }

        this.store.vx[slot] += this.acceleration[0] * deltaSeconds;
        this.store.vy[slot] += this.acceleration[1] * deltaSeconds;
        this.store.vz[slot] += this.acceleration[2] * deltaSeconds;

        for (ForceEffect force : this.forces) {
            force.constrain(this.store, slot, this.variables);
        }

        applyDrag(slot, deltaSeconds);

        return this.collisions.travel(this.store, slot, deltaSeconds);
    }

    private void applyDrag(int slot, double deltaSeconds) {
        double drag = eval(this.particle.drag());

        if (!(drag > 0)) return;

        double keep = Math.exp(-drag * deltaSeconds);
        this.store.vx[slot] *= keep;
        this.store.vy[slot] *= keep;
        this.store.vz[slot] *= keep;
    }

    private double eval(Expression expression) {
        return Numbers.finiteOr(expression.eval(this.variables), 0);
    }
}
