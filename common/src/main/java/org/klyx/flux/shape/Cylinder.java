package org.klyx.flux.shape;

import org.klyx.flux.sample.PointCloud;
import org.klyx.flux.sample.SamplingStyle;

public record Cylinder(double radius, double height) implements Shape {
    public Cylinder {
        Shape.requirePositive("radius", radius);
        Shape.requirePositive("height", height);
    }

    @Override
    public PointCloud sample(SamplingStyle style, double density) {
        Shape.requireDensity(density);
        PointCloud.Builder builder = PointCloud.builder();

        if (style == SamplingStyle.OUTLINE) {
            Circle.ring(builder, radius, 0, density);
            Circle.ring(builder, radius, height, density);
            return builder.build();
        }

        int layers = Shape.steps(height, density);
        for (int i = 0; i <= layers; i++) {
            double y = height * i / layers;
            boolean cap = i == 0 || i == layers;

            if (style == SamplingStyle.FILLED || cap) {
                Circle.disc(builder, radius, y, density);
            } else {
                Circle.ring(builder, radius, y, density);
            }
        }

        return builder.build();
    }
}
