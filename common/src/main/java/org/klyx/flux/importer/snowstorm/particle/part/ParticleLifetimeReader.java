package org.klyx.flux.importer.snowstorm.particle.part;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.expr.Expression;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.sim.spec.ParticleSpec;

public final class ParticleLifetimeReader implements ParticlePart {
    @Override
    public void apply(ReadingContext context, ParticleSpec.Builder particle) {
        Node lifetime = context.component("particle_lifetime_expression");
        if (lifetime == null) {
            particle.lifetime(Expression.constant(1));
            return;
        }

        lifetime.onlyAllow("max_lifetime", "expiration_expression");

        Node expiration = lifetime.child("expiration_expression");
        if (expiration != null) {
            context.ignore(expiration, "an expression that ends the particle early isn't supported, it lives for max_lifetime");
        }

        Node max = lifetime.child("max_lifetime");
        if (max == null) {
            particle.lifetime(Expression.constant(1));
        } else {
            particle.lifetime(context.values().expression(max, "1.0"));
        }
    }
}
