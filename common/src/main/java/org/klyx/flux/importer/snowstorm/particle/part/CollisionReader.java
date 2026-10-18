package org.klyx.flux.importer.snowstorm.particle.part;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.expr.Expression;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.importer.snowstorm.reading.Values;
import org.klyx.flux.sim.collision.Collision;
import org.klyx.flux.sim.spec.ParticleSpec;

public final class CollisionReader implements ParticlePart {
    @Override
    public void apply(ReadingContext context, ParticleSpec.Builder particle) {
        Node node = context.component("particle_motion_collision");
        if (node == null) return;

        node.onlyAllow("enabled", "collision_drag", "coefficient_of_restitution", "collision_radius", "expire_on_contact", "events");

        Values values = context.values();
        Expression enabled = values.expression(node.child("enabled"), "1");
        double drag = values.nonNegative(node, "collision_drag", 0);

        double restitution = values.nonNegative(node, "coefficient_of_restitution", 0);
        if (restitution > 1) restitution = 1;

        double radius = values.nonNegative(node, "collision_radius", Collision.DEFAULT_RADIUS);
        boolean expire = node.bool("expire_on_contact", false);

        Node events = node.child("events");
        if (events != null) context.ignore(events, "events aren't supported");

        particle.collision(new Collision(enabled, restitution, drag, radius, expire));
    }
}
