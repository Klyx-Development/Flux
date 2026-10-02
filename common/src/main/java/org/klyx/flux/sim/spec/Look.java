package org.klyx.flux.sim.spec;

import org.klyx.flux.expr.Expression;

public record Look(
        Expression size, Expression spin, Expression initialRoll,
        Billboard billboard, boolean emissive, double viewRange
) {
    public Look {
        if (!(viewRange > 0) || !Double.isFinite(viewRange)) {
            throw new IllegalArgumentException("viewRange must be positive and finite: " + viewRange);
        }
    }
}
