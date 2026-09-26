package org.klyx.flux.shape;

import org.klyx.flux.math.Vec3;
import org.klyx.flux.sample.PointCloud;
import org.klyx.flux.sample.SamplingStyle;

public record Cuboid(Vec3 size) implements Shape {
    private static final long MAX_GRID_CELLS = 4_000_000;

    public Cuboid {
        Shape.requirePositive("size.x", size.x());
        Shape.requirePositive("size.y", size.y());
        Shape.requirePositive("size.z", size.z());
    }

    @Override
    public PointCloud sample(SamplingStyle style, double density) {
        Shape.requireDensity(density);
        int stepsX = Shape.steps(size.x(), density);
        int stepsY = Shape.steps(size.y(), density);
        int stepsZ = Shape.steps(size.z(), density);

        requireGridWithinLimit(stepsX, stepsY, stepsZ);

        int minBoundary = minBoundaryAxes(style);
        PointCloud.Builder builder = PointCloud.builder();

        for (int i = 0; i <= stepsX; i++) {
            int boundaryX = onBoundary(i, stepsX);

            for (int j = 0; j <= stepsY; j++) {
                int boundaryXY = boundaryX + onBoundary(j, stepsY);

                for (int k = 0; k <= stepsZ; k++) {
                    int boundary = boundaryXY + onBoundary(k, stepsZ);
                    if (boundary < minBoundary) continue;

                    builder.add((double) i / stepsX * size.x() - size.x() / 2,
                            (double) j / stepsY * size.y() - size.y() / 2,
                            (double) k / stepsZ * size.z() - size.z() / 2);
                }
            }
        }

        return builder.build();
    }

    private static void requireGridWithinLimit(int stepsX, int stepsY, int stepsZ) {
        long cells = (long) (stepsX + 1) * (stepsY + 1) * (stepsZ + 1);
        if (cells > MAX_GRID_CELLS) {
            throw new IllegalArgumentException("a cuboid this large needs " + cells
                    + " grid cells at this density, lower density/size");
        }
    }

    private static int minBoundaryAxes(SamplingStyle style) {
        return switch (style) {
            case OUTLINE -> 2;
            case SURFACE -> 1;
            case FILLED -> 0;
        };
    }

    private static int onBoundary(int index, int steps) {
        return index == 0 || index == steps ? 1 : 0;
    }
}
