package org.klyx.flux.paper.display.viewer;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.paper.display.DisplayParticle;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Which particle slots each viewer currently sees. */
public final class ViewerLedger {
    private final ConcurrentHashMap<UUID, BitSet> shown = new ConcurrentHashMap<>();
    private final int slots;

    public ViewerLedger(int slots) {
        this.slots = slots;
    }

    public void add(UUID viewer) {
        this.shown.putIfAbsent(viewer, new BitSet(this.slots));
    }

    public @Nullable BitSet remove(UUID viewer) {
        return this.shown.remove(viewer);
    }

    public @Nullable BitSet get(UUID viewer) {
        return this.shown.get(viewer);
    }

    public Set<Map.Entry<UUID, BitSet>> entries() {
        return this.shown.entrySet();
    }

    public Collection<UUID> ids() {
        return this.shown.keySet();
    }

    public int size() {
        return this.shown.size();
    }

    public void clear() {
        this.shown.clear();
    }

    public List<UUID> showing(List<UUID> active, int slot) {
        List<UUID> showing = new ArrayList<>(active.size());

        for (UUID viewer : active) {
            BitSet bits = this.shown.get(viewer);
            if (bits != null && bits.get(slot)) showing.add(viewer);
        }

        return showing;
    }

    public void forget(List<UUID> active, int slot) {
        for (UUID viewer : active) {
            this.shown.get(viewer).clear(slot);
        }
    }

    public static void despawnAll(UUID viewer, BitSet shown, DisplayParticle[] particles) {
        List<UUID> single = List.of(viewer);

        for (int slot = shown.nextSetBit(0); slot >= 0; slot = shown.nextSetBit(slot + 1)) {
            DisplayParticle particle = particles[slot];
            if (particle != null) particle.despawn(single);
        }

        shown.clear();
    }
}
