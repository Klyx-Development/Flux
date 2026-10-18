package org.klyx.flux.importer.snowstorm.emitter;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.importer.snowstorm.reading.Reader;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.importer.snowstorm.reading.Values;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.sim.emission.Direction;
import org.klyx.flux.sim.emission.EmitterShape;

import java.util.List;
import java.util.Locale;

public final class EmitterShapeReader implements Reader<ShapeResult> {
    private static final List<String> NAMES = List.of("emitter_shape_point", "emitter_shape_sphere", "emitter_shape_box",
            "emitter_shape_disc", "emitter_shape_custom", "emitter_shape_entity_aabb");

    private final DirectionReader directions = new DirectionReader();

    @Override
    public ShapeResult read(ReadingContext context) {
        for (String name : NAMES) {
            Node node = context.component(name);
            if (node != null) return read(context, name, node);
        }

        return ShapeResult.point();
    }

    private ShapeResult read(ReadingContext context, String name, Node node) {
        Values values = context.values();
        return switch (name) {
            case "emitter_shape_point" -> {
                node.onlyAllow("offset", "direction");
                yield result(context, node, new EmitterShape.Point());
            }
            case "emitter_shape_sphere" -> {
                node.onlyAllow("offset", "radius", "surface_only", "direction");
                yield result(context, node, new EmitterShape.Sphere(values.nonNegative(node, "radius", 1), node.bool("surface_only", false)));
            }
            case "emitter_shape_box" -> {
                node.onlyAllow("offset", "half_dimensions", "surface_only", "direction");
                Vec3 half = values.constantVector(node, "half_dimensions", new Vec3(1, 1, 1));
                yield result(context, node, new EmitterShape.Box(half, node.bool("surface_only", false)));
            }
            case "emitter_shape_disc" -> {
                node.onlyAllow("offset", "radius", "plane_normal", "surface_only", "direction");
                yield result(context, node, new EmitterShape.Disc(values.nonNegative(node, "radius", 1), node.bool("surface_only", false), planeNormal(context, node)));
            }
            case "emitter_shape_custom" -> {
                context.ignore(node, "custom emitter shapes aren't supported");
                yield ShapeResult.point();
            }
            default -> {
                context.ignore(node, "following an entity's bounding box isn't supported");
                yield ShapeResult.point();
            }
        };
    }

    private ShapeResult result(ReadingContext context, Node node, EmitterShape shape) {
        Direction direction = this.directions.read(context, node, new Direction.Outwards());
        return new ShapeResult(shape, direction, context.values().vector(node.child("offset"), Values.zeroVector()));
    }

    private Vec3 planeNormal(ReadingContext context, Node disc) {
        Node node = disc.child("plane_normal");
        if (node == null) return new Vec3(0, 1, 0);
        if (node.isString()) {
            return switch (node.asString("").toLowerCase(Locale.ROOT)) {
                case "x" -> new Vec3(1, 0, 0);
                case "y" -> new Vec3(0, 1, 0);
                case "z" -> new Vec3(0, 0, 1);
                default -> {
                    node.error("\"" + node.asString("") + "\" isn't a plane. Use \"x\", \"y\", \"z\" or [x, y, z]");
                    yield new Vec3(0, 1, 0);
                }
            };
        }
        Vec3 normal = context.values().constantVector(disc, "plane_normal", new Vec3(0, 1, 0));
        if (normal.equals(Vec3.ZERO)) {
            node.error("a plane normal can't be [0, 0, 0]");
            return new Vec3(0, 1, 0);
        }
        return normal;
    }
}
