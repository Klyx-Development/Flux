package org.klyx.flux.importer.snowstorm.particle.part;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.expr.Expression;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.importer.snowstorm.reading.Values;
import org.klyx.flux.sim.spec.Parametric;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.util.List;

public final class ParametricMotionReader implements ParticlePart {
    @Override
    public void apply(ReadingContext context, ParticleSpec.Builder particle) {
        if (context.has("particle_motion_dynamic")) return;

        Node node = context.component("particle_motion_parametric");
        if (node == null) return;

        node.onlyAllow("relative_position", "direction", "rotation");

        Node position = node.child("relative_position");
        Node rotation = node.child("rotation");
        Node direction = node.child("direction");
        if (direction != null) context.ignore(direction, "particles don't turn to follow a direction");

        List<Expression> where = position == null ? null : context.values().vector(position, Values.zeroVector());
        Expression roll = rotation == null ? null : context.values().expression(rotation, "0");

        particle.parametric(new Parametric(where, roll));
    }
}
