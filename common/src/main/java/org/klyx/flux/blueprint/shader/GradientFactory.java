package org.klyx.flux.blueprint.shader;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Dynamic;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.reader.StyleValues;
import org.klyx.flux.math.Easing;
import org.klyx.flux.shader.Gradient;
import org.klyx.flux.shader.GradientShader;
import org.klyx.flux.shader.PointShader;
import org.klyx.flux.timeline.Frame;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class GradientFactory implements ShaderFactory {
    @Override
    public String name() {
        return "gradient";
    }

    @Override
    public String[] keys() {
        return new String[] {"type", "colors", "mode", "phase", "easing"};
    }

    @Override
    public @Nullable Function<Frame, PointShader> read(Node node, Set<String> tracks) {
        int before = node.problemCount();
        Gradient gradient = gradient(node);
        GradientShader.Mode mode = node.enumValue("mode", GradientShader.Mode.class, GradientShader.Mode.Y);
        Dynamic phase = StyleValues.optional(node, "phase", tracks, 0);
        if (gradient == null || node.problemCount() > before) return null;

        return frame -> new GradientShader(gradient, mode, phase.at(frame));
    }

    private static @Nullable Gradient gradient(Node node) {
        Node colors = node.child("colors");
        if (colors == null) {
            node.error("missing \"colors\", use a list like [\"#FF0000\", \"#0000FF\"]");
            return null;
        }

        List<Node> items = colors.items();
        if (items.isEmpty()) {
            if (colors.isArray()) colors.error("a gradient needs at least one colour");
            return null;
        }

        int[] rgb = new int[items.size()];
        boolean valid = true;
        for (int i = 0; i < rgb.length; i++) {
            Integer parsed = StyleValues.parseColor(items.get(i));
            if (parsed == null) {
                valid = false;
            } else {
                rgb[i] = parsed;
            }
        }

        if (!valid) return null;

        Easing easing = node.enumValue("easing", Easing.class, Easing.LINEAR);
        return new Gradient(Gradient.of(rgb).stops(), easing);
    }
}
