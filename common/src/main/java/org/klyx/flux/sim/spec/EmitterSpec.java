package org.klyx.flux.sim.spec;

import org.klyx.flux.expr.Expression;
import org.klyx.flux.expr.Script;
import org.klyx.flux.sim.SimVariables;
import org.klyx.flux.sim.curve.Curve;
import org.klyx.flux.sim.emission.Direction;
import org.klyx.flux.sim.emission.EmitterShape;

import java.util.List;
import java.util.Map;

public record EmitterSpec(
        Rate rate, Lifetime lifetime, EmitterShape shape, Direction direction, int maxParticles,
        List<Expression> offset, Map<String, Curve> curves, Script creation, Script perUpdate
) {
    public static final int MAX_PARTICLES = 4096;
    public static final int DEFAULT_MAX_PARTICLES = 256;

    public EmitterSpec {
        if (maxParticles < 1 || maxParticles > MAX_PARTICLES) {
            throw new IllegalArgumentException(
                    "maxParticles must be between 1 and " + MAX_PARTICLES + ": " + maxParticles
            );
        }

        if (offset.size() != 3) {
            throw new IllegalArgumentException("offset needs 3 expressions but has " + offset.size());
        }

        offset = List.copyOf(offset);
        curves = Map.copyOf(curves);

        for (Script script : List.of(creation, perUpdate)) {
            for (String name : script.assignedNames()) {
                if (SimVariables.isKnown(name)) {
                    throw new IllegalArgumentException("\"" + name + "\" is built in and can't be assigned to");
                }

                if (curves.containsKey(name)) {
                    throw new IllegalArgumentException("\"" + name + "\" is already the name of a curve");
                }
            }
        }
    }

    public EmitterSpec(Rate rate, Lifetime lifetime, EmitterShape shape, Direction direction,
            int maxParticles, List<Expression> offset, Map<String, Curve> curves) {
        this(rate, lifetime, shape, direction, maxParticles, offset, curves, Script.EMPTY, Script.EMPTY);
    }

    public EmitterSpec(Rate rate, Lifetime lifetime, EmitterShape shape, Direction direction, int maxParticles) {
        this(rate, lifetime, shape, direction, maxParticles,
                List.of(Expression.constant(0), Expression.constant(0), Expression.constant(0)), Map.of());
    }

    public sealed interface Rate permits Rate.Steady, Rate.Instant {
        record Steady(Expression perSecond) implements Rate { }
        record Instant(Expression count) implements Rate { }
    }

    public sealed interface Lifetime permits Lifetime.Once, Lifetime.Looping {
        record Once(Expression activeTime) implements Lifetime { }
        record Looping(Expression activeTime, Expression sleepTime) implements Lifetime { }
    }
}
