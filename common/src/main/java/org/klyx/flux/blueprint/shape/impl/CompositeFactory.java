package org.klyx.flux.blueprint.shape.impl;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.reader.TransformReader;
import org.klyx.flux.blueprint.shape.NestedShapes;
import org.klyx.flux.blueprint.shape.ShapeFactory;
import org.klyx.flux.math.Transform;
import org.klyx.flux.shape.Composite;
import org.klyx.flux.shape.Shape;

import java.util.ArrayList;
import java.util.List;

public final class CompositeFactory implements ShapeFactory {
    @Override
    public String name() {
        return "composite";
    }

    @Override
    public String[] keys() {
        return new String[] {"type", "parts"};
    }

    @Override
    public @Nullable Shape read(Node node, NestedShapes nested) {
        if (nested.depth() >= NestedShapes.MAX_NESTING) {
            node.error("a composite shape can't be nested more than " + NestedShapes.MAX_NESTING + " times!");
            return null;
        }

        Node partsNode = node.child("parts");
        if (partsNode == null) {
            node.error("missing \"parts\"");
            return null;
        }

        List<Composite.Part> parts = new ArrayList<>();
        for (Node part : partsNode.items()) {
            part.onlyAllow("shape", "transform");

            Node shapeNode = part.child("shape");
            if (shapeNode == null) {
                part.error("missing \"shape\"");
                continue;
            }

            Shape shape = nested.read(shapeNode);
            Node transformNode = part.child("transform");
            Transform transform = transformNode == null ? Transform.IDENTITY : TransformReader.read(transformNode);
            if (shape != null) parts.add(new Composite.Part(shape, transform));
        }

        if (parts.isEmpty()) {
            if (partsNode.isArray()) partsNode.error("a composite shape needs at least one part");
            return null;
        }

        return new Composite(parts);
    }
}
