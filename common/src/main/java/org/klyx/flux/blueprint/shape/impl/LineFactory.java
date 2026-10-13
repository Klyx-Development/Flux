package org.klyx.flux.blueprint.shape.impl;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.shape.NestedShapes;
import org.klyx.flux.blueprint.shape.ShapeFactory;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.shape.Line;
import org.klyx.flux.shape.Shape;

public final class LineFactory implements ShapeFactory {
    @Override
    public String name() {
        return "line";
    }

    @Override
    public String[] keys() {
        return new String[] {"type", "from", "to"};
    }

    @Override
    public Shape read(Node node, NestedShapes nested) {
        return new Line(node.vec3("from", Vec3.ZERO, false), node.requireVec3("to", false));
    }
}
