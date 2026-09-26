package org.klyx.flux.shape;

import org.klyx.flux.sample.PointCloud;
import org.klyx.flux.sample.SamplingStyle;

public record Sphere(double radius) implements Shape {
    private static final double GOLDEN_ANGLE = Math.PI * (3 - Math.sqrt(5));

    public Sphere {
        Shape.requirePositive("radius", radius);
    }

    @Override
    public PointCloud sample(SamplingStyle style, double density) {
        Shape.requireDensity(density);
        PointCloud.Builder builder = PointCloud.builder();

        switch (style) {
            case OUTLINE -> {
                int steps = Shape.steps(2 * Math.PI * radius, density);
                for (int i = 0; i < steps; i++) {
                    double angle = 2 * Math.PI * i / steps;
                    double cos = Math.cos(angle) * radius;
                    double sin = Math.sin(angle) * radius;
                    builder.add(cos, 0, sin).add(cos, sin, 0).add(0, cos, sin);
                }
            }
            case SURFACE -> fibonacci(builder, radius, density);
            case FILLED -> {
                int shells = Shape.steps(radius, density);
                for (int i = 1; i <= shells; i++) {
                    fibonacci(builder, radius * i / shells, density);
                }

                builder.add(0, 0, 0);
            }
        }

        return builder.build();
    }

    private static void fibonacci(PointCloud.Builder builder, double radius, double density) {
        int count = Shape.steps(4 * Math.PI * radius * radius, density * density);

        for (int i = 0; i < count; i++) {
            double y = count == 1 ? 0 : 1 - 2.0 * i / (count - 1);
            double ring = Math.sqrt(Math.max(0, 1 - y * y));
            double theta = GOLDEN_ANGLE * i;
            builder.add(Math.cos(theta) * ring * radius, y * radius, Math.sin(theta) * ring * radius);
        }
    }
}
