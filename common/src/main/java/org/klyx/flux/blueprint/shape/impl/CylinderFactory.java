package org.klyx.flux.blueprint.shape.impl;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.shape.NestedShapes;
import org.klyx.flux.blueprint.shape.ShapeFactory;
import org.klyx.flux.shape.Cylinder;
import org.klyx.flux.shape.Shape;

public final class CylinderFactory implements ShapeFactory {
    @Override
    public String name() {
        return "cylinder";
    }

    @Override
    public String[] keys() {
        return new String[] {"type", "radius", "height"};
    }

    @Override
    public Shape read(Node node, NestedShapes nested) {
        return new Cylinder(node.requireNumber("radius"), node.requireNumber("height"));
    }
}
