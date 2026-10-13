package org.klyx.flux.blueprint.modifier;

import org.klyx.flux.blueprint.Dynamic;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.reader.StyleValues;
import org.klyx.flux.modifier.PointModifier;
import org.klyx.flux.modifier.TwistModifier;
import org.klyx.flux.timeline.Frame;

import java.util.Set;
import java.util.function.Function;

public final class TwistFactory implements ModifierFactory {
    @Override
    public String name() {
        return "twist";
    }

    @Override
    public String[] keys() {
        return new String[] {"type", "radians_per_block"};
    }

    @Override
    public Function<Frame, PointModifier> read(Node node, Set<String> tracks) {
        Dynamic amount = StyleValues.required(node, "radians_per_block", tracks);
        return frame -> new TwistModifier(amount.at(frame));
    }
}
