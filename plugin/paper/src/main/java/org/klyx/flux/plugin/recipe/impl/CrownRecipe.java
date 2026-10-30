package org.klyx.flux.plugin.recipe.impl;

import org.klyx.flux.expr.Expression;
import org.klyx.flux.sim.emission.Direction;
import org.klyx.flux.sim.emission.EmitterShape;
import org.klyx.flux.sim.spec.DisplayKind;
import org.klyx.flux.sim.spec.EmitterSpec;
import org.klyx.flux.sim.spec.Parametric;
import org.klyx.flux.sim.spec.ParticleSpec;
import org.klyx.flux.plugin.recipe.Recipe;

import java.util.List;

import static org.klyx.flux.plugin.recipe.DemoExpressions.expr;

public final class CrownRecipe {
    public static Recipe create() {
        String angle = "(variable.particle_age * 240 + variable.particle_random_1 * 360)";
        EmitterSpec emitter = new EmitterSpec(
                new EmitterSpec.Rate.Steady(Expression.constant(10)),
                new EmitterSpec.Lifetime.Once(Expression.constant(3600)),
                new EmitterShape.Point(), new Direction.None(), 40);
        ParticleSpec particle = ParticleSpec.builder()
                .lifetime(Expression.constant(1.5))
                .parametric(new Parametric(List.of(
                        expr("0.85 * math.cos(" + angle + ")"),
                        expr("0.12 * math.sin(" + angle + " * 2)"),
                        expr("0.85 * math.sin(" + angle + ")")), null))
                .spin(Expression.constant(180))
                .size(expr("0.2 * math.clamp(math.min(variable.particle_age, variable.particle_lifetime - variable.particle_age) * 4, 0, 1)"))
                .kind(DisplayKind.ITEM).item("minecraft:nether_star").tint(false)
                .build();
        return new Recipe(emitter, particle, 2.3);
    }
}
