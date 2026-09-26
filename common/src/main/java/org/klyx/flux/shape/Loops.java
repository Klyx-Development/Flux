package org.klyx.flux.shape;

import org.klyx.flux.sample.PointCloud;
import org.klyx.flux.sample.SamplingStyle;

public final class Loops {
    public static PointCloud sample(double[] us, double[] vs, Plane plane, SamplingStyle style, double density) {
        PointCloud.Builder builder = PointCloud.builder();

        if (style == SamplingStyle.OUTLINE) {
            loop(builder, us, vs, 1.0, plane, density);
            return builder.build();
        }

        double reach = 0;
        for (int i = 0; i < us.length; i++) {
            reach = Math.max(reach, Math.hypot(us[i], vs[i]));
        }

        int rings = Shape.steps(reach, density);
        for (int i = 1; i <= rings; i++) {
            loop(builder, us, vs, (double) i / rings, plane, density);
        }

        add(builder, 0, 0, plane);
        return builder.build();
    }

    private static void loop(
            PointCloud.Builder builder, double[] us, double[] vs, double scale, Plane plane, double density
    ) {
        int count = us.length;

        for (int edge = 0; edge < count; edge++) {
            int next = (edge + 1) % count;
            double fromU = us[edge] * scale;
            double fromV = vs[edge] * scale;
            double toU = us[next] * scale;
            double toV = vs[next] * scale;

            int steps = Shape.steps(Math.hypot(toU - fromU, toV - fromV), density);
            for (int i = 0; i < steps; i++) {
                double progress = (double) i / steps;
                add(builder, fromU + (toU - fromU) * progress, fromV + (toV - fromV) * progress, plane);
            }
        }
    }

    private static void add(PointCloud.Builder builder, double u, double v, Plane plane) {
        if (plane == Plane.XZ) {
            builder.add(u, 0, v);
        } else {
            builder.add(u, v, 0);
        }
    }

    public enum Plane {
        XZ,
        XY
    }
}
