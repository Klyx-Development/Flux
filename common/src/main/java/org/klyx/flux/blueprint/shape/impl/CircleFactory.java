package org.klyx.flux.blueprint.shape.impl;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.shape.NestedShapes;
import org.klyx.flux.blueprint.shape.ShapeFactory;
import org.klyx.flux.shape.Circle;
import org.klyx.flux.shape.Shape;

public final class CircleFactory implements ShapeFactory {
    @Override
    public String name() {
        return "circle";
    }

    @Override
    public String[] keys() {
        return new String[] {"type", "radius"};
    }

    @Override
    public Shape read(Node node, NestedShapes nested) {
        return new Circle(node.requireNumber("radius"));
    }
}
