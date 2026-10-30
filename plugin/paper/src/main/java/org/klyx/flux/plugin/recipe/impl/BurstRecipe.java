package org.klyx.flux.plugin.recipe.impl;

import org.klyx.flux.expr.Expression;
import org.klyx.flux.shader.Gradient;
import org.klyx.flux.sim.emission.Direction;
import org.klyx.flux.sim.emission.EmitterShape;
import org.klyx.flux.sim.spec.EmitterSpec;
import org.klyx.flux.sim.spec.ParticleSpec;
import org.klyx.flux.plugin.recipe.Recipe;

import static org.klyx.flux.plugin.recipe.DemoExpressions.expr;

public final class BurstRecipe {
    public static Recipe create() {
        EmitterSpec emitter = new EmitterSpec(
                new EmitterSpec.Rate.Instant(Expression.constant(200)),
                new EmitterSpec.Lifetime.Once(Expression.constant(1)),
                new EmitterShape.Sphere(0.1, false),
                new Direction.Outwards(),
                256);
        ParticleSpec particle = ParticleSpec.basic()
                .lifetime(expr("0.8 + variable.particle_random_1 * 0.6"))
                .speed(expr("3 + variable.particle_random_2 * 3"))
                .drag(1.5)
                .size(Expression.constant(0.1))
                .color(Gradient.of(0xFFFFFF, 0xFFD23B, 0xFF3B3B));
        return new Recipe(emitter, particle, 1);
    }
}
