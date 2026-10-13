package org.klyx.flux.blueprint.shape.impl;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.shape.NestedShapes;
import org.klyx.flux.blueprint.shape.ShapeFactory;
import org.klyx.flux.shape.Shape;
import org.klyx.flux.shape.Torus;

public final class TorusFactory implements ShapeFactory {
    @Override
    public String name() {
        return "torus";
    }

    @Override
    public String[] keys() {
        return new String[] {"type", "major", "minor"};
    }

    @Override
    public Shape read(Node node, NestedShapes nested) {
        return new Torus(node.requireNumber("major"), node.requireNumber("minor"));
    }
}
