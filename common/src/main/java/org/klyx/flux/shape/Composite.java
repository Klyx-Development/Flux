package org.klyx.flux.shape;

import org.klyx.flux.math.Transform;
import org.klyx.flux.sample.PointCloud;
import org.klyx.flux.sample.SamplingStyle;

import java.util.ArrayList;
import java.util.List;

public record Composite(List<Part> parts) implements Shape {
    public Composite {
        parts = List.copyOf(parts);
        if (parts.isEmpty()) throw new IllegalArgumentException("a composite shape needs at least one part");
    }

    @Override
    public PointCloud sample(SamplingStyle style, double density) {
        Shape.requireDensity(density);
        List<PointCloud> clouds = new ArrayList<>(parts.size());

        for (Part part : parts) {
            clouds.add(part.shape().sample(style, density).transformed(part.transform()));
        }

        return PointCloud.concat(clouds);
    }

    public record Part(Shape shape, Transform transform) {
        public static Part of(Shape shape) {
            return new Part(shape, Transform.IDENTITY);
        }
    }
}
