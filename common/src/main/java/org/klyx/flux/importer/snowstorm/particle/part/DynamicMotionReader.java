package org.klyx.flux.importer.snowstorm.particle.part;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.importer.snowstorm.reading.Values;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.util.List;

public final class DynamicMotionReader implements ParticlePart {
    @Override
    public void apply(ReadingContext context, ParticleSpec.Builder particle) {
        Node node = context.component("particle_motion_dynamic");
        if (node == null) return;

        node.onlyAllow("linear_acceleration", "linear_drag_coefficient", "rotation_acceleration", "rotation_drag_coefficient");

        Values values = context.values();
        Node acceleration = node.child("linear_acceleration");
        if (acceleration != null) particle.acceleration(values.vector(acceleration, Values.zeroVector()));

        Node drag = node.child("linear_drag_coefficient");
        if (drag != null) particle.drag(values.expression(drag, "0"));
    }
}
