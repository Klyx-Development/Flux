package org.klyx.flux.shader;

import org.klyx.flux.sample.PointCloud;

@FunctionalInterface
public interface PointShader {
    StyledCloud shade(PointCloud cloud);
}
