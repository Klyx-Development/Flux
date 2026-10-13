package org.klyx.flux.blueprint.modifier;

import org.klyx.flux.blueprint.Dynamic;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.reader.StyleValues;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.modifier.PointModifier;
import org.klyx.flux.modifier.ScaleModifier;
import org.klyx.flux.timeline.Frame;

import java.util.Set;
import java.util.function.Function;

public final class ScaleFactory implements ModifierFactory {
    @Override
    public String name() {
        return "scale";
    }

    @Override
    public String[] keys() {
        return new String[] {"type", "factors"};
    }

    @Override
    public Function<Frame, PointModifier> read(Node node, Set<String> tracks) {
        Dynamic x = StyleValues.vectorPart(node, "factors", 0, tracks, 1);
        Dynamic y = StyleValues.vectorPart(node, "factors", 1, tracks, 1);
        Dynamic z = StyleValues.vectorPart(node, "factors", 2, tracks, 1);
        return frame -> new ScaleModifier(new Vec3(x.at(frame), y.at(frame), z.at(frame)));
    }
}
