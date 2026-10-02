package org.klyx.flux.sim.force;

import org.klyx.flux.expr.Variables;
import org.klyx.flux.sim.Numbers;
import org.klyx.flux.sim.emitter.ParticleStore;

public final class SpeedHoldEffect implements ForceEffect {
    private final Force.SetSpeed hold;

    public SpeedHoldEffect(Force.SetSpeed hold) {
        this.hold = hold;
    }

    @Override
    public void constrain(ParticleStore store, int slot, Variables variables) {
        double wanted = Math.max(0, Numbers.finiteOr(this.hold.speed().eval(variables), 0));
        double speed = Math.sqrt(
                store.vx[slot] * store.vx[slot] + store.vy[slot] * store.vy[slot] + store.vz[slot] * store.vz[slot]
        );

        if (!(speed > 1.0e-9)) return;

        double scale = wanted / speed;
        store.vx[slot] *= scale;
        store.vy[slot] *= scale;
        store.vz[slot] *= scale;
    }
}
