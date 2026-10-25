package org.klyx.flux.paper.display.viewer;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.klyx.flux.dispatch.PacketDispatcher;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class ViewerSent {
    private static final double DELTA_SCALE = 4096;

    private final ConcurrentHashMap<UUID, double[]> queued = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, long[]> client = new ConcurrentHashMap<>();
    private final PacketDispatcher<Packet<?>> dispatcher;
    private final int slots;

    public ViewerSent(int slots, PacketDispatcher<Packet<?>> dispatcher) {
        this.slots = slots;
        this.dispatcher = dispatcher;
    }

    public double[] of(UUID viewer) {
        return this.queued.computeIfAbsent(viewer, _ -> new double[this.slots * 3]);
    }

    public void forget(UUID viewer) {
        this.queued.remove(viewer);
        this.dispatcher.execute(() -> this.client.remove(viewer));
    }

    public void clear() {
        this.queued.clear();
        this.dispatcher.execute(this.client::clear);
    }

    public void place(UUID viewer, int slot, double x, double y, double z) {
        long[] positions = this.client.computeIfAbsent(viewer, _ -> new long[this.slots * 3]);
        int base = slot * 3;

        positions[base] = Math.round(x * DELTA_SCALE);
        positions[base + 1] = Math.round(y * DELTA_SCALE);
        positions[base + 2] = Math.round(z * DELTA_SCALE);
    }

    // The ClientboundEntityPositionSyncPacket is a really heavy packet (~60 bytes)
    // because the packet contains the PositionMoveRotation structure inside of it
    // so instead (if we can), send the much tinier ClientboundMoveEntity.Pos packet which is around ~15 bytes
    public Supplier<@Nullable Packet<?>> move(UUID viewer, int slot, int entityId, double x, double y, double z) {
        return () -> {
            long[] positions = this.client.get(viewer);
            if (positions == null) return null;

            int base = slot * 3;
            long targetX = Math.round(x * DELTA_SCALE);
            long targetY = Math.round(y * DELTA_SCALE);
            long targetZ = Math.round(z * DELTA_SCALE);

            long deltaX = targetX - positions[base];
            long deltaY = targetY - positions[base + 1];
            long deltaZ = targetZ - positions[base + 2];

            if (deltaX == 0 && deltaY == 0 && deltaZ == 0) return null;

            positions[base] = targetX;
            positions[base + 1] = targetY;
            positions[base + 2] = targetZ;

            if (fits(deltaX) && fits(deltaY) && fits(deltaZ)) {
                return new ClientboundMoveEntityPacket.Pos(entityId, (short) deltaX, (short) deltaY, (short) deltaZ, false);
            }

            PositionMoveRotation position = new PositionMoveRotation(new Vec3(x, y, z), Vec3.ZERO, 0f, 0f);
            return new ClientboundEntityPositionSyncPacket(entityId, position, false);
        };
    }

    private static boolean fits(long delta) {
        return delta >= Short.MIN_VALUE && delta <= Short.MAX_VALUE;
    }

    public static void record(double[] sent, int slot, double x, double y, double z) {
        int base = slot * 3;
        sent[base] = x;
        sent[base + 1] = y;
        sent[base + 2] = z;
    }

    public static boolean movedMoreThan(double[] sent, int slot, double x, double y, double z, double tolerance) {
        int base = slot * 3;
        return Math.abs(x - sent[base]) > tolerance
                || Math.abs(y - sent[base + 1]) > tolerance
                || Math.abs(z - sent[base + 2]) > tolerance;
    }
}
