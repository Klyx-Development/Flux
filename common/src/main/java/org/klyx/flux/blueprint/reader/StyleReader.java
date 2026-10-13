package org.klyx.flux.blueprint.reader;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.modifier.ModifierFactory;
import org.klyx.flux.blueprint.modifier.ModifierRegistry;
import org.klyx.flux.blueprint.shader.ShaderFactory;
import org.klyx.flux.blueprint.shader.ShaderRegistry;
import org.klyx.flux.modifier.PointModifier;
import org.klyx.flux.shader.PointShader;
import org.klyx.flux.timeline.Frame;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class StyleReader {
    public static @Nullable Function<Frame, PointModifier> modifiers(Node list, Set<String> tracks) {
        List<Function<Frame, PointModifier>> steps = new ArrayList<>();
        for (Node item : list.items()) {
            Function<Frame, PointModifier> step = modifier(item, tracks);
            if (step != null) steps.add(step);
        }

        if (steps.isEmpty()) return null;

        return frame -> {
            PointModifier chain = steps.getFirst().apply(frame);
            for (int i = 1; i < steps.size(); i++) {
                chain = chain.then(steps.get(i).apply(frame));
            }

            return chain;
        };
    }

    private static @Nullable Function<Frame, PointModifier> modifier(Node node, Set<String> tracks) {
        if (!node.isObject()) {
            node.error("expected an object like {\"type\": \"twist\", \"radians_per_block\": 0.5} but got " + node.describe() + " instead");
            return null;
        }

        ModifierFactory factory = ModifierRegistry.find(node);
        if (factory == null) return null;

        node.onlyAllow(factory.keys());
        return factory.read(node, tracks);
    }

    public static @Nullable Function<Frame, PointShader> shader(Node node, Set<String> tracks) {
        if (!node.isObject()) {
            node.error("expected an object like {\"type\": \"solid\", \"color\": \"#FF8800\"} but got " + node.describe() + " instead");
            return null;
        }

        ShaderFactory factory = ShaderRegistry.find(node);
        if (factory == null) return null;

        node.onlyAllow(factory.keys());
        return factory.read(node, tracks);
    }
}
