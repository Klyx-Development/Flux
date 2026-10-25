package org.klyx.flux.paper.display.simulation;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import org.klyx.flux.dispatch.PacketDispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Determines everything that a tick wants to send to a viewer
 * things like removals, spawns and metadata will never be left out, as that is essential
 */
public final class PacketOutbox {
    private static final int REMOVALS_PER_PACKET = 256;

    private final PacketDispatcher<Packet<?>> dispatcher;
    private final Map<UUID, IntArrayList> removals = new HashMap<>();
    private final Map<UUID, List<Packet<?>>> essential = new HashMap<>();
    private final List<Runnable> afterSpawns = new ArrayList<>();
    private final Map<UUID, Map<Integer, Supplier<Packet<?>>>> moves = new HashMap<>();

    public PacketOutbox(PacketDispatcher<Packet<?>> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void remove(UUID viewer, int entityId) {
        this.removals.computeIfAbsent(viewer, _ -> new IntArrayList()).add(entityId);
    }

    public void essential(UUID viewer, Packet<?> packet) {
        this.essential.computeIfAbsent(viewer, _ -> new ArrayList<>()).add(packet);
    }

    public void move(UUID viewer, int entityId, Supplier<Packet<?>> packet) {
        this.moves.computeIfAbsent(viewer, _ -> new HashMap<>()).put(entityId, packet);
    }

    public void afterSpawns(Runnable task) {
        this.afterSpawns.add(task);
    }

    public void flush() {
        Set<UUID> viewers = new HashSet<>(this.removals.keySet());
        viewers.addAll(this.essential.keySet());

        for (UUID viewer : viewers) {
            List<Packet<?>> ordered = new ArrayList<>();

            IntArrayList ids = this.removals.get(viewer);
            if (ids != null) {
                this.dispatcher.cancelLatest(viewer, ids.toIntArray());
                ordered.addAll(removals(ids));
            }

            List<Packet<?>> rest = this.essential.get(viewer);
            if (rest != null) ordered.addAll(rest);

            this.dispatcher.scheduleEssential(viewer, ordered);
        }

        this.afterSpawns.forEach(this.dispatcher::execute);
        this.moves.forEach(this.dispatcher::scheduleLatest);

        this.removals.clear();
        this.essential.clear();
        this.moves.clear();
        this.afterSpawns.clear();
    }

    // the remove entity packet supports an array of entity ids, so lets use that to our advantage!
    public static List<Packet<?>> removals(IntArrayList ids) {
        List<Packet<?>> packets = new ArrayList<>((ids.size() / REMOVALS_PER_PACKET) + 1);

        for (int from = 0; from < ids.size(); from += REMOVALS_PER_PACKET) {
            int to = Math.min(from + REMOVALS_PER_PACKET, ids.size());
            int[] chunk = new int[to - from];
            ids.getElements(from, chunk, 0, chunk.length);

            packets.add(new ClientboundRemoveEntitiesPacket(chunk));
        }

        return packets;
    }
}
