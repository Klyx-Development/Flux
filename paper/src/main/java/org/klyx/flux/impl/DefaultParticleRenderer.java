package org.klyx.flux.impl;

import net.kyori.adventure.audience.Audience;
import org.klyx.flux.data.particle.ParticleConfiguration;
import org.klyx.flux.data.particle.ParticleRenderer;
import org.klyx.flux.effects.EffectConfiguration;
import org.klyx.flux.effects.EffectFormat;
import org.klyx.flux.utils.Position;
import org.klyx.flux.utils.Vector3;

import java.util.Collection;

public class DefaultParticleRenderer implements ParticleRenderer {

    private double viewDistance = 64.0;

    @Override
    public void render(EffectFormat effect, Position position, Collection<Audience> viewers) {
        Collection<Vector3> points = effect.getPoints();

        ParticleConfiguration config = new DefaultParticleConfiguration();
    }

    @Override
    public void render(EffectFormat effect, EffectConfiguration configuration, ParticleConfiguration particleConfiguration, Position position, Collection<Audience> viewers) {

    }

    @Override
    public void render(EffectFormat effect, ParticleConfiguration particleConfiguration, Position position, Collection<Audience> viewers) {
        Collection<Audience>
    }

    @Override
    public double getViewDistance() {
        return 0;
    }

    @Override
    public void setViewDistance(double distance) {

    }
}
