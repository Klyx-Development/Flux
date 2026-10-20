package org.klyx.flux.paper.display.simulation;

import net.minecraft.network.protocol.Packet;
import org.klyx.exo.paper.packet.impl.Packets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MoveBatch {
    private final Map<UUID, List<Packet<?>>> moves = new HashMap<>();

    public void add(List<UUID> viewers, Packet<?> packet) {
        for (UUID viewer : viewers) {
            this.moves.computeIfAbsent(viewer, _ -> new ArrayList<>()).add(packet);
        }
    }

    public void send() {
        for (Map.Entry<UUID, List<Packet<?>>> entry : this.moves.entrySet()) {
            Packets.INSTANCE.sendPackets(entry.getKey(), entry.getValue().toArray(Packet[]::new));
        }
    }
}
