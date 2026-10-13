package org.klyx.flux.blueprint.shape.impl;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.shape.NestedShapes;
import org.klyx.flux.blueprint.shape.ShapeFactory;
import org.klyx.flux.shape.Shape;
import org.klyx.flux.shape.Star;

public final class StarFactory implements ShapeFactory {
    @Override
    public String name() {
        return "star";
    }

    @Override
    public String[] keys() {
        return new String[] {"type", "points", "outer", "inner"};
    }

    @Override
    public Shape read(Node node, NestedShapes nested) {
        return new Star(node.requireInt("points"), node.requireNumber("outer"), node.requireNumber("inner"));
    }
}
