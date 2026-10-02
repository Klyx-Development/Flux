package org.klyx.flux.sim.collision;

import org.klyx.flux.expr.Expression;

public record Collision(Expression enabled, double restitution, double drag, double radius, boolean expireOnContact) {
    public static final double DEFAULT_RADIUS = 0.1;

    public Collision {
        if (!(restitution >= 0 && restitution <= 1)) {
            throw new IllegalArgumentException("restitution has to be between 0 and 1: " + restitution);
        }

        if (!(drag >= 0) || !Double.isFinite(drag)) {
            throw new IllegalArgumentException("drag has to be greater than or equal to zero and finite: " + drag);
        }

        if (!(radius >= 0) || !Double.isFinite(radius)) {
            throw new IllegalArgumentException("radius has to be greater than or equal to zero and finite: " + radius);
        }
    }

    public static Collision bouncy(double restitution) {
        return new Collision(Expression.constant(1), restitution, 0, DEFAULT_RADIUS, false);
    }
}
