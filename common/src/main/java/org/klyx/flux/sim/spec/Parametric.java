package org.klyx.flux.sim.spec;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.expr.Expression;

import java.util.List;

public record Parametric(@Nullable List<Expression> position, @Nullable Expression rotation) {
    public Parametric {
        if (position != null) {
            if (position.size() != 3) {
                throw new IllegalArgumentException("position needs 3 expressions but has " + position.size());
            }

            position = List.copyOf(position);
        }
    }
}
