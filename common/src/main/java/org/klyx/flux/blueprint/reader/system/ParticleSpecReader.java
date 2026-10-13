package org.klyx.flux.blueprint.reader.system;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.expr.Expression;
import org.klyx.flux.importer.snowstorm.particle.part.ColorReader;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.importer.snowstorm.reading.Values;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.shader.Gradient;
import org.klyx.flux.sim.collision.Collision;
import org.klyx.flux.sim.force.Force;
import org.klyx.flux.sim.spec.Billboard;
import org.klyx.flux.sim.spec.DisplayKind;
import org.klyx.flux.sim.spec.Parametric;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ParticleSpecReader {
    private static final Pattern ITEM_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    private final ReadingContext context;
    private final Values values;

    public ParticleSpecReader(ReadingContext context) {
        this.context = context;
        this.values = context.values();
    }

    public @Nullable ParticleSpec read(@Nullable Node node) {
        if (node == null) return ParticleSpec.basic();

        if (!node.isObject()) {
            node.error("a particle must be an object but this is " + node.describe());
            return null;
        }

        node.onlyAllow("lifetime", "speed", "acceleration", "drag", "forces", "collision", "parametric", "size", "spin",
                "initial_roll", "billboard", "emissive", "view_range", "color", "display");
        int before = node.problemCount();

        ParticleSpec.Builder particle = ParticleSpec.builder();
        particle.lifetime(this.values.expression(node.child("lifetime"), "1"));
        speed(node.child("speed"), particle);
        acceleration(node.child("acceleration"), particle);
        particle.drag(this.values.expression(node.child("drag"), "0"));
        particle.forces(forces(node.child("forces")));
        particle.collision(collision(node.child("collision")));
        particle.parametric(parametric(node.child("parametric")));
        particle.size(this.values.expression(node.child("size"), "0.25"));
        particle.spin(this.values.expression(node.child("spin"), "0"));
        particle.initialRoll(this.values.expression(node.child("initial_roll"), "0"));
        particle.billboard(node.enumValue("billboard", Billboard.class, Billboard.CENTER));
        particle.emissive(node.bool("emissive", true));
        view(node, particle);
        color(node.child("color"), particle);
        display(node.child("display"), particle);

        return node.problemCount() > before ? null : particle.build();
    }

    private void speed(@Nullable Node node, ParticleSpec.Builder particle) {
        if (node == null) return;

        if (node.isArray()) {
            particle.axisSpeed(this.values.vector(node, Values.zeroVector()));
            return;
        }

        particle.speed(this.values.expression(node, "0"));
    }

    private void acceleration(@Nullable Node node, ParticleSpec.Builder particle) {
        if (node != null) particle.acceleration(this.values.vector(node, Values.zeroVector()));
    }

    private void view(Node node, ParticleSpec.Builder particle) {
        Node range = node.child("view_range");
        if (range == null) return;

        double viewRange = range.asNumber(Double.NaN);
        if (!(viewRange > 0) || !Double.isFinite(viewRange)) {
            range.error("view_range has to be more than 0 but is " + viewRange);
            return;
        }

        particle.viewRange(viewRange);
    }

    private List<Force> forces(@Nullable Node node) {
        List<Force> forces = new ArrayList<>();
        if (node == null) return forces;

        for (Node item : node.items()) {
            Force force = force(item);
            if (force != null) forces.add(force);
        }

        return forces;
    }

    private @Nullable Force force(Node node) {
        if (!node.isObject()) {
            node.error("expected a force object like {\"type\": \"attractor\", \"center\": [0, 1, 0]} but got " + node.describe());
            return null;
        }

        Node typeNode = node.child("type");
        String type = typeNode == null ? "" : typeNode.asString("").trim().toLowerCase(Locale.ROOT).replace('-', '_');

        try {
            return switch (type) {
                case "attractor" -> {
                    node.onlyAllow("type", "center", "strength");
                    yield new Force.Attractor(
                            this.values.constantVector(node, "center", Vec3.ZERO),
                            this.values.expression(node.child("strength"), "1"));
                }
                case "axis" -> {
                    node.onlyAllow("type", "axis", "along", "toward");
                    yield new Force.Axis(
                            this.values.constantVector(node, "axis", new Vec3(0, 1, 0)),
                            this.values.expression(node.child("along"), "0"),
                            this.values.expression(node.child("toward"), "0"));
                }
                case "set_speed" -> {
                    node.onlyAllow("type", "speed");
                    yield new Force.SetSpeed(this.values.expression(node.child("speed"), "1"));
                }
                default -> {
                    (typeNode != null ? typeNode : node).error(
                            "\"" + type + "\" isn't a force, use one of: attractor, axis, set_speed");
                    yield null;
                }
            };
        } catch (IllegalArgumentException e) {
            node.error(String.valueOf(e.getMessage()));
            return null;
        }
    }

    private @Nullable Collision collision(@Nullable Node node) {
        if (node == null) return null;

        node.onlyAllow("enabled", "restitution", "drag", "radius", "expire_on_contact");

        double restitution = this.values.nonNegative(node, "restitution", 0);
        double drag = this.values.nonNegative(node, "drag", 0);
        double radius = this.values.nonNegative(node, "radius", Collision.DEFAULT_RADIUS);
        if (restitution > 1) {
            node.child("restitution").error("restitution can't be more than 1 but is " + restitution);
            return null;
        }

        return new Collision(
                this.values.expression(node.child("enabled"), "1"), restitution, drag, radius,
                node.bool("expire_on_contact", false));
    }

    private @Nullable Parametric parametric(@Nullable Node node) {
        if (node == null) return null;

        node.onlyAllow("position", "rotation");

        Node position = node.child("position");
        Node rotation = node.child("rotation");

        return new Parametric(
                position == null ? null : this.values.vector(position, Values.zeroVector()),
                rotation == null ? null : this.values.expression(rotation, "0"));
    }

    private void color(@Nullable Node node, ParticleSpec.Builder particle) {
        if (node == null) return;

        ColorReader colors = new ColorReader(this.context);
        if (node.isString()) {
            particle.color(Gradient.of(colors.hex(node)));
            return;
        }

        if (node.isObject() && node.child("gradient") != null) {
            node.onlyAllow("gradient", "interpolant");

            Node interpolant = node.child("interpolant");
            if (interpolant != null) particle.colorInterpolant(this.values.expression(interpolant, "0"));

            Gradient gradient = colors.gradient(node.child("gradient"));
            if (gradient != null) particle.color(gradient);
            return;
        }

        Gradient gradient = colors.gradient(node);
        if (gradient != null) particle.color(gradient);
    }

    private void display(@Nullable Node node, ParticleSpec.Builder particle) {
        if (node == null) return;

        node.onlyAllow("kind", "item", "model", "tint");

        DisplayKind kind = node.enumValue("kind", DisplayKind.class, DisplayKind.TEXT);
        if (kind == DisplayKind.TEXT) return;

        Node itemNode = node.child("item");
        String item = itemNode == null ? ParticleSpec.DEFAULT_ITEM : id(itemNode);
        Node modelNode = node.child("model");
        String model = modelNode == null ? null : id(modelNode);

        if (item == null || (modelNode != null && model == null)) return;
        particle.kind(DisplayKind.ITEM).item(item).model(model).tint(node.bool("tint", true));
    }

    private static @Nullable String id(Node node) {
        String text = node.asString("").trim().toLowerCase(Locale.ROOT);
        String id = text.contains(":") ? text : "minecraft:" + text;

        if (text.isEmpty() || !ITEM_ID.matcher(id).matches()) {
            node.error("\"" + text + "\" isn't a valid id, use something like minecraft:snowball (namespace:name)");
            return null;
        }

        return id;
    }
}
