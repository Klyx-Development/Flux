package org.klyx.flux.data.particle;

import net.kyori.adventure.audience.Audience;
import org.klyx.flux.utils.Position;
import org.klyx.flux.effects.EffectConfiguration;
import org.klyx.flux.effects.EffectFormat;

import java.util.Collection;

public interface ParticleRenderer {

    void render(EffectFormat effect, Position position, Collection<Audience> viewers);

    void render(EffectFormat effect, EffectConfiguration configuration, ParticleConfiguration particleConfiguration,
                Position position, Collection<Audience> viewers);

    void render(EffectFormat effect, ParticleConfiguration particleConfiguration, Position position,
                Collection<Audience> viewers);

    double getViewDistance();
    void setViewDistance(double distance);

}
