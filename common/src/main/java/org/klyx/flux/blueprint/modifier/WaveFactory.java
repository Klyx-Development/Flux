package org.klyx.flux.blueprint.modifier;

import org.klyx.flux.blueprint.Dynamic;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.reader.StyleValues;
import org.klyx.flux.math.Axis;
import org.klyx.flux.modifier.PointModifier;
import org.klyx.flux.modifier.WaveModifier;
import org.klyx.flux.timeline.Frame;

import java.util.Set;
import java.util.function.Function;

public final class WaveFactory implements ModifierFactory {
    @Override
    public String name() {
        return "wave";
    }

    @Override
    public String[] keys() {
        return new String[] {"type", "displace", "along", "amplitude", "wavelength", "phase"};
    }

    @Override
    public Function<Frame, PointModifier> read(Node node, Set<String> tracks) {
        Axis displace = node.requireEnum("displace", Axis.class);
        Axis along = node.requireEnum("along", Axis.class);
        if (displace == along) node.error("\"displace\" and \"along\" must be different axes");

        Dynamic amplitude = StyleValues.required(node, "amplitude", tracks);
        Dynamic wavelength = StyleValues.required(node, "wavelength", tracks);
        Dynamic phase = StyleValues.optional(node, "phase", tracks, 0);
        if (wavelength.isConstant() && !(wavelength.constantValue() > 0)) {
            Node wavelengthNode = node.child("wavelength");
            if (wavelengthNode != null) wavelengthNode.error("wavelength must be greater than 0");
        }

        return frame -> {
            // an expression can go down to 0 or below mid-effect
            // so just make sure it's not 0 as it will be rejected if it is 0
            double length = Math.max(wavelength.at(frame), 1.0e-6);
            return new WaveModifier(displace, along, amplitude.at(frame), length, phase.at(frame));
        };
    }
}
