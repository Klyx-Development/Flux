package org.klyx.flux.blueprint.shape.impl;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.shape.NestedShapes;
import org.klyx.flux.blueprint.shape.ShapeFactory;
import org.klyx.flux.shape.Heart;
import org.klyx.flux.shape.Shape;

public final class HeartFactory implements ShapeFactory {
    @Override
    public String name() {
        return "heart";
    }

    @Override
    public String[] keys() {
        return new String[] {"type", "width"};
    }

    @Override
    public Shape read(Node node, NestedShapes nested) {
        return new Heart(node.requireNumber("width"));
    }
}
