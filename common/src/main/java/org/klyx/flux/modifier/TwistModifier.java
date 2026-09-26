package org.klyx.flux.modifier;

import org.klyx.flux.sample.PointCloud;

public record TwistModifier(double radiansPerBlock) implements PointModifier {
    @Override
    public PointCloud apply(PointCloud cloud) {
        return cloud.map(point -> {
            double angle = radiansPerBlock * point[1];
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double x = point[0];
            double z = point[2];

            point[0] = x * cos + z * sin;
            point[2] = -x * sin + z * cos;
        });
    }
}
