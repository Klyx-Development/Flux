package org.klyx.flux.blueprint.shader;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.support.named.NamedRegistry;

import java.util.List;

public final class ShaderRegistry {
    private static final NamedRegistry<ShaderFactory> FACTORIES = new NamedRegistry<>(List.of(
            new SolidFactory(),
            new GradientFactory()
    ));

    public static @Nullable ShaderFactory find(Node node) {
        return FACTORIES.find(node);
    }
}
