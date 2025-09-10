package org.klyx.flux.impl;

import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftParticle;
import org.klyx.flux.particle.ParticleConfiguration;
import org.klyx.flux.particle.ParticleRenderer;
import org.klyx.flux.effects.EffectFormat;
import org.klyx.flux.utils.PaperConversion;
import org.klyx.flux.utils.player.FluxPlayer;
import org.klyx.flux.utils.Position;
import org.klyx.flux.utils.Vector3;
import org.klyx.flux.utils.player.PaperFluxPlayer;

import java.util.Collection;
import java.util.List;

public class DefaultParticleRenderer implements ParticleRenderer {

    private double viewDistance = 64.0;
    private boolean distanceCullingEnabled = true;

    @Override
    public void render(EffectFormat effect, Position position, Collection<FluxPlayer> viewers) {
        ParticleConfiguration config = new DefaultParticleConfiguration();
        render(effect.getPoints(), config, position, viewers);
    }

    @Override
    public void render(Collection<Vector3> points, ParticleConfiguration particleConfiguration, Position position, Collection<FluxPlayer> viewers) {
        Collection<FluxPlayer> visiblePlayers = getVisiblePlayers(position);
        visiblePlayers.retainAll(viewers);

        if (visiblePlayers.isEmpty()) return;

        for (Vector3 point : points) {
            Position particleLocation = position.clone().add(point);
            Location bukkitLocation = PaperConversion.toBukkit(particleLocation);

            for (FluxPlayer player : visiblePlayers) {
                PaperFluxPlayer paperFluxPlayer = (PaperFluxPlayer) player;

                if (distanceCullingEnabled &&
                        paperFluxPlayer.getLocation().distance(bukkitLocation) > viewDistance) {
                    continue;
                }

                paperFluxPlayer.sendPacket(
                        new ClientboundLevelParticlesPacket(
                                CraftParticle.createParticleParam(PaperConversion.toBukkit(particleConfiguration.getParticleType()),
                                        particleConfiguration.getParticleData()),
                                false,
                                false,
                                bukkitLocation.getX(),
                                bukkitLocation.getY(),
                                bukkitLocation.getZ(),
                                (float) particleConfiguration.getOffsetX(),
                                (float) particleConfiguration.getOffsetY(),
                                (float) particleConfiguration.getOffsetZ(),
                                (float) particleConfiguration.getExtra(),
                                particleConfiguration.getCount()
                        )
                );
            }
        }
    }

    @Override
    public double getViewDistance() {
        return viewDistance;
    }

    @Override
    public void setViewDistance(double distance) {
        this.viewDistance = Math.max(1, distance);
    }

    @Override
    public boolean isDistanceCullingEnabled() {
        return distanceCullingEnabled;
    }

    @Override
    public void setDistanceCullingEnabled(boolean enabled) {
        this.distanceCullingEnabled = enabled;
    }

    @Override
    public boolean canSpawnAt(Position position) {
        Location location = PaperConversion.toBukkit(position);
        return location.getWorld() != null &&
                location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    @Override
    public Collection<FluxPlayer> getVisiblePlayers(Position position) {
        if (!canSpawnAt(position)) {
            return List.of();
        }

        Location location = PaperConversion.toBukkit(position);

        return location.getWorld().getPlayers().stream()
                .filter(player -> player.getLocation().distance(location) <= viewDistance)
                .map(player -> (FluxPlayer) PaperConversion.fromBukkit(player))
                .toList();
    }
}
