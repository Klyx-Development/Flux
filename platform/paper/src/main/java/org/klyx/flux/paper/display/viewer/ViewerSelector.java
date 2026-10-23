package org.klyx.flux.paper.display.viewer;

import net.minecraft.network.protocol.Packet;
import org.klyx.flux.dispatch.PacketDispatcher;
import org.klyx.flux.effect.anchor.Anchor;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.paper.display.DisplayParticle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ViewerSelector {
    private static final double BASE_TOLERANCE = 1.0 / 128;
    // tolerance increments by one per this many blocks, e.g. 16 blocks away is 2x and 64 is 5x
    private static final double TOLERANCE_RANGE = 16;

    private final ViewerLedger ledger;
    private final ViewerPositions positions;
    private final Anchor anchor;
    private final double maxDistanceSquared;
    private final DisplayParticle[] particles;
    private final PacketDispatcher<Packet<?>> dispatcher;
    private final ViewerSent sent;

    public ViewerSelector(
            ViewerLedger ledger, ViewerPositions positions, Anchor anchor, double maxDistanceSquared,
            DisplayParticle[] particles, PacketDispatcher<Packet<?>> dispatcher, ViewerSent sent
    ) {
        this.sent = sent;
        this.ledger = ledger;
        this.positions = positions;
        this.anchor = anchor;
        this.maxDistanceSquared = maxDistanceSquared;
        this.particles = particles;
        this.dispatcher = dispatcher;
    }

    /**
     * @param tolerances how far a particle has to move before it's worth sending that packet (per-viewer)
     */
    public record Selection(List<UUID> viewers, double[] tolerances) { }

    public Selection select() {
        List<UUID> active = new ArrayList<>(this.ledger.size());
        double[] tolerances = new double[this.ledger.size()];

        for (Map.Entry<UUID, BitSet> entry : this.ledger.entries()) {
            UUID viewer = entry.getKey();
            BitSet shown = entry.getValue();

            if (this.positions.isOffline(viewer)) {
                this.ledger.remove(viewer);
                this.sent.forget(viewer);
                continue;
            }

            Vec3 where = this.positions.positionOf(viewer);
            double distanceSquared = where == null ? Double.MAX_VALUE : distanceSquared(where);
            if (distanceSquared > this.maxDistanceSquared) {
                if (!shown.isEmpty()) ViewerLedger.despawnAll(viewer, shown, this.particles, this.dispatcher);
                continue;
            }

            if (active.size() == tolerances.length) tolerances = Arrays.copyOf(tolerances, tolerances.length * 2 + 1);

            tolerances[active.size()] = BASE_TOLERANCE * (1 + Math.sqrt(distanceSquared) / TOLERANCE_RANGE);
            active.add(viewer);
        }

        return new Selection(active, tolerances);
    }

    private double distanceSquared(Vec3 viewer) {
        Vec3 here = this.anchor.position();
        double deltaX = viewer.x() - here.x();
        double deltaY = viewer.y() - here.y();
        double deltaZ = viewer.z() - here.z();

        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }
}
