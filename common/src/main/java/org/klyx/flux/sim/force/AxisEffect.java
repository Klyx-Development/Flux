package org.klyx.flux.sim.force;

import org.klyx.flux.expr.Variables;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.sim.Numbers;
import org.klyx.flux.sim.emitter.ParticleStore;

public final class AxisEffect implements ForceEffect {
    private final Force.Axis line;

    public AxisEffect(Force.Axis line) {
        this.line = line;
    }

    @Override
    public void accelerate(ParticleStore store, int slot, Variables variables, double[] acceleration) {
        Vec3 axis = this.line.axis();
        double rx = store.x[slot] - store.anchorX[slot];
        double ry = store.y[slot] - store.anchorY[slot];
        double rz = store.z[slot] - store.anchorZ[slot];
        double along = Numbers.finiteOr(this.line.along().eval(variables), 0);

        acceleration[0] += axis.x() * along;
        acceleration[1] += axis.y() * along;
        acceleration[2] += axis.z() * along;

        double projection = rx * axis.x() + ry * axis.y() + rz * axis.z();
        double ox = rx - axis.x() * projection;
        double oy = ry - axis.y() * projection;
        double oz = rz - axis.z() * projection;
        double distance = Math.sqrt(ox * ox + oy * oy + oz * oz);

        if (distance > 1.0e-6) {
            double toward = Numbers.finiteOr(this.line.toward().eval(variables), 0);

            acceleration[0] -= ox / distance * toward;
            acceleration[1] -= oy / distance * toward;
            acceleration[2] -= oz / distance * toward;
        }
    }
}
