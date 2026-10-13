package org.klyx.flux.blueprint.shader;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.support.named.Named;
import org.klyx.flux.shader.PointShader;
import org.klyx.flux.timeline.Frame;

import java.util.Set;
import java.util.function.Function;

public interface ShaderFactory extends Named {
    @Nullable Function<Frame, PointShader> read(Node node, Set<String> tracks);
}
