package org.klyx.flux.particle;

import org.klyx.flux.utils.Vector3;
import org.klyx.flux.utils.player.FluxPlayer;
import org.klyx.flux.utils.Position;
import org.klyx.flux.effects.EffectFormat;

import java.util.Collection;

public interface ParticleRenderer {

    void render(EffectFormat effect, Position position, Collection<FluxPlayer> viewers);

    void render(Collection<Vector3> points, ParticleConfiguration particleConfiguration, Position position,
                Collection<FluxPlayer> viewers);

    double getViewDistance();
    void setViewDistance(double distance);

    boolean isDistanceCullingEnabled();
    void setDistanceCullingEnabled(boolean enabled);

    boolean canSpawnAt(Position position);
    Collection<FluxPlayer> getVisiblePlayers(Position position);
}
