package org.klyx.flux.blueprint.reader.system;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.expr.Expression;
import org.klyx.flux.importer.snowstorm.emitter.DirectionReader;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.importer.snowstorm.reading.Values;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.sim.emission.Direction;
import org.klyx.flux.sim.emission.EmitterShape;
import org.klyx.flux.sim.spec.EmitterSpec;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EmitterSpecReader {
    private final ReadingContext context;
    private final Values values;
    private final DirectionReader directions = new DirectionReader();

    public EmitterSpecReader(ReadingContext context) {
        this.context = context;
        this.values = context.values();
    }

    public @Nullable EmitterSpec read(Node node) {
        if (!node.isObject()) {
            node.error("an emitter must be an object but got " + node.describe() + " instead");
            return null;
        }

        node.onlyAllow("rate", "lifetime", "shape", "direction", "offset", "max_particles");
        int before = node.problemCount();

        EmitterSpec.Rate rate = rate(node.child("rate"), node);
        EmitterSpec.Lifetime lifetime = lifetime(node.child("lifetime"));
        EmitterShape shape = shape(node.child("shape"));
        Direction direction = direction(node);
        List<Expression> offset = this.values.vector(node.child("offset"), Values.zeroVector());
        int capacity = capacity(node);

        if (node.problemCount() > before || rate == null) return null;
        return new EmitterSpec(rate, lifetime, shape, direction, capacity, offset, Map.of());
    }

    private EmitterSpec.@Nullable Rate rate(@Nullable Node node, Node emitter) {
        if (node == null) {
            emitter.error("missing \"rate\", e.g. {\"type\": \"steady\", \"per_second\": 30}");
            return null;
        }

        String type = type(node, "steady, instant");
        if (type == null) return null;

        return switch (type) {
            case "steady" -> {
                node.onlyAllow("type", "per_second");
                yield new EmitterSpec.Rate.Steady(this.values.expression(node.child("per_second"), "1"));
            }
            case "instant" -> {
                node.onlyAllow("type", "count");
                yield new EmitterSpec.Rate.Instant(this.values.expression(node.child("count"), "10"));
            }
            default -> unknown(node, type, "steady, instant");
        };
    }

    private EmitterSpec.Lifetime lifetime(@Nullable Node node) {
        if (node == null) return new EmitterSpec.Lifetime.Once(Expression.constant(10));

        String type = type(node, "once, looping");
        if (type == null) return new EmitterSpec.Lifetime.Once(Expression.constant(10));

        return switch (type) {
            case "looping" -> {
                node.onlyAllow("type", "active", "sleep");
                yield new EmitterSpec.Lifetime.Looping(
                        this.values.expression(node.child("active"), "1"),
                        this.values.expression(node.child("sleep"), "0"));
            }
            case "once" -> {
                node.onlyAllow("type", "active");
                yield new EmitterSpec.Lifetime.Once(this.values.expression(node.child("active"), "10"));
            }
            default -> {
                unknown(node, type, "once, looping");
                yield new EmitterSpec.Lifetime.Once(Expression.constant(10));
            }
        };
    }

    private EmitterShape shape(@Nullable Node node) {
        EmitterShape point = new EmitterShape.Point();
        if (node == null) return point;

        String type = type(node, "point, sphere, box, disc");
        if (type == null) return point;

        try {
            return switch (type) {
                case "point" -> {
                    node.onlyAllow("type");
                    yield point;
                }
                case "sphere" -> {
                    node.onlyAllow("type", "radius", "surface_only");
                    yield new EmitterShape.Sphere(this.values.nonNegative(node, "radius", 1), node.bool("surface_only", false));
                }
                case "box" -> {
                    node.onlyAllow("type", "half_size", "surface_only");
                    yield new EmitterShape.Box(
                            this.values.constantVector(node, "half_size", new Vec3(1, 1, 1)), node.bool("surface_only", false));
                }
                case "disc" -> {
                    node.onlyAllow("type", "radius", "surface_only", "normal");
                    yield new EmitterShape.Disc(
                            this.values.nonNegative(node, "radius", 1), node.bool("surface_only", false),
                            this.values.constantVector(node, "normal", new Vec3(0, 1, 0)));
                }
                default -> {
                    unknown(node, type, "point, sphere, box, disc");
                    yield point;
                }
            };
        } catch (IllegalArgumentException e) {
            node.error(String.valueOf(e.getMessage()));
            return point;
        }
    }

    private Direction direction(Node emitter) {
        Direction fallback = new Direction.Outwards();
        Node node = emitter.child("direction");
        if (node == null) return fallback;

        if (!node.isObject()) return this.directions.read(this.context, emitter, fallback);

        node.onlyAllow("type", "axis", "angle");
        String type = type(node, "cone");
        if (type == null) return fallback;

        if (!type.equals("cone")) {
            unknown(node, type, "cone");
            return fallback;
        }

        try {
            return new Direction.Cone(this.values.constantVector(node, "axis", new Vec3(0, 1, 0)), node.number("angle", 30));
        } catch (IllegalArgumentException e) {
            node.error(String.valueOf(e.getMessage()));
            return fallback;
        }
    }

    private int capacity(Node node) {
        double wanted = node.number("max_particles", EmitterSpec.DEFAULT_MAX_PARTICLES);
        boolean whole = wanted == Math.rint(wanted);

        if (!whole || wanted < 1 || wanted > EmitterSpec.MAX_PARTICLES) {
            Node child = node.child("max_particles");
            if (child != null) {
                child.error("max_particles has to be a whole number from 1 to " + EmitterSpec.MAX_PARTICLES + " but is " + wanted);
            }

            return EmitterSpec.DEFAULT_MAX_PARTICLES;
        }

        return (int) wanted;
    }

    private static @Nullable String type(Node node, String allowed) {
        if (!node.isObject()) {
            node.error("expected an object with a \"type\" (" + allowed + ") but got " + node.describe());
            return null;
        }

        Node type = node.child("type");
        if (type == null) {
            node.error("missing \"type\", use one of: " + allowed);
            return null;
        }

        if (!type.isString()) {
            type.error("expected one of: " + allowed + " but got " + type.describe());
            return null;
        }

        return type.asString("").trim().toLowerCase(Locale.ROOT);
    }

    private static <T> @Nullable T unknown(Node node, String type, String allowed) {
        Node child = node.child("type");
        (child != null ? child : node).error("\"" + type + "\" isn't valid, use one of: " + allowed);

        return null;
    }
}
