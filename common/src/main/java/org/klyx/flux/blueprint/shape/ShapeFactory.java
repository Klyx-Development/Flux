package org.klyx.flux.blueprint.shape;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.support.named.Named;
import org.klyx.flux.shape.Shape;

public interface ShapeFactory extends Named {
    @Nullable Shape read(Node node, NestedShapes nested);
}
