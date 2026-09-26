package org.klyx.flux.timeline;

import org.klyx.flux.expr.Variables;

import java.util.Map;

public record Frame(double time, double progress, double duration, Map<String, Double> values) {
    public Frame {
        values = Map.copyOf(values);
    }

    public double value(String track, double fallback) {
        Double value = this.values.get(track);
        return value == null ? fallback : value;
    }

    public Variables variables() {
        return name -> switch (name) {
            case "query.time" -> this.time;
            case "query.progress" -> this.progress;
            case "query.duration" -> this.duration;
            default -> name.startsWith("variable.") ? value(name.substring("variable.".length()), 0) : 0;
        };
    }
}
