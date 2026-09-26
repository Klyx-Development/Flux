package org.klyx.flux.timeline;

public record Cue(double time, String name) {
    public Cue {
        if (!(time >= 0) || !Double.isFinite(time)) {
            throw new IllegalArgumentException("time has to be greater than or equal to zero and finite: " + time);
        }

        if (name.isBlank()) throw new IllegalArgumentException("a cue needs a name");
    }
}
