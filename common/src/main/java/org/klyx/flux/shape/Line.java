package org.klyx.flux.shape;

import org.klyx.flux.math.Vec3;
import org.klyx.flux.sample.PointCloud;
import org.klyx.flux.sample.SamplingStyle;

public record Line(Vec3 from, Vec3 to) implements Shape {
    @Override
    public PointCloud sample(SamplingStyle style, double density) {
        Shape.requireDensity(density);
        int steps = Shape.steps(from.distance(to), density);
        PointCloud.Builder builder = PointCloud.builder();

        for (int i = 0; i <= steps; i++) {
            double progress = (double) i / steps;
            builder.add(
                    from.x() + (to.x() - from.x()) * progress,
                    from.y() + (to.y() - from.y()) * progress,
                    from.z() + (to.z() - from.z()) * progress);
        }

        return builder.build();
    }
}
