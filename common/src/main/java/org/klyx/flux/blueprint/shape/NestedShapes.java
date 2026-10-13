package org.klyx.flux.blueprint.shape;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.shape.Shape;

public interface NestedShapes {
    int MAX_NESTING = 6;

    int depth();

    @Nullable Shape read(Node node);
}
