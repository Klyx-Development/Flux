package org.klyx.flux.data.particle.animation;

import net.kyori.adventure.audience.Audience;
import org.klyx.flux.utils.Position;
import org.klyx.flux.data.particle.ParticleConfiguration;
import org.klyx.flux.data.particle.ParticleRenderer;
import org.klyx.flux.effects.EffectConfiguration;
import org.klyx.flux.effects.EffectFormat;

import java.util.Collection;

public class ParticleShapeRenderer {

    private final ParticleRenderer renderer;
    private final ParticleAnimator animator;

    public ParticleShapeRenderer(ParticleRenderer renderer, ParticleAnimator animator) {
        this.renderer = renderer;
        this.animator = animator;
    }

    public void render(EffectFormat format, Position position, Collection<Audience> viewers) {
        renderer.render(format, position, viewers);
    }

    public ParticleAnimation animate(EffectFormat format, Position position, Collection<Audience> viewers,
                                     long durationMs) {
        AnimationSettings settings = new AnimationSettings().setDurationMs(durationMs);
        return animator.startAnimation(format, position, viewers, settings);
    }

    public static class RenderBuilder {
        private final EffectFormat format;
        private final ParticleRenderer renderer;
        private final ParticleAnimator animator;

        private Position position;
        private Collection<Audience> viewers;
        private EffectConfiguration configuration;
        private ParticleConfiguration particleConfiguration;
        private AnimationSettings animationSettings;

        public RenderBuilder(EffectFormat format, ParticleRenderer renderer, ParticleAnimator animator) {
            this.format = format;
            this.renderer = renderer;
            this.animator = animator;
        }

        public RenderBuilder at(Position position) {
            this.position = position;
            return this;
        }

        public RenderBuilder viewers(Collection<Audience> viewers) {
            this.viewers = viewers;
            return this;
        }

        public RenderBuilder config(EffectConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public RenderBuilder particles(ParticleConfiguration particleConfiguration) {
            this.particleConfiguration = particleConfiguration;
            return this;
        }

        public RenderBuilder animated(AnimationSettings settings) {
            this.animationSettings = settings;
            return this;
        }

        public void spawn() {
            if (configuration != null && particleConfiguration != null) {
                renderer.render(format, configuration, particleConfiguration, position, viewers);
            } else {
                renderer.render(format, position, viewers);
            }
        }

        public ParticleAnimation start() {
            if (animationSettings == null) {
                animationSettings = new AnimationSettings();
            }
            return animator.startAnimation(format, position, viewers, animationSettings);
        }
    }

}
