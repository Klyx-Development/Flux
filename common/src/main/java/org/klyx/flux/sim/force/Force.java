package org.klyx.flux.sim.force;

import org.klyx.flux.expr.Expression;
import org.klyx.flux.math.Vec3;

public sealed interface Force permits Force.Attractor, Force.Axis, Force.SetSpeed {
    double SOFTENING = 0.6;

    record Attractor(Vec3 center, Expression strength) implements Force { }

    record Axis(Vec3 axis, Expression along, Expression toward) implements Force {
        public Axis {
            Vec3 unit = axis.normalize();
            if (unit.equals(Vec3.ZERO)) throw new IllegalArgumentException("an axis can't be zero");

            axis = unit;
        }
    }

    record SetSpeed(Expression speed) implements Force {
    }
}
