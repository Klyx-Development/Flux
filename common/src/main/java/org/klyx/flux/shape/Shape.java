package org.klyx.flux.shape;

import org.klyx.flux.sample.PointCloud;
import org.klyx.flux.sample.SamplingStyle;

public sealed interface Shape
        permits Line, Circle, Sphere, Helix, Cuboid, Cylinder, Torus, Polygon, Star, Heart, Arc, Composite {
    int MAX_STEPS = 20_000;

    PointCloud sample(SamplingStyle style, double density);

    static void requireDensity(double density) {
        if (!(density > 0) || !Double.isFinite(density)) {
            throw new IllegalArgumentException("density has to be positive and finite: " + density);
        }
    }

    static int steps(double length, double density) {
        return (int) Math.clamp(Math.ceil(length * density), 1, MAX_STEPS);
    }

    static void requirePositive(String name, double value) {
        if (!(value > 0) || !Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " has to be positive and finite: " + value);
        }
    }

    static void requirePositiveInfinite(String name, double value) {
        if (!(value > 0)) {
            throw new IllegalArgumentException(name + " has to be positive: " + value);
        }
    }
}
