package org.klyx.flux.blueprint.shape;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.shape.impl.ArcFactory;
import org.klyx.flux.blueprint.shape.impl.CircleFactory;
import org.klyx.flux.blueprint.shape.impl.CompositeFactory;
import org.klyx.flux.blueprint.shape.impl.CuboidFactory;
import org.klyx.flux.blueprint.shape.impl.CylinderFactory;
import org.klyx.flux.blueprint.shape.impl.HeartFactory;
import org.klyx.flux.blueprint.shape.impl.HelixFactory;
import org.klyx.flux.blueprint.shape.impl.LineFactory;
import org.klyx.flux.blueprint.shape.impl.PolygonFactory;
import org.klyx.flux.blueprint.shape.impl.SphereFactory;
import org.klyx.flux.blueprint.shape.impl.StarFactory;
import org.klyx.flux.blueprint.shape.impl.TorusFactory;
import org.klyx.flux.blueprint.support.named.NamedRegistry;

import java.util.List;

public final class ShapeRegistry {
    private static final NamedRegistry<ShapeFactory> FACTORIES = new NamedRegistry<>(List.of(
            new LineFactory(),
            new CircleFactory(),
            new SphereFactory(),
            new HelixFactory(),
            new CuboidFactory(),
            new CylinderFactory(),
            new TorusFactory(),
            new PolygonFactory(),
            new StarFactory(),
            new HeartFactory(),
            new ArcFactory(),
            new CompositeFactory()
    ));

    public static @Nullable ShapeFactory find(Node node) {
        return FACTORIES.find(node);
    }
}
