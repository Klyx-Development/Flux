package org.klyx.flux.shape;

import org.klyx.flux.sample.PointCloud;
import org.klyx.flux.sample.SamplingStyle;

public record Torus(double major, double minor) implements Shape {
    public Torus {
        Shape.requirePositive("major", major);
        Shape.requirePositive("minor", minor);

        if (minor >= major) {
            throw new IllegalArgumentException("minor has to be smaller than major: " + minor + " >= " + major);
        }
    }

    @Override
    public PointCloud sample(SamplingStyle style, double density) {
        Shape.requireDensity(density);
        PointCloud.Builder builder = PointCloud.builder();

        switch (style) {
            case OUTLINE -> {
                Circle.ring(builder, major + minor, 0, density);
                Circle.ring(builder, major - minor, 0, density);
                Circle.ring(builder, major, minor, density);
                Circle.ring(builder, major, -minor, density);
            }
            case SURFACE -> surface(builder, minor, density);
            case FILLED -> {
                int shells = Shape.steps(minor, density);
                for (int i = 1; i <= shells; i++) {
                    surface(builder, minor * i / shells, density);
                }

                Circle.ring(builder, major, 0, density);
            }
        }

        return builder.build();
    }

    private void surface(PointCloud.Builder builder, double tubeRadius, double density) {
        int rings = Shape.steps(2 * Math.PI * tubeRadius, density);

        for (int i = 0; i < rings; i++) {
            double angle = 2 * Math.PI * i / rings;
            Circle.ring(builder, major + tubeRadius * Math.cos(angle), tubeRadius * Math.sin(angle), density);
        }
    }
}
