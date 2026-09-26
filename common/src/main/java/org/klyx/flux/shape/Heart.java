package org.klyx.flux.shape;

import org.klyx.flux.sample.PointCloud;
import org.klyx.flux.sample.SamplingStyle;

public record Heart(double width) implements Shape {
    private static final int SEGMENTS = 96;

    public Heart {
        Shape.requirePositive("width", width);
    }

    @Override
    public PointCloud sample(SamplingStyle style, double density) {
        Shape.requireDensity(density);
        double factor = width / 32;
        double[] us = new double[SEGMENTS];
        double[] vs = new double[SEGMENTS];

        for (int i = 0; i < SEGMENTS; i++) {
            double t = 2 * Math.PI * i / SEGMENTS;
            double sin = Math.sin(t);

            us[i] = 16 * sin * sin * sin * factor;
            vs[i] = (13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t) + 2.5)
                    * factor;
        }

        return Loops.sample(us, vs, Loops.Plane.XY, style, density);
    }
}
