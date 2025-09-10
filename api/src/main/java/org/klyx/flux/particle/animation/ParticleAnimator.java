package org.klyx.flux.particle.animation;

import org.klyx.flux.utils.player.FluxPlayer;
import org.klyx.flux.utils.Position;
import org.klyx.flux.effects.EffectFormat;

import java.util.Collection;
import java.util.UUID;

public interface ParticleAnimator {

    ParticleAnimation createAnimation(EffectFormat format, AnimationSettings settings);

    ParticleAnimation startAnimation(EffectFormat format, Position position,
                                     Collection<FluxPlayer> viewers, AnimationSettings settings);

    void stopAnimations(UUID effectId);

    Collection<ParticleAnimation> getActiveAnimations();

}
