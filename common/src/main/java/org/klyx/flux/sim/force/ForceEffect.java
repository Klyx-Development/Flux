package org.klyx.flux.sim.force;

import org.klyx.flux.expr.Variables;
import org.klyx.flux.sim.emitter.ParticleStore;

public interface ForceEffect {
    default void accelerate(ParticleStore store, int slot, Variables variables, double[] acceleration) {
    }

    default void constrain(ParticleStore store, int slot, Variables variables) {
    }
}
