package org.klyx.flux.paper.display.viewer;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.math.Vec3;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerPositions {
    private final ConcurrentHashMap<UUID, Slot> slots = new ConcurrentHashMap<>();
    private final Reading scratch = new Reading();
    private long pass;

    public void refresh(Iterable<? extends Collection<UUID>> watchers, Source source) {
        long now = ++this.pass;

        for (Collection<UUID> watching : watchers) {
            for (UUID id : watching) {
                Slot slot = this.slots.computeIfAbsent(id, _ -> new Slot());

                if (slot.stamp == now) continue;
                slot.stamp = now;

                if (!source.read(id, this.scratch)) {
                    slot.online = false;
                    continue;
                }

                if (!Objects.equals(slot.world, this.scratch.world)) slot.world = this.scratch.world;
                slot.x = this.scratch.x;
                slot.y = this.scratch.y;
                slot.z = this.scratch.z;
                slot.online = true;
            }
        }

        this.slots.values().removeIf(slot -> slot.stamp != now);
    }

    public boolean isOffline(UUID player) {
        Slot slot = this.slots.get(player);
        return slot != null && !slot.online;
    }

    public @Nullable Vec3 positionIn(UUID player, String world) {
        Slot slot = this.slots.get(player);
        if (slot == null || !slot.online || !world.equals(slot.world)) return null;

        return new Vec3(slot.x, slot.y, slot.z);
    }

    public boolean isKnown(UUID player) {
        Slot slot = this.slots.get(player);
        return slot != null && slot.online;
    }

    public int size() {
        return this.slots.size();
    }

    public static final class Reading {
        public @Nullable String world;
        public double x;
        public double y;
        public double z;
    }

    @FunctionalInterface
    public interface Source {
        boolean read(UUID player, Reading out);
    }

    private static final class Slot {
        private volatile boolean online;
        private volatile @Nullable String world;
        private volatile double x;
        private volatile double y;
        private volatile double z;
        private long stamp;
    }
}
