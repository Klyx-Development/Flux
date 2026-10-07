package org.klyx.flux.effect.playback;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.effect.Effect;
import org.klyx.flux.effect.Layer;
import org.klyx.flux.effect.anchor.Anchor;
import org.klyx.flux.timeline.Cue;
import org.klyx.flux.timeline.Frame;
import org.klyx.flux.timeline.Timeline;

import java.util.BitSet;
import java.util.List;

public final class EffectInstance implements Playback {
    private static final double DUE_TOLERANCE = 1.0e-9;

    private final Effect effect;
    private final Anchor anchor;
    private final EffectSink sink;
    private final double[] nextDue;

    private double elapsed;
    private double speed = 1;
    private boolean paused;
    private boolean stopped;
    private boolean includeStart = true;

    public EffectInstance(Effect effect, Anchor anchor, EffectSink sink) {
        this.effect = effect;
        this.anchor = anchor;
        this.sink = sink;
        this.nextDue = new double[effect.layers().size()];
    }

    public Effect effect() {
        return this.effect;
    }

    public Anchor anchor() {
        return this.anchor;
    }

    public EffectSink sink() {
        return this.sink;
    }

    @Override
    public synchronized void pause() {
        this.paused = true;
    }

    @Override
    public synchronized void resume() {
        this.paused = false;
    }

    @Override
    public synchronized void stop() {
        this.stopped = true;
    }

    @Override
    public synchronized boolean isPaused() {
        return this.paused;
    }

    @Override
    public synchronized boolean isStopped() {
        return this.stopped;
    }

    public synchronized double elapsed() {
        return this.elapsed;
    }

    public synchronized void setSpeed(double speed) {
        if (!(speed > 0) || !Double.isFinite(speed)) {
            throw new IllegalArgumentException("speed must be positive and finite: " + speed);
        }

        this.speed = speed;
    }

    public synchronized @Nullable Step advance(double deltaSeconds) {
        if (this.stopped || this.paused) return null;

        Timeline timeline = this.effect.timeline();
        double from = this.elapsed;
        this.elapsed += deltaSeconds * this.speed;

        List<Cue> cues = timeline.cuesBetween(from, this.elapsed, this.includeStart);
        this.includeStart = false;

        Frame frame = timeline.frameAt(timeline.localTime(this.elapsed));
        boolean finished = timeline.isFinished(this.elapsed);
        if (finished) this.stopped = true;

        return new Step(frame, cues, dueLayers(), finished);
    }

    private BitSet dueLayers() {
        List<Layer> layers = this.effect.layers();
        BitSet due = new BitSet(layers.size());

        for (int i = 0; i < layers.size(); i++) {
            double interval = layers.get(i).interval();
            if (interval == 0) {
                due.set(i);
                continue;
            }

            if (this.elapsed + DUE_TOLERANCE < this.nextDue[i]) continue;

            due.set(i);
            double next = this.nextDue[i] + interval;
            this.nextDue[i] = next > this.elapsed ? next : this.elapsed + interval;
        }

        return due;
    }

    public record Step(Frame frame, List<Cue> cues, BitSet due, boolean finished) {
    }
}
