package org.klyx.flux.blueprint.shape.impl;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.shape.NestedShapes;
import org.klyx.flux.blueprint.shape.ShapeFactory;
import org.klyx.flux.shape.Helix;
import org.klyx.flux.shape.Shape;

public final class HelixFactory implements ShapeFactory {
    @Override
    public String name() {
        return "helix";
    }

    @Override
    public String[] keys() {
        return new String[] {"type", "radius", "height", "turns"};
    }

    @Override
    public Shape read(Node node, NestedShapes nested) {
        return new Helix(node.requireNumber("radius"), node.requireNumber("height"), node.number("turns", 1));
    }
}
