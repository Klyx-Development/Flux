package org.klyx.flux.data.particle.animation;

import net.kyori.adventure.audience.Audience;
import org.klyx.flux.utils.Position;
import org.klyx.flux.effects.EffectFormat;

import java.util.Collection;
import java.util.UUID;

public interface ParticleAnimator {

    ParticleAnimation createAnimation(EffectFormat format, AnimationSettings settings);

    ParticleAnimation startAnimation(EffectFormat format, Position position,
                                     Collection<Audience> viewers, AnimationSettings settings);

    void stopAnimations(UUID effectId);

    Collection<ParticleAnimation> getActiveAnimations();

}
