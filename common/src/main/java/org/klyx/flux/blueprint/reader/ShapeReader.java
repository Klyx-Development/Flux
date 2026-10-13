package org.klyx.flux.blueprint.reader;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.BlueprintProblem;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.shape.NestedShapes;
import org.klyx.flux.blueprint.shape.ShapeFactory;
import org.klyx.flux.blueprint.shape.ShapeRegistry;
import org.klyx.flux.shape.Shape;

import java.util.List;

public final class ShapeReader implements NestedShapes {
    private final List<BlueprintProblem> problems;
    private final int depth;

    private ShapeReader(List<BlueprintProblem> problems, int depth) {
        this.problems = problems;
        this.depth = depth;
    }

    public static @Nullable Shape read(Node node, List<BlueprintProblem> problems) {
        return new ShapeReader(problems, 0).readHere(node);
    }

    @Override
    public int depth() {
        return this.depth;
    }

    @Override
    public @Nullable Shape read(Node node) {
        return new ShapeReader(this.problems, this.depth + 1).readHere(node);
    }

    private @Nullable Shape readHere(Node node) {
        if (!node.isObject()) {
            node.error("expected an object like {\"type\": \"sphere\", \"radius\": 2} but got " + node.describe() + " instead");
            return null;
        }

        int before = this.problems.size();
        ShapeFactory factory = ShapeRegistry.find(node);
        if (factory == null) return null;

        node.onlyAllow(factory.keys());
        Shape shape = build(node, factory);

        return this.problems.size() == before ? shape : null;
    }

    private @Nullable Shape build(Node node, ShapeFactory factory) {
        int before = node.problemCount();
        try {
            Shape shape = factory.read(node, this);
            return node.problemCount() > before ? null : shape;
        } catch (IllegalArgumentException e) {
            if (node.problemCount() == before) {
                node.error(String.valueOf(e.getMessage()));
            }

            return null;
        }
    }
}
