package org.klyx.flux.importer.snowstorm.particle.part;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.sim.spec.ParticleSpec;

public final class InitialSpinReader implements ParticlePart {
    @Override
    public void apply(ReadingContext context, ParticleSpec.Builder particle) {
        Node spin = context.component("particle_initial_spin");
        if (spin == null) return;

        spin.onlyAllow("rotation", "rotation_rate");
        if (spin.child("rotation") != null) particle.initialRoll(context.values().expression(spin.child("rotation"), "0"));
        if (spin.child("rotation_rate") != null) particle.spin(context.values().expression(spin.child("rotation_rate"), "0"));
    }
}
