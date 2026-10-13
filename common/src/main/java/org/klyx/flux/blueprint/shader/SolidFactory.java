package org.klyx.flux.blueprint.shader;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.reader.StyleValues;
import org.klyx.flux.shader.PointShader;
import org.klyx.flux.shader.SolidShader;
import org.klyx.flux.timeline.Frame;

import java.util.Set;
import java.util.function.Function;

public final class SolidFactory implements ShaderFactory {
    @Override
    public String name() {
        return "solid";
    }

    @Override
    public String[] keys() {
        return new String[] {"type", "color"};
    }

    @Override
    public @Nullable Function<Frame, PointShader> read(Node node, Set<String> tracks) {
        int before = node.problemCount();
        int color = StyleValues.color(node, "color");
        if (node.problemCount() > before) return null;

        PointShader shader = new SolidShader(color);
        return _ -> shader;
    }
}
