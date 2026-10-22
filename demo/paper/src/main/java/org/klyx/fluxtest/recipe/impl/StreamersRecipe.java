package org.klyx.fluxtest.recipe.impl;

import org.klyx.flux.expr.Expression;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.sim.emission.Direction;
import org.klyx.flux.sim.emission.EmitterShape;
import org.klyx.flux.sim.force.Force;
import org.klyx.flux.sim.spec.EmitterSpec;
import org.klyx.flux.sim.spec.ParticleSpec;
import org.klyx.fluxtest.recipe.Recipe;

import java.util.List;

import static org.klyx.fluxtest.recipe.DemoExpressions.expr;

public final class StreamersRecipe {
    public static Recipe create() {
        EmitterSpec emitter = new EmitterSpec(
                new EmitterSpec.Rate.Instant(Expression.constant(80)),
                new EmitterSpec.Lifetime.Looping(Expression.constant(0.1), Expression.constant(1.8)),
                new EmitterShape.Point(), new Direction.Cone(new Vec3(0, 1, 0), 35),
                160);
        ParticleSpec particle = ParticleSpec.builder()
                .lifetime(Expression.constant(3))
                .speed(expr("5 + variable.particle_random_1 * 2"))
                .acceleration(new Vec3(0, -12, 0))
                .force(new Force.SetSpeed(Expression.constant(4.5)))
                .spin(Expression.constant(360))
                .size(Expression.constant(0.1))
                .colorExpressions(List.of(
                        expr("0.4 + 0.6 * variable.particle_random_1"),
                        expr("0.4 + 0.6 * variable.particle_random_2"),
                        expr("0.4 + 0.6 * variable.particle_random_3")))
                .build();
        return new Recipe(emitter, particle, 0.5);
    }
}
