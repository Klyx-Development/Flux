package org.klyx.flux.importer.snowstorm.emitter;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.expr.Expression;
import org.klyx.flux.importer.snowstorm.reading.Reader;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.sim.spec.EmitterSpec;

public final class RateReader implements Reader<EmitterSpec.Rate> {
    @Override
    public EmitterSpec.Rate read(ReadingContext context) {
        Node instant = context.component("emitter_rate_instant");
        if (instant != null) return instant(context, instant);

        Node steady = context.component("emitter_rate_steady");
        if (steady != null) return steady(context, steady);

        Node manual = context.component("emitter_rate_manual");
        if (manual != null) {
            context.ignore(manual, "manual rates are triggered from game code, which Flux has no equivalent of. Using a single particle");
        }

        return new EmitterSpec.Rate.Instant(Expression.constant(1));
    }

    private EmitterSpec.Rate instant(ReadingContext context, Node instant) {
        instant.onlyAllow("num_particles");
        Expression count = context.values().expression(instant.child("num_particles"), "10");

        // a looping burst can fire again while the last one is alive
        double rooms = context.has("emitter_lifetime_looping") ? 2 : 1;
        if (count.isConstant()) context.capacity(clamp(context, count.evalConstant() * rooms, instant));

        return new EmitterSpec.Rate.Instant(count);
    }

    private EmitterSpec.Rate steady(ReadingContext context, Node steady) {
        steady.onlyAllow("spawn_rate", "max_particles");
        Node max = steady.child("max_particles");
        if (max != null) {
            Expression limit = context.values().expression(max, "50");
            if (limit.isConstant()) {
                context.capacity(clamp(context, limit.evalConstant(), max));
            } else {
                max.error("max_particles must be a plain number");
            }
        }

        return new EmitterSpec.Rate.Steady(context.values().expression(steady.child("spawn_rate"), "1"));
    }

    private int clamp(ReadingContext context, double wanted, Node at) {
        if (wanted > EmitterSpec.MAX_PARTICLES) {
            context.ignore(at, "wants to spawn " + (long) wanted + " particles but the max is " + EmitterSpec.MAX_PARTICLES + ", using that instead");
            return EmitterSpec.MAX_PARTICLES;
        }

        return Math.max(1, (int) Math.ceil(wanted));
    }
}
