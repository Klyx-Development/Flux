package org.klyx.flux.sim.spec;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.expr.Expression;
import org.klyx.flux.sim.collision.Collision;
import org.klyx.flux.sim.force.Force;

import java.util.List;

public record Dynamics(
        Expression lifetime, Expression speed, @Nullable List<Expression> axisSpeed,
        List<Expression> acceleration, Expression drag, List<Force> forces,
        @Nullable Collision collision, @Nullable Parametric parametric
) {
    public Dynamics {
        axisSpeed = axisSpeed == null ? null : Triples.require("axisSpeed", axisSpeed);
        acceleration = Triples.require("acceleration", acceleration);
        forces = List.copyOf(forces);
    }
}
