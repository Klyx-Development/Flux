package org.klyx.flux.effect;

import org.klyx.flux.blueprint.BlueprintProblem;
import org.klyx.flux.sim.spec.EmitterSpec;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.util.List;

public record AdvancedParticle(String identifier, EmitterSpec emitter, ParticleSpec particle, List<BlueprintProblem> ignored)
        implements ParticleLike {
    public static final String UNNAMED = "unnamed";

    public AdvancedParticle {
        ignored = List.copyOf(ignored);
    }

    public AdvancedParticle(EmitterSpec emitter, ParticleSpec particle) {
        this(UNNAMED, emitter, particle, List.of());
    }
}
