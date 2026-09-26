package org.klyx.flux.shape;

import org.klyx.flux.sample.PointCloud;
import org.klyx.flux.sample.SamplingStyle;

public record Arc(double radius, double angle) implements Shape {
    private static final double FULL_TURN = 2 * Math.PI;

    public Arc {
        Shape.requirePositive("radius", radius);
        Shape.requirePositive("angle", angle);

        if (angle > FULL_TURN) {
            throw new IllegalArgumentException("angle must be at most a full turn: " + angle);
        }
    }

    @Override
    public PointCloud sample(SamplingStyle style, double density) {
        Shape.requireDensity(density);
        PointCloud.Builder builder = PointCloud.builder();

        if (style == SamplingStyle.OUTLINE) {
            arc(builder, radius, density);
        } else {
            int arcs = Shape.steps(radius, density);
            for (int i = 1; i <= arcs; i++) {
                arc(builder, radius * i / arcs, density);
            }

            builder.add(0, 0, 0);
        }

        return builder.build();
    }

    private void arc(PointCloud.Builder builder, double arcRadius, double density) {
        int steps = Shape.steps(arcRadius * angle, density);
        int last = angle >= FULL_TURN - 1.0e-9 ? steps - 1 : steps;

        for (int i = 0; i <= last; i++) {
            double current = angle * i / steps;
            builder.add(Math.cos(current) * arcRadius, 0, Math.sin(current) * arcRadius);
        }
    }
}
