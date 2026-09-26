package org.klyx.flux.timeline;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CueScanner {
    private final List<Cue> cues;
    private final LoopMode loop;
    private final double duration;

    public CueScanner(List<Cue> cues, LoopMode loop, double duration) {
        this.cues = cues;
        this.loop = loop;
        this.duration = duration;
    }

    public List<Cue> between(double from, double to, boolean includeFrom) {
        if (this.cues.isEmpty() || to < from) return List.of();

        double period = this.loop == LoopMode.PING_PONG ? this.duration * 2 : this.duration;
        long firstCycle = 0;
        long lastCycle = 0;

        if (this.loop != LoopMode.ONCE) {
            firstCycle = Math.max(0, (long) Math.floor(Math.max(0, from) / period) - 1);
            lastCycle = (long) Math.floor(Math.max(0, to) / period);
        }

        List<Passed> found = new ArrayList<>();

        for (long cycle = firstCycle; cycle <= lastCycle; cycle++) {
            double base = cycle * period;

            for (Cue cue : this.cues) {
                collect(found, cue, base + cue.time(), from, to, includeFrom);

                if (this.loop == LoopMode.PING_PONG && cue.time() > 0 && cue.time() < this.duration) {
                    collect(found, cue, base + period - cue.time(), from, to, includeFrom);
                }
            }
        }

        found.sort(Comparator.comparingDouble(Passed::at));
        return found.stream().map(Passed::cue).toList();
    }

    private static void collect(List<Passed> found, Cue cue, double at, double from, double to, boolean includeFrom) {
        boolean afterFrom = includeFrom ? at >= from : at > from;

        if (afterFrom && at <= to) found.add(new Passed(cue, at));
    }

    private record Passed(Cue cue, double at) { }
}
