package org.klyx.flux.shader;

import org.klyx.flux.sample.PointCloud;

public record GradientShader(Gradient gradient, Mode mode, double phase) implements PointShader {
    public GradientShader(Gradient gradient, Mode mode) {
        this(gradient, mode, 0);
    }

    public GradientShader withPhase(double phase) {
        return new GradientShader(gradient, mode, phase);
    }

    @Override
    public StyledCloud shade(PointCloud cloud) {
        int count = cloud.size();
        double[] metrics = new double[count];
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < count; i++) {
            double value = metric(cloud, i);
            metrics[i] = value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        double range = max - min;
        int[] rgb = new int[count];

        for (int i = 0; i < count; i++) {
            double progress = range < 1.0e-9 ? 0 : (metrics[i] - min) / range;
            if (phase != 0) {
                progress += phase;
                progress -= Math.floor(progress);
            }

            rgb[i] = gradient.at(progress);
        }

        return new StyledCloud(cloud, rgb);
    }

    private double metric(PointCloud cloud, int index) {
        return switch (mode) {
            case X -> cloud.x(index);
            case Y -> cloud.y(index);
            case Z -> cloud.z(index);
            case RADIAL -> Math.sqrt(
                    cloud.x(index) * cloud.x(index) + cloud.y(index) * cloud.y(index) + cloud.z(index) * cloud.z(index));
        };
    }

    public enum Mode {
        X,
        Y,
        Z,
        RADIAL
    }
}
