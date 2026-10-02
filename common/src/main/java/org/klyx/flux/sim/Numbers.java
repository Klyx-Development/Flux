package org.klyx.flux.sim;

public final class Numbers {
    public static final double MIN_LIFETIME = 1.0e-3;

    public static double finiteOr(double value, double fallback) {
        return Double.isFinite(value) ? value : fallback;
    }

    public static double nonNegative(double value) {
        return value > 0 && Double.isFinite(value) ? value : 0;
    }
}
