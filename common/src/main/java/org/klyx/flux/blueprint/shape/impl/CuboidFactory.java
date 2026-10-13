package org.klyx.flux.blueprint.shape.impl;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.shape.NestedShapes;
import org.klyx.flux.blueprint.shape.ShapeFactory;
import org.klyx.flux.shape.Cuboid;
import org.klyx.flux.shape.Shape;

public final class CuboidFactory implements ShapeFactory {
    @Override
    public String name() {
        return "cuboid";
    }

    @Override
    public String[] keys() {
        return new String[] {"type", "size"};
    }

    @Override
    public Shape read(Node node, NestedShapes nested) {
        return new Cuboid(node.requireVec3("size", true));
    }
}
