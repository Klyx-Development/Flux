package org.klyx.flux.shader;

import org.klyx.flux.math.Transform;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.sample.PointCloud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class StyledCloud {
    private final PointCloud points;
    private final int[] rgb;

    public StyledCloud(PointCloud points, int[] rgb) {
        if (rgb.length != points.size()) {
            throw new IllegalArgumentException("expected " + points.size() + " colours but got " + rgb.length);
        }

        this.points = points;
        this.rgb = rgb.clone();
    }

    public PointCloud points() {
        return this.points;
    }

    public int size() {
        return this.points.size();
    }

    public int rgb(int index) {
        return this.rgb[index];
    }

    public static StyledCloud concat(List<StyledCloud> clouds) {
        List<PointCloud> points = new ArrayList<>(clouds.size());
        int total = 0;

        for (StyledCloud cloud : clouds) {
            points.add(cloud.points);
            total += cloud.size();
        }

        int[] rgb = new int[total];
        int offset = 0;

        for (StyledCloud cloud : clouds) {
            System.arraycopy(cloud.rgb, 0, rgb, offset, cloud.rgb.length);
            offset += cloud.rgb.length;
        }

        return new StyledCloud(PointCloud.concat(points), rgb);
    }

    public StyledCloud transformed(Transform transform) {
        return new StyledCloud(this.points.transformed(transform), this.rgb);
    }

    public StyledCloud stretched(Vec3 scale, Vec3 offset) {
        return new StyledCloud(this.points.stretched(scale, offset), this.rgb);
    }

}
