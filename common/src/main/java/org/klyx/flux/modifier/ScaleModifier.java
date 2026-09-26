package org.klyx.flux.modifier;

import org.klyx.flux.math.Vec3;
import org.klyx.flux.sample.PointCloud;

public record ScaleModifier(Vec3 factors) implements PointModifier {
    @Override
    public PointCloud apply(PointCloud cloud) {
        return cloud.map(point -> {
            point[0] *= factors.x();
            point[1] *= factors.y();
            point[2] *= factors.z();
        });
    }
}
