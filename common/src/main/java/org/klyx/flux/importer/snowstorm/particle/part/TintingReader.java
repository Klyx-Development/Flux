package org.klyx.flux.importer.snowstorm.particle.part;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.expr.Expression;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.shader.Gradient;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.util.List;

public final class TintingReader implements ParticlePart {
    @Override
    public void apply(ReadingContext context, ParticleSpec.Builder particle) {
        Node node = context.component("particle_appearance_tinting");
        if (node == null) return;

        node.onlyAllow("color");

        Node color = node.child("color");
        if (color == null) return;

        ColorReader colors = new ColorReader(context);
        if (color.isString()) {
            particle.color(Gradient.of(colors.hex(color)));
            return;
        }

        if (color.isArray()) {
            List<Expression> channels = colors.channels(color);
            particle.colorExpressions(channels);
            return;
        }

        if (!color.isObject()) {
            color.error("expected a colour like \"#RRGGBB\", [r, g, b, a] or a gradient but got " + color.describe());
            return;
        }

        color.onlyAllow("interpolant", "gradient");

        Node interpolant = color.child("interpolant");
        Node gradient = color.child("gradient");
        if (gradient == null) {
            color.error("missing \"gradient\"");
            return;
        }

        if (interpolant != null) particle.colorInterpolant(context.values().expression(interpolant, "0"));

        Gradient parsed = colors.gradient(gradient);
        if (parsed != null) particle.color(parsed);
    }
}
