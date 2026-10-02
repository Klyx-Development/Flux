package org.klyx.flux.sim.force;

import java.util.List;

public final class ForceEffects {
    public static List<ForceEffect> allOf(List<Force> forces) {
        return forces.stream().map(ForceEffects::of).toList();
    }

    private static ForceEffect of(Force force) {
        return switch (force) {
            case Force.Attractor attractor -> new AttractorEffect(attractor);
            case Force.Axis line -> new AxisEffect(line);
            case Force.SetSpeed hold -> new SpeedHoldEffect(hold);
        };
    }
}
