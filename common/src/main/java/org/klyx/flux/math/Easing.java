package org.klyx.flux.math;

import java.util.function.DoubleUnaryOperator;

public enum Easing {
    LINEAR(t -> t),
    IN_QUAD(t -> t * t),
    OUT_QUAD(t -> 1 - (1 - t) * (1 - t)),
    IN_OUT_QUAD(t -> t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2),
    IN_CUBIC(t -> t * t * t),
    OUT_CUBIC(t -> 1 - Math.pow(1 - t, 3)),
    IN_OUT_CUBIC(t -> t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2),
    IN_SINE(t -> 1 - Math.cos(t * Math.PI / 2)),
    OUT_SINE(t -> Math.sin(t * Math.PI / 2)),
    IN_OUT_SINE(t -> -(Math.cos(Math.PI * t) - 1) / 2);

    private final DoubleUnaryOperator curve;

    Easing(DoubleUnaryOperator curve) {
        this.curve = curve;
    }

    public double apply(double progress) {
        if (progress <= 0) return 0;
        if (progress >= 1) return 1;

        return this.curve.applyAsDouble(progress);
    }
}
