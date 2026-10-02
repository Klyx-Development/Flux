package org.klyx.flux.sim.spec;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.expr.Expression;
import org.klyx.flux.shader.Gradient;

import java.util.List;

public record Tinting(Gradient color, @Nullable Expression colorInterpolant, @Nullable List<Expression> colorExpressions) {
    public Tinting {
        colorExpressions = colorExpressions == null ? null : Triples.require("colorExpressions", colorExpressions);
    }
}
