package org.klyx.flux.timeline;

import org.klyx.flux.math.Easing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record Track(List<Keyframe> keys) {
    public Track {
        if (keys.isEmpty()) throw new IllegalArgumentException("track needs at least one keyframe");
        keys = keys.stream().sorted(Comparator.comparingDouble(Keyframe::time)).toList();
    }

    public static Track constant(double value) {
        return new Track(List.of(new Keyframe(0, value)));
    }

    public static Track between(double duration, double from, double to, Easing easing) {
        return new Track(List.of(new Keyframe(0, from, easing), new Keyframe(duration, to)));
    }

    public static Builder builder() {
        return new Builder();
    }

    public double valueAt(double time) {
        Keyframe first = this.keys.getFirst();
        if (time < first.time()) return first.value();

        Keyframe last = this.keys.getLast();
        if (time >= last.time()) return last.value();

        for (int i = 1; i < this.keys.size(); i++) {
            Keyframe to = this.keys.get(i);
            if (time >= to.time()) continue;

            Keyframe from = this.keys.get(i - 1);
            double eased = from.easing().apply((time - from.time()) / (to.time() - from.time()));

            return from.value() + (to.value() - from.value()) * eased;
        }

        return last.value();
    }

    public static final class Builder {
        private final List<Keyframe> keys = new ArrayList<>();

        public Builder key(double time, double value) {
            this.keys.add(new Keyframe(time, value));
            return this;
        }

        public Builder key(double time, double value, Easing easing) {
            this.keys.add(new Keyframe(time, value, easing));
            return this;
        }

        public Track build() {
            return new Track(this.keys);
        }
    }
}
