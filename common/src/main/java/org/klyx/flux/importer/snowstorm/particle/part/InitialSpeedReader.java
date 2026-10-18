package org.klyx.flux.importer.snowstorm.particle.part;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.importer.snowstorm.reading.Values;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.util.List;

public final class InitialSpeedReader implements ParticlePart {
    @Override
    public void apply(ReadingContext context, ParticleSpec.Builder particle) {
        Node node = context.component("particle_initial_speed");
        if (node == null) return;

        Values values = context.values();
        if (!node.isArray()) {
            particle.speed(values.expression(node, "0"));
            return;
        }

        List<Node> parts = node.items();
        if (parts.size() != 3) {
            node.error("expected three speeds [x, y, z] but got " + parts.size());
            return;
        }

        particle.axisSpeed(List.of(values.expression(parts.get(0), "0"), values.expression(parts.get(1), "0"), values.expression(parts.get(2), "0")));
    }
}
