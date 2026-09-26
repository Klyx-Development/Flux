package org.klyx.flux.shape;

import org.klyx.flux.sample.PointCloud;
import org.klyx.flux.sample.SamplingStyle;

public record Circle(double radius) implements Shape {
    public Circle {
        Shape.requirePositive("radius", radius);
    }

    @Override
    public PointCloud sample(SamplingStyle style, double density) {
        Shape.requireDensity(density);
        PointCloud.Builder builder = PointCloud.builder();

        if (style == SamplingStyle.OUTLINE) {
            ring(builder, radius, 0, density);
        } else {
            disc(builder, radius, 0, density);
        }

        return builder.build();
    }

    public static void ring(PointCloud.Builder builder, double radius, double y, double density) {
        int steps = Shape.steps(2 * Math.PI * radius, density);
        for (int i = 0; i < steps; i++) {
            double angle = 2 * Math.PI * i / steps;
            builder.add(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
        }
    }

    public static void disc(PointCloud.Builder builder, double radius, double y, double density) {
        int rings = Shape.steps(radius, density);
        for (int i = 1; i <= rings; i++) {
            ring(builder, radius * i / rings, y, density);
        }

        builder.add(0, y, 0);
    }
}
