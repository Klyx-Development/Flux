package org.klyx.flux.shader;

import org.klyx.flux.sample.PointCloud;

import java.util.Arrays;

public record SolidShader(int rgb) implements PointShader {
    @Override
    public StyledCloud shade(PointCloud cloud) {
        int[] colours = new int[cloud.size()];
        Arrays.fill(colours, rgb);

        return new StyledCloud(cloud, colours);
    }
}
