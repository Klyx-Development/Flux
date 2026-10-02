package org.klyx.flux.sim.force;

import org.klyx.flux.expr.Variables;
import org.klyx.flux.sim.Numbers;
import org.klyx.flux.sim.emitter.ParticleStore;

public final class AttractorEffect implements ForceEffect {
    private final Force.Attractor attractor;

    public AttractorEffect(Force.Attractor attractor) {
        this.attractor = attractor;
    }

    @Override
    public void accelerate(ParticleStore store, int slot, Variables variables, double[] acceleration) {
        double toCenterX = store.bornX[slot] + this.attractor.center().x() - store.x[slot];
        double toCenterY = store.bornY[slot] + this.attractor.center().y() - store.y[slot];
        double toCenterZ = store.bornZ[slot] + this.attractor.center().z() - store.z[slot];
        double squared = toCenterX * toCenterX + toCenterY * toCenterY + toCenterZ * toCenterZ;
        double soft = Force.SOFTENING * Force.SOFTENING;
        double strength = Numbers.finiteOr(this.attractor.strength().eval(variables), 0);
        double pull = strength / Math.pow(squared + soft, 1.5);

        acceleration[0] += toCenterX * pull;
        acceleration[1] += toCenterY * pull;
        acceleration[2] += toCenterZ * pull;
    }
}
