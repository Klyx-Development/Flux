package org.klyx.fluxtest.recipe;

import org.klyx.flux.effect.AdvancedParticle;
import org.klyx.flux.sim.spec.EmitterSpec;
import org.klyx.flux.sim.spec.ParticleSpec;

public record Recipe(AdvancedParticle system, double heightOffset) {
    public Recipe(EmitterSpec emitter, ParticleSpec particle, double heightOffset) {
        this(new AdvancedParticle(emitter, particle), heightOffset);
    }

    public EmitterSpec emitter() {
        return this.system.emitter();
    }

    public ParticleSpec particle() {
        return this.system.particle();
    }
}
