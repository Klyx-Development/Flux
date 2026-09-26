package org.klyx.flux.timeline;

import org.klyx.flux.expr.Expression;
import org.klyx.flux.expr.Variables;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record Timeline(
        double duration, LoopMode loop, Map<String, Track> tracks,
        Map<String, Expression> expressions, List<Cue> cues
) {
    public Timeline {
        if (!(duration > 0) || !Double.isFinite(duration)) {
            throw new IllegalArgumentException("duration has to be positive and finite: " + duration);
        }

        tracks = Map.copyOf(tracks);
        expressions = Map.copyOf(expressions);

        for (String name : expressions.keySet()) {
            if (tracks.containsKey(name)) {
                throw new IllegalArgumentException("\"" + name + "\" can't be both a keyframe and an expression");
            }
        }

        cues = cues.stream().sorted(Comparator.comparingDouble(Cue::time)).toList();

        for (Cue cue : cues) {
            if (cue.time() > duration) {
                throw new IllegalArgumentException(
                        "cue \"" + cue.name() + "\" at " + cue.time() + " is past the duration " + duration
                );
            }
        }
    }

    public static Builder builder(double duration) {
        return new Builder(duration);
    }

    public double localTime(double elapsed) {
        if (elapsed <= 0) return 0;

        return switch (this.loop) {
            case ONCE -> Math.min(elapsed, this.duration);
            case LOOP -> elapsed % this.duration;
            case PING_PONG -> {
                double wrapped = elapsed % (this.duration * 2);
                yield wrapped <= this.duration ? wrapped : this.duration * 2 - wrapped;
            }
        };
    }

    public boolean isFinished(double elapsed) {
        return this.loop == LoopMode.ONCE && elapsed >= this.duration;
    }

    public Frame frameAt(double localTime) {
        double progress = localTime / this.duration;
        Map<String, Double> values = new HashMap<>();

        this.tracks.forEach((name, track) -> values.put(name, track.valueAt(localTime)));

        if (!this.expressions.isEmpty()) {
            Variables variables = new Frame(localTime, progress, this.duration, values).variables();

            this.expressions.forEach((name, expression) -> {
                double value = expression.eval(variables);
                // nan or infinity would corrupt every point it touches!
                values.put(name, Double.isFinite(value) ? value : 0);
            });
        }

        return new Frame(localTime, progress, this.duration, values);
    }

    public List<Cue> cuesBetween(double from, double to, boolean includeFrom) {
        return new CueScanner(this.cues, this.loop, this.duration).between(from, to, includeFrom);
    }

    public static final class Builder {
        private final double duration;
        private final Map<String, Track> tracks = new LinkedHashMap<>();
        private final Map<String, Expression> expressions = new LinkedHashMap<>();
        private final List<Cue> cues = new ArrayList<>();
        private LoopMode loop = LoopMode.ONCE;

        private Builder(double duration) {
            this.duration = duration;
        }

        public Builder loop(LoopMode loop) {
            this.loop = loop;
            return this;
        }

        public Builder track(String name, Track track) {
            this.tracks.put(name, track);
            return this;
        }

        public Builder expression(String name, Expression expression) {
            this.expressions.put(name, expression);
            return this;
        }

        public Builder cue(double time, String name) {
            this.cues.add(new Cue(time, name));
            return this;
        }

        public Timeline build() {
            return new Timeline(this.duration, this.loop, this.tracks, this.expressions, this.cues);
        }
    }
}
