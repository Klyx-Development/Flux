package org.klyx.flux.shape;

import org.klyx.flux.sample.PointCloud;
import org.klyx.flux.sample.SamplingStyle;

public record Star(int points, double outer, double inner) implements Shape {
    public Star {
        if (points < 3) throw new IllegalArgumentException("points has to be at least 3: " + points);
        Shape.requirePositive("outer", outer);
        Shape.requirePositive("inner", inner);

        if (inner >= outer) {
            throw new IllegalArgumentException("inner has to be smaller than outer: " + inner + " >= " + outer);
        }
    }

    @Override
    public PointCloud sample(SamplingStyle style, double density) {
        Shape.requireDensity(density);
        int vertices = points * 2;
        double[] us = new double[vertices];
        double[] vs = new double[vertices];

        for (int i = 0; i < vertices; i++) {
            double angle = Math.PI * i / points;
            double distance = i % 2 == 0 ? outer : inner;
            us[i] = Math.cos(angle) * distance;
            vs[i] = Math.sin(angle) * distance;
        }

        return Loops.sample(us, vs, Loops.Plane.XZ, style, density);
    }
}
