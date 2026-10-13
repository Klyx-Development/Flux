package org.klyx.flux.blueprint.shape.impl;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.shape.NestedShapes;
import org.klyx.flux.blueprint.shape.ShapeFactory;
import org.klyx.flux.shape.Polygon;
import org.klyx.flux.shape.Shape;

public final class PolygonFactory implements ShapeFactory {
    @Override
    public String name() {
        return "polygon";
    }

    @Override
    public String[] keys() {
        return new String[] {"type", "sides", "radius"};
    }

    @Override
    public Shape read(Node node, NestedShapes nested) {
        return new Polygon(node.requireInt("sides"), node.requireNumber("radius"));
    }
}
