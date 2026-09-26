package org.klyx.flux.timeline;

import org.klyx.flux.math.Easing;

public record Keyframe(double time, double value, Easing easing) {
    public Keyframe {
        if (!(time >= 0) || !Double.isFinite(time)) {
            throw new IllegalArgumentException("time has to be greater than or equal to zero and finite: " + time);
        }

        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("value must be finite: " + value);
        }
    }

    public Keyframe(double time, double value) {
        this(time, value, Easing.LINEAR);
    }
}
