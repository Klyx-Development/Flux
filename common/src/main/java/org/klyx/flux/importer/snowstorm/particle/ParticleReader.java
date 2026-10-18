package org.klyx.flux.importer.snowstorm.particle;

import org.klyx.flux.importer.snowstorm.particle.part.BillboardReader;
import org.klyx.flux.importer.snowstorm.particle.part.CollisionReader;
import org.klyx.flux.importer.snowstorm.particle.part.DynamicMotionReader;
import org.klyx.flux.importer.snowstorm.particle.part.IgnoredComponents;
import org.klyx.flux.importer.snowstorm.particle.part.InitialSpeedReader;
import org.klyx.flux.importer.snowstorm.particle.part.InitialSpinReader;
import org.klyx.flux.importer.snowstorm.particle.part.ItemReader;
import org.klyx.flux.importer.snowstorm.particle.part.ParametricMotionReader;
import org.klyx.flux.importer.snowstorm.particle.part.ParticleLifetimeReader;
import org.klyx.flux.importer.snowstorm.particle.part.ParticlePart;
import org.klyx.flux.importer.snowstorm.particle.part.TintingReader;
import org.klyx.flux.importer.snowstorm.reading.Reader;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.util.List;

public final class ParticleReader implements Reader<ParticleSpec> {
    private static final List<ParticlePart> PARTS = List.of(
            new ParticleLifetimeReader(),
            new InitialSpeedReader(),
            new InitialSpinReader(),
            new DynamicMotionReader(),
            new ParametricMotionReader(),
            new CollisionReader(),
            new BillboardReader(),
            new TintingReader(),
            new ItemReader(),
            new IgnoredComponents()
    );

    @Override
    public ParticleSpec read(ReadingContext context) {
        ParticleSpec.Builder particle = ParticleSpec.builder();
        for (ParticlePart part : PARTS) {
            part.apply(context, particle);
        }

        return particle.build();
    }
}
