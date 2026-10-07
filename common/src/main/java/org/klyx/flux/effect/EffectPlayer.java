package org.klyx.flux.effect;

import org.klyx.flux.effect.anchor.Anchor;
import org.klyx.flux.effect.playback.EffectInstance;
import org.klyx.flux.effect.playback.EffectSink;
import org.klyx.flux.effect.playback.SampleCache;
import org.klyx.flux.timeline.Cue;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EffectPlayer {
    private final List<EffectInstance> instances = new CopyOnWriteArrayList<>();
    private final SampleCache cache = new SampleCache(256);
    private final Logger logger;

    public EffectPlayer(Logger logger) {
        this.logger = logger;
    }

    public EffectInstance play(Effect effect, Anchor anchor, EffectSink sink) {
        EffectInstance instance = new EffectInstance(effect, anchor, sink);
        this.instances.add(instance);
        return instance;
    }

    public int activeCount() {
        return this.instances.size();
    }

    public void stopAll() {
        for (EffectInstance instance : this.instances) {
            instance.stop();
        }

        this.instances.clear();
    }

    public void tick(double deltaSeconds) {
        for (EffectInstance instance : this.instances) {
            try {
                if (!instance.anchor().isValid()) {
                    instance.stop();
                } else {
                    step(instance, deltaSeconds);
                }
            } catch (RuntimeException e) {
                this.logger.error("an effect failed and was stopped", e);
                instance.stop();
            }

            if (instance.isStopped()) this.instances.remove(instance);
        }
    }

    private void step(EffectInstance instance, double deltaSeconds) {
        EffectInstance.Step step = instance.advance(deltaSeconds);
        if (step == null) return;

        if (!step.due().isEmpty()) {
            instance.sink().render(
                    instance.effect().composeLayers(step.frame(), this.cache, step.due(), instance.anchor().bounds()),
                    instance.anchor().position()
            );
        }

        for (Cue cue : step.cues()) {
            Effect sub = instance.effect().cueEffects().get(cue.name());
            if (sub != null) play(sub, instance.anchor(), instance.sink());
        }
    }
}
