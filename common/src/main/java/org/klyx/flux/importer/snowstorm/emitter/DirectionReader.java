package org.klyx.flux.importer.snowstorm.emitter;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.expr.Expression;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.importer.snowstorm.reading.Values;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.sim.emission.Direction;

import java.util.List;
import java.util.Locale;

public final class DirectionReader {
    public Direction read(ReadingContext context, Node shape, Direction fallback) {
        Node node = shape.child("direction");
        if (node == null) return fallback;

        if (node.isString()) {
            return switch (node.asString("").toLowerCase(Locale.ROOT)) {
                case "outwards" -> new Direction.Outwards();
                case "inwards" -> new Direction.Inwards();
                default -> {
                    node.error("\"" + node.asString("") + "\" isn't a direction. Use \"inwards\", \"outwards\" or [x, y, z]");
                    yield fallback;
                }
            };
        }

        if (!node.isArray()) {
            node.error("expected \"inwards\", \"outwards\" or a list but got " + node.describe());
            return fallback;
        }

        List<Expression> parts = context.values().vector(node, Values.zeroVector());
        if (parts.stream().allMatch(Expression::isConstant)) {
            Vec3 vector = new Vec3(parts.get(0).evalConstant(), parts.get(1).evalConstant(), parts.get(2).evalConstant());
            return vector.equals(Vec3.ZERO) ? new Direction.None() : new Direction.Fixed(vector);
        }

        return new Direction.Computed(parts);
    }
}
