package org.klyx.flux.shape;

import org.klyx.flux.sample.PointCloud;
import org.klyx.flux.sample.SamplingStyle;

public record Polygon(int sides, double radius) implements Shape {
    public Polygon {
        if (sides < 3) throw new IllegalArgumentException("sides must be at least 3: " + sides);
        Shape.requirePositive("radius", radius);
    }

    @Override
    public PointCloud sample(SamplingStyle style, double density) {
        Shape.requireDensity(density);
        double[] us = new double[sides];
        double[] vs = new double[sides];

        for (int i = 0; i < sides; i++) {
            double angle = 2 * Math.PI * i / sides;
            us[i] = Math.cos(angle) * radius;
            vs[i] = Math.sin(angle) * radius;
        }

        return Loops.sample(us, vs, Loops.Plane.XZ, style, density);
    }
}
