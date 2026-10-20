package org.klyx.flux.paper.display.simulation;

import net.minecraft.network.protocol.Packet;
import org.klyx.exo.entity.ExoPos;
import org.klyx.flux.paper.display.DisplayParticle;
import org.klyx.flux.paper.display.DisplayStyle;
import org.klyx.flux.paper.display.viewer.ViewerLedger;
import org.klyx.flux.sim.Emitter;
import org.klyx.flux.sim.EmitterView;

import java.util.BitSet;
import java.util.List;
import java.util.UUID;

public final class SlotSync implements EmitterView.Listener {
    private final Emitter emitter;
    private final DisplayParticle[] particles;
    private final DisplayStyle style;
    private final ViewerLedger ledger;
    private final List<UUID> active;
    private final MoveBatch moves;

    public SlotSync(Emitter emitter, DisplayParticle[] particles, DisplayStyle style, ViewerLedger ledger,
             List<UUID> active, MoveBatch moves) {
        this.emitter = emitter;
        this.particles = particles;
        this.style = style;
        this.ledger = ledger;
        this.active = active;
        this.moves = moves;
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

        if (metaChanged) particle.updateMeta(showing);

        if ((changed & EmitterView.MOVED) != 0) {
            Packet<?> move = particle.positionPacket(this.emitter.x(slot), this.emitter.y(slot), this.emitter.z(slot));
            this.moves.add(showing, move);
        }
    }

    @Override
    public void remove(int slot) {
        List<UUID> showing = this.ledger.showing(this.active, slot);
        if (!showing.isEmpty()) this.particles[slot].despawn(showing);

        this.ledger.forget(this.active, slot);
    }

    public void spawnMissing() {
        for (UUID viewer : this.active) {
            BitSet shown = this.ledger.get(viewer);
            if (shown == null) continue;

            for (int slot = 0; slot < this.emitter.highWater(); slot++) {
                if (!this.emitter.isAlive(slot) || shown.get(slot)) continue;

                ExoPos where = new ExoPos(this.emitter.x(slot), this.emitter.y(slot), this.emitter.z(slot), 0f, 0f);
                this.particles[slot].spawn(where, List.of(viewer));
                shown.set(slot);
            }
        }
    }
}
