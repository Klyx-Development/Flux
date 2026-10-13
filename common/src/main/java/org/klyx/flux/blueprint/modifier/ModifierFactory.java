package org.klyx.flux.blueprint.modifier;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.support.named.Named;
import org.klyx.flux.modifier.PointModifier;
import org.klyx.flux.timeline.Frame;

import java.util.Set;
import java.util.function.Function;

public interface ModifierFactory extends Named {
    Function<Frame, PointModifier> read(Node node, Set<String> tracks);
}
