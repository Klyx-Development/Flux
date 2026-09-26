package org.klyx.flux.shape;

import org.klyx.flux.sample.PointCloud;
import org.klyx.flux.sample.SamplingStyle;

public record Helix(double radius, double height, double turns) implements Shape {
    public Helix {
        Shape.requirePositiveInfinite("radius", radius);
        Shape.requirePositiveInfinite("height", height);
        Shape.requirePositiveInfinite("turns", turns);
    }

    @Override
    public PointCloud sample(SamplingStyle style, double density) {
        Shape.requireDensity(density);
        double length = Math.hypot(2 * Math.PI * radius * turns, height);
        int steps = Shape.steps(length, density);
        PointCloud.Builder builder = PointCloud.builder();

        for (int i = 0; i <= steps; i++) {
            double progress = (double) i / steps;
            double angle = 2 * Math.PI * turns * progress;
            builder.add(Math.cos(angle) * radius, height * progress, Math.sin(angle) * radius);
        }

        return builder.build();
    }
}
