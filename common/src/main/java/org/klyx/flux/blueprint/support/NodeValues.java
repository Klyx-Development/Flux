package org.klyx.flux.blueprint.support;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.math.Vec3;

import java.util.List;
import java.util.Locale;

public final class NodeValues {
    public static String string(Node node, String fallback) {
        if (!node.isString()) {
            node.error("expected text but got " + node.describe());
            return fallback;
        }

        return node.json().getAsString();
    }

    public static boolean bool(Node node, boolean fallback) {
        if (!node.json().isJsonPrimitive() || !node.json().getAsJsonPrimitive().isBoolean()) {
            node.error("expected a boolean (true/false) but got " + node.describe());
            return fallback;
        }

        return node.json().getAsBoolean();
    }

    public static double number(Node node, double fallback) {
        if (!node.isNumber()) {
            node.error("expected a number but got " + node.describe());
            return fallback;
        }

        double value = node.json().getAsDouble();
        if (!Double.isFinite(value)) {
            node.error("expected a finite number");
            return fallback;
        }

        return value;
    }

    public static <E extends Enum<E>> E enumValue(Node node, Class<E> type, E fallback) {
        if (!node.isString()) {
            node.error("expected one of: " + Node.names(type) + " but got " + node.describe());
            return fallback;
        }

        String wanted = node.json().getAsString().trim().toUpperCase(Locale.ROOT)
                .replace('-', '_').replace(' ', '_');
        for (E constant : type.getEnumConstants()) {
            if (constant.name().equals(wanted)) return constant;
        }

        node.error("\"" + node.json().getAsString() + "\" isn't valid, use one of: " + Node.names(type));
        return fallback;
    }

    public static Vec3 vec3(Node node, Vec3 fallback, boolean allowScalar) {
        if (node.isNumber() && allowScalar) {
            double value = number(node, Double.NaN);
            return Double.isNaN(value) ? fallback : new Vec3(value, value, value);
        }

        if (!node.isArray()) {
            node.error("expected " + (allowScalar ? "a number or " : "") + "a list of three numbers [x, y, z] but got " + node.describe());
            return fallback;
        }

        List<Node> parts = node.items();
        if (parts.size() != 3) {
            node.error("expected exactly three numbers [x, y, z] but got " + parts.size());
            return fallback;
        }

        double x = number(parts.get(0), Double.NaN);
        double y = number(parts.get(1), Double.NaN);
        double z = number(parts.get(2), Double.NaN);

        return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z) ? fallback : new Vec3(x, y, z);
    }
}
