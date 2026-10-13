package org.klyx.flux.blueprint.shape.impl;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.shape.NestedShapes;
import org.klyx.flux.blueprint.shape.ShapeFactory;
import org.klyx.flux.shape.Arc;
import org.klyx.flux.shape.Shape;

public final class ArcFactory implements ShapeFactory {
    @Override
    public String name() {
        return "arc";
    }

    @Override
    public String[] keys() {
        return new String[] {"type", "radius", "angle"};
    }

    @Override
    public Shape read(Node node, NestedShapes nested) {
        return new Arc(node.requireNumber("radius"), Math.toRadians(node.requireNumber("angle")));
    }
}
