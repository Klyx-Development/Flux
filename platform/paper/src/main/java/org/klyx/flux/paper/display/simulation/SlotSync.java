package org.klyx.flux.paper.display.simulation;

import net.minecraft.network.protocol.Packet;
import org.klyx.exo.entity.meta.impl.MetaEntry;
import org.klyx.exo.entity.meta.impl.MetaViewerGrouping;
import org.klyx.flux.paper.display.DisplayParticle;
import org.klyx.flux.paper.display.DisplayStyle;
import org.klyx.flux.paper.display.viewer.ViewerLedger;
import org.klyx.flux.paper.display.viewer.ViewerSelector;
import org.klyx.flux.paper.display.viewer.ViewerSent;
import org.klyx.flux.sim.Emitter;
import org.klyx.flux.sim.EmitterView;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SlotSync implements EmitterView.Listener {
    private final Emitter emitter;
    private final DisplayParticle[] particles;
    private final DisplayStyle style;
    private final ViewerLedger ledger;
    private final List<UUID> active;
    private final double[] tolerances;
    private final ViewerSent sent;
    private final PacketOutbox outbox;

    public SlotSync(
            Emitter emitter, DisplayParticle[] particles, DisplayStyle style, ViewerLedger ledger,
            ViewerSelector.Selection selection, ViewerSent sent, PacketOutbox outbox
    ) {
        this.emitter = emitter;
        this.particles = particles;
        this.style = style;
        this.ledger = ledger;
        this.active = selection.viewers();
        this.tolerances = selection.tolerances();
        this.sent = sent;
        this.outbox = outbox;
    }

    @Override
    public void spawn(int slot) {
        DisplayParticle particle = this.particles[slot];
        if (particle == null) {
            particle = new DisplayParticle(this.style);
            this.particles[slot] = particle;
        }

        particle.reset(this.emitter.size(slot), this.emitter.roll(slot), this.emitter.rgb(slot));
    }

    @Override
    public void update(int slot, int changed) {
        DisplayParticle particle = this.particles[slot];
        boolean metaChanged = particle.update(
                changed, this.emitter.size(slot), this.emitter.roll(slot), this.emitter.rgb(slot));

        List<UUID> showing = this.ledger.showing(this.active, slot);
        if (showing.isEmpty()) return;

        if (metaChanged) queueMeta(particle, showing);
        if ((changed & EmitterView.MOVED) != 0) queueMoves(slot, particle);
    }

    @Override
    public void remove(int slot) {
        List<UUID> showing = this.ledger.showing(this.active, slot);
        DisplayParticle particle = this.particles[slot];

        for (UUID viewer : showing) {
            this.outbox.remove(viewer, particle.entityId());
        }

        this.ledger.forget(this.active, slot);
    }

    public void spawnMissing() {
        for (UUID viewer : this.active) {
            BitSet shown = this.ledger.get(viewer);
            if (shown == null) continue;

            int version = MetaViewerGrouping.resolveVersion(viewer);
            double[] sentTo = this.sent.of(viewer);

            for (int slot = 0; slot < this.emitter.highWater(); slot++) {
                if (!this.emitter.isAlive(slot) || shown.get(slot)) continue;

                double x = this.emitter.x(slot);
                double y = this.emitter.y(slot);
                double z = this.emitter.z(slot);

                DisplayParticle particle = this.particles[slot];
                this.outbox.essential(viewer, particle.addPacket(x, y, z));

                Packet<?> data = particle.dataPacket(particle.takeAll(), version);
                if (data != null) this.outbox.essential(viewer, data);

                ViewerSent.record(sentTo, slot, x, y, z);
                shown.set(slot);
            }
        }
    }

    // checks against the last position sent rather than the previous tick
    // this allows flux to skip some movement and save a bit of bandwidth
    // when a viewer is far enough away to not notice a difference
    private void queueMoves(int slot, DisplayParticle particle) {
        double x = this.emitter.x(slot);
        double y = this.emitter.y(slot);
        double z = this.emitter.z(slot);

        Packet<?> move = null;

        for (int i = 0; i < this.active.size(); i++) {
            UUID viewer = this.active.get(i);

            BitSet shown = this.ledger.get(viewer);
            if (shown == null || !shown.get(slot)) continue;

            double[] sentTo = this.sent.of(viewer);
            if (!ViewerSent.movedMoreThan(sentTo, slot, x, y, z, this.tolerances[i])) continue;

            ViewerSent.record(sentTo, slot, x, y, z);

            if (move == null) move = particle.positionPacket(x, y, z);
            this.outbox.move(viewer, particle.entityId(), move);
        }
    }

    private void queueMeta(DisplayParticle particle, List<UUID> showing) {
        List<MetaEntry<?>> dirty = particle.takeDirty();
        if (dirty.isEmpty()) return;

        for (Map.Entry<Integer, List<UUID>> group : MetaViewerGrouping.groupByProtocolVersion(showing).entrySet()) {
            Packet<?> data = particle.dataPacket(dirty, group.getKey());
            if (data == null) continue;

            for (UUID viewer : group.getValue()) {
                this.outbox.essential(viewer, data);
            }
        }
    }
}
