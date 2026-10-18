package org.klyx.flux.importer.snowstorm.particle.part;

import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.sim.spec.ParticleSpec;

public interface ParticlePart {
    void apply(ReadingContext context, ParticleSpec.Builder particle);
}
