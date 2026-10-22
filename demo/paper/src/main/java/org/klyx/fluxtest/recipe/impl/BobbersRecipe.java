package org.klyx.fluxtest.recipe.impl;

import org.klyx.flux.expr.Expression;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.shader.Gradient;
import org.klyx.flux.sim.emission.Direction;
import org.klyx.flux.sim.emission.EmitterShape;
import org.klyx.flux.sim.force.Force;
import org.klyx.flux.sim.spec.EmitterSpec;
import org.klyx.flux.sim.spec.ParticleSpec;
import org.klyx.fluxtest.recipe.Recipe;

import static org.klyx.fluxtest.recipe.DemoExpressions.expr;

public final class BobbersRecipe {
    public static Recipe create() {
        EmitterSpec emitter = new EmitterSpec(
                new EmitterSpec.Rate.Steady(Expression.constant(30)),
                new EmitterSpec.Lifetime.Once(Expression.constant(3600)),
                new EmitterShape.Sphere(2.5, true), new Direction.None(), 250);
        ParticleSpec particle = ParticleSpec.builder()
                .lifetime(Expression.constant(6))
                .drag(Expression.constant(0.3))
                .force(new Force.Attractor(new Vec3(0, 1.2, 0), Expression.constant(14)))
                .size(Expression.constant(0.09))
                .color(Gradient.of(0xFF5A5A, 0xFFC857, 0x6BFF8A, 0x57C7FF, 0xB07BFF))
                .colorInterpolant(expr("variable.particle_random_1"))
                .build();
        return new Recipe(emitter, particle, 1.4);
    }
}
