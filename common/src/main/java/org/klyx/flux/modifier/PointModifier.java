package org.klyx.flux.modifier;

import org.klyx.flux.sample.PointCloud;

@FunctionalInterface
public interface PointModifier {
    PointCloud apply(PointCloud cloud);

    default PointModifier then(PointModifier next) {
        return cloud -> next.apply(apply(cloud));
    }
}
