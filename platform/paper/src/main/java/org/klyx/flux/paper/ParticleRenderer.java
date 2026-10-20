package org.klyx.flux.paper;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.klyx.flux.effect.LayerCloud;
import org.klyx.flux.effect.ParticleBrush;
import org.klyx.flux.sample.PointCloud;
import org.klyx.flux.shader.StyledCloud;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ParticleRenderer {
    private final FluxPaperPlatform platform;
    private final Map<ParticleBrush, BrushOptions> brushes = new HashMap<>();

    public ParticleRenderer(FluxPaperPlatform platform) {
        this.platform = platform;
    }

    public void render(List<LayerCloud> layers, Location origin, Collection<? extends Player> viewers) {
        if (origin.getWorld() == null) return;

        List<Packet<?>> packets = new ArrayList<>();
        for (LayerCloud layer : layers) {
            draw(layer.brush(), layer.cloud(), origin, packets);
        }

        if (!packets.isEmpty()) send(packets, origin, viewers);
    }

    public void check(ParticleBrush brush) {
        options(brush);
    }

    private void draw(ParticleBrush brush, StyledCloud cloud, Location origin, List<Packet<?>> packets) {
        if (cloud.size() == 0) return;

        BrushOptions options = options(brush);
        PointCloud points = cloud.points();

        if (!options.usesColor()) {
            ParticleOptions plain = options.create(0xFFFFFF);
            for (int i = 0; i < cloud.size(); i++) {
                packets.add(packet(plain, brush, origin, points, i));
            }

            return;
        }

        Int2ObjectOpenHashMap<ParticleOptions> byColor = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < cloud.size(); i++) {
            ParticleOptions coloured = byColor.computeIfAbsent(cloud.rgb(i) & 0xFFFFFF, options::create);
            packets.add(packet(coloured, brush, origin, points, i));
        }
    }

    private synchronized BrushOptions options(ParticleBrush brush) {
        BrushOptions found = this.brushes.get(brush);
        if (found == null) {
            found = BrushOptions.of(brush);
            this.brushes.put(brush, found);
        }

        return found;
    }

    private static Packet<?> packet(
            ParticleOptions options, ParticleBrush brush, Location origin, PointCloud cloud, int index
    ) {
        return new ClientboundLevelParticlesPacket(
                options, false, false,
                origin.getX() + cloud.x(index), origin.getY() + cloud.y(index), origin.getZ() + cloud.z(index),
                0f, 0f, 0f, (float) brush.speed(), 1);
    }

    private void send(List<Packet<?>> packets, Location origin, Collection<? extends Player> viewers) {
        for (Player viewer : viewers) {
            if (viewer.getWorld().equals(origin.getWorld())) {
                this.platform.dispatcher().schedule(viewer.getUniqueId(), packets);
            }
        }
    }
}
