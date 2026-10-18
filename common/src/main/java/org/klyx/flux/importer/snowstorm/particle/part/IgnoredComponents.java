package org.klyx.flux.importer.snowstorm.particle.part;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.util.List;

public final class IgnoredComponents implements ParticlePart {

    @Override
    public void apply(ReadingContext context, ParticleSpec.Builder particle) {
        Node lighting = context.component("particle_appearance_lighting");
        if (lighting != null) context.ignore(lighting, "particles are always rendered at full brightness");

        for (String name : List.of("particle_expire_if_in_blocks", "particle_expire_if_not_in_blocks", "particle_kill_plane")) {
            Node node = context.component(name);
            if (node != null) context.ignore(node, "particles don't know about the blocks around them");
        }

        for (String name : List.of("particle_lifetime_events", "emitter_lifetime_events")) {
            Node node = context.component(name);
            if (node != null) context.ignore(node, "events aren't supported");
        }

        Node localSpace = context.component("emitter_local_space");
        if (localSpace != null) {
            localSpace.onlyAllow("position", "rotation", "velocity");
            context.ignore(localSpace, "particles aren't attached to the emitter's movement");
        }
    }

}
