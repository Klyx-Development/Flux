package org.klyx.flux.paper;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.klyx.flux.dispatch.PacketSender;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PaperPacketSender implements PacketSender<Packet<?>> {
    public static final int MAX_PACKETS_PER_BUNDLE = 128;

    private final Logger logger;

    public PaperPacketSender(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void sendBatch(UUID viewer, List<Packet<?>> packets) {
        Player player = Bukkit.getPlayer(viewer);
        if (player == null) return;

        ServerPlayer handle = ((CraftPlayer) player).getHandle();
        for (Packet<?> packet : bundle(packets)) {
            handle.connection.send(packet);
        }
    }

    @Override
    public void onFlushError(UUID viewer, Exception exception) {
        this.logger.error("failed to flush particle packets for {}", viewer, exception);
    }

    @SuppressWarnings("unchecked")
    public static List<Packet<?>> bundle(List<Packet<?>> packets) {
        List<Packet<?>> bundles = new ArrayList<>((packets.size() / MAX_PACKETS_PER_BUNDLE) + 1);

        for (int from = 0; from < packets.size(); from += MAX_PACKETS_PER_BUNDLE) {
            List<Packet<?>> chunk = packets.subList(from, Math.min(from + MAX_PACKETS_PER_BUNDLE, packets.size()));

            if (chunk.size() == 1) {
                bundles.add(chunk.getFirst());
            } else {
                // intellij says that this cast is redundant but I can't build without it...
                Iterable<Packet<? super ClientGamePacketListener>> copy =
                        (Iterable<Packet<? super ClientGamePacketListener>>) (Iterable<?>) List.copyOf(chunk);
                bundles.add(new ClientboundBundlePacket(copy));
            }
        }

        return bundles;
    }
}
