package org.klyx.flux.blueprint.shape.impl;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.shape.NestedShapes;
import org.klyx.flux.blueprint.shape.ShapeFactory;
import org.klyx.flux.shape.Shape;
import org.klyx.flux.shape.Sphere;

public final class SphereFactory implements ShapeFactory {
    @Override
    public String name() {
        return "sphere";
    }

    @Override
    public String[] keys() {
        return new String[] {"type", "radius"};
    }

    @Override
    public Shape read(Node node, NestedShapes nested) {
        return new Sphere(node.requireNumber("radius"));
    }
}
