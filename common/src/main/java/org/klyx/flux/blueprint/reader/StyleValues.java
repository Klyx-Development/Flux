package org.klyx.flux.blueprint.reader;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Dynamic;
import org.klyx.flux.blueprint.Node;

import java.util.List;
import java.util.Set;

public final class StyleValues {
    public static int color(Node parent, String key) {
        Node node = parent.child(key);
        if (node == null) {
            parent.error("missing \"" + key + "\", use a colour/hex code like \"#FFFFFF\"");
            return 0;
        }

        Integer parsed = parseColor(node);
        return parsed == null ? 0 : parsed;
    }

    public static @Nullable Integer parseColor(Node node) {
        if (!node.isString()) {
            node.error("expected a colour like \"#FFFFFF\" but got " + node.describe());
            return null;
        }

        String text = node.json().getAsString().trim();
        String hex = text.startsWith("#") ? text.substring(1) : text;
        if (hex.length() != 6 || !hex.chars().allMatch(character -> Character.digit(character, 16) >= 0)) {
            node.error("\"" + text + "\" isn't a colour, write a valid hex code like \"#FFFFFF\"");
            return null;
        }

        return Integer.parseInt(hex, 16);
    }

    public static Dynamic required(Node parent, String key, Set<String> tracks) {
        Node node = parent.child(key);
        if (node == null) {
            parent.error("missing \"" + key + "\"");
            return Dynamic.constant(0);
        }

        return Dynamic.read(node, tracks, 0);
    }

    public static Dynamic optional(Node parent, String key, Set<String> tracks, double fallback) {
        Node node = parent.child(key);
        return node == null ? Dynamic.constant(fallback) : Dynamic.read(node, tracks, fallback);
    }

    public static Dynamic vectorPart(Node parent, String key, int index, Set<String> tracks, double fallback) {
        Node node = parent.child(key);
        if (node == null) {
            parent.error("missing \"" + key + "\"");
            return Dynamic.constant(fallback);
        }

        if (!node.isArray()) {
            if (index == 0) node.error("expected a list of three values [x, y, z] but got " + node.describe() + " instead");
            return Dynamic.constant(fallback);
        }

        List<Node> parts = node.items();
        if (parts.size() != 3) {
            if (index == 0) node.error("expected exactly three values [x, y, z] but got " + parts.size() + " instead");
            return Dynamic.constant(fallback);
        }

        return Dynamic.read(parts.get(index), tracks, fallback);
    }
}
