package org.klyx.flux.importer.snowstorm.particle.part;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.expr.Expression;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.math.Easing;
import org.klyx.flux.shader.Gradient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ColorReader {
    private final ReadingContext context;

    public ColorReader(ReadingContext context) {
        this.context = context;
    }

    public @Nullable Gradient gradient(Node node) {
        List<Gradient.ColorStop> stops = new ArrayList<>();
        if (node.isArray()) {
            arrayStops(node, stops);
        } else if (node.isObject()) {
            objectStops(node, stops);
        } else {
            node.error("expected a list of colours or an object but got " + node.describe());
            return null;
        }

        if (stops.isEmpty()) {
            if (!this.context.hasProblems() || !node.isArray()) node.error("a gradient needs at least one colour");
            return null;
        }

        return new Gradient(stops, Easing.LINEAR);
    }

    private void arrayStops(Node node, List<Gradient.ColorStop> stops) {
        List<Node> items = node.items();
        for (int i = 0; i < items.size(); i++) {
            double at = items.size() == 1 ? 0 : (double) i / (items.size() - 1);

            Integer rgb = colour(items.get(i));
            if (rgb != null) stops.add(new Gradient.ColorStop(at, rgb));
        }
    }

    private void objectStops(Node node, List<Gradient.ColorStop> stops) {
        for (Map.Entry<String, Node> entry : node.entries().entrySet()) {
            double at;
            try {
                at = Double.parseDouble(entry.getKey());
            } catch (NumberFormatException e) {
                entry.getValue().error("\"" + entry.getKey() + "\" isn't a number");
                continue;
            }

            if (!(at >= 0 && at <= 1)) {
                entry.getValue().error("position " + at + " is outside 0 to 1");
                continue;
            }

            Integer rgb = colour(entry.getValue());
            if (rgb != null) stops.add(new Gradient.ColorStop(at, rgb));
        }
    }

    private @Nullable Integer colour(Node node) {
        if (node.isString()) return hex(node);

        if (node.isArray()) {
            List<Expression> parts = channels(node);
            if (parts == null) return null;

            int[] rgb = new int[3];
            for (int i = 0; i < 3; i++) {
                if (!parts.get(i).isConstant()) {
                    node.error("a gradient's colours has to be plain numbers or hex codes");
                    return null;
                }
                rgb[i] = (int) Math.round(Math.clamp(parts.get(i).evalConstant(), 0, 1) * 255);
            }

            return rgb[0] << 16 | rgb[1] << 8 | rgb[2];
        }

        node.error("expected a colour like \"#RRGGBB\" or [r, g, b] but got " + node.describe());
        return null;
    }

    public @Nullable List<Expression> channels(Node node) {
        List<Node> items = node.items();
        if (items.size() != 3 && items.size() != 4) {
            node.error("expected [r, g, b] or [r, g, b, a] but got " + items.size() + " values");
            return null;
        }

        if (items.size() == 4) {
            Expression alpha = this.context.values().expression(items.get(3), "1");
            if (!alpha.isConstant() || alpha.evalConstant() != 1) {
                this.context.ignore(items.get(3), "transparency isn't used");
            }
        }

        return List.of(
                this.context.values().expression(items.get(0), "1"),
                this.context.values().expression(items.get(1), "1"),
                this.context.values().expression(items.get(2), "1"));
    }

    // #AARRGGBB, snowstorm puts the alpha in the hex code because bedrock supports transparency
    public int hex(Node node) {
        String text = node.asString("#FFFFFF").trim();
        String digits = text.startsWith("#") ? text.substring(1) : text;
        if ((digits.length() != 6 && digits.length() != 8) || !digits.chars().allMatch(c -> Character.digit(c, 16) >= 0)) {
            node.error("\"" + text + "\" isn't a colour, use #RRGGBB or #AARRGGBB");
            return 0xFFFFFF;
        }

        return Integer.parseInt(digits.substring(digits.length() - 6), 16);
    }
}
