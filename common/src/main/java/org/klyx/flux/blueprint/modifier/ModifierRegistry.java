package org.klyx.flux.blueprint.modifier;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.support.named.NamedRegistry;

import java.util.List;

public final class ModifierRegistry {
    private static final NamedRegistry<ModifierFactory> FACTORIES = new NamedRegistry<>(List.of(
            new ScaleFactory(),
            new TwistFactory(),
            new WaveFactory()
    ));

    public static @Nullable ModifierFactory find(Node node) {
        return FACTORIES.find(node);
    }
}
