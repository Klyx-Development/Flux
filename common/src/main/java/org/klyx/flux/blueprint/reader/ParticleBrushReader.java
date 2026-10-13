package org.klyx.flux.blueprint.reader;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.effect.ParticleBrush;

import java.util.Locale;

public final class ParticleBrushReader {
    private static final String PREFIX = "minecraft:";

    // either a plain name or an object when the particle needs data
    public static @Nullable ParticleBrush read(Node node) {
        if (node.isString()) return named(node, node.asString(""));

        if (!node.isObject()) {
            node.error("expected a particle name like \"end_rod\" or an object like {\"type\": \"dust\", \"size\": 1} but got "
                    + node.describe());
            return null;
        }

        node.onlyAllow("type", "size", "to", "block", "item", "speed");

        Node typeNode = node.child("type");
        if (typeNode == null) {
            node.error("missing \"type\", use a particle like \"flame\" or \"dust\"");
            return null;
        }

        String id = id(typeNode);
        if (id == null) return null;

        int before = node.problemCount();
        double size = node.number("size", ParticleBrush.DEFAULT_SIZE);
        double speed = node.number("speed", 0);
        Integer to = color(node.child("to"));
        String block = optionalId(node.child("block"));
        String item = optionalId(node.child("item"));
        if (node.problemCount() > before) return null;

        try {
            return new ParticleBrush(id, size, to, block, item, speed);
        } catch (IllegalArgumentException e) {
            node.error(String.valueOf(e.getMessage()));
            return null;
        }
    }

    private static @Nullable ParticleBrush named(Node node, String name) {
        String id = id(node);
        if (id == null) return null;

        return new ParticleBrush(id, ParticleBrush.DEFAULT_SIZE, null, null, null, 0);
    }

    private static @Nullable String id(Node node) {
        String text = node.asString("").trim().toLowerCase(Locale.ROOT);
        String id = text.contains(":") ? text : PREFIX + text;

        if (text.isEmpty() || !id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            node.error("\"" + text + "\" isn't a particle, use an id like \"flame\" or \"minecraft:end_rod\"");
            return null;
        }

        return id;
    }

    private static @Nullable String optionalId(@Nullable Node node) {
        if (node == null) return null;

        String text = node.asString("").trim().toLowerCase(Locale.ROOT);
        String id = text.contains(":") ? text : PREFIX + text;

        if (text.isEmpty() || !id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            node.error("\"" + text + "\" isn't an id, use an id in the format of namespace:name, e.g. minecraft:stone");
            return null;
        }

        return id;
    }

    private static @Nullable Integer color(@Nullable Node node) {
        return node == null ? null : StyleValues.parseColor(node);
    }
}
