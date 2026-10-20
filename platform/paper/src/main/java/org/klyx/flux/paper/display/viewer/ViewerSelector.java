package org.klyx.flux.paper.display.viewer;

import org.klyx.flux.effect.anchor.Anchor;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.paper.display.DisplayParticle;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ViewerSelector {
    private final ViewerLedger ledger;
    private final ViewerPositions positions;
    private final Anchor anchor;
    private final double maxDistanceSquared;
    private final DisplayParticle[] particles;

    public ViewerSelector(
            ViewerLedger ledger, ViewerPositions positions, Anchor anchor, double maxDistanceSquared, DisplayParticle[] particles
    ) {
        this.ledger = ledger;
        this.positions = positions;
        this.anchor = anchor;
        this.maxDistanceSquared = maxDistanceSquared;
        this.particles = particles;
    }

    public List<UUID> select() {
        List<UUID> active = new ArrayList<>(this.ledger.size());

        for (Map.Entry<UUID, BitSet> entry : this.ledger.entries()) {
            UUID viewer = entry.getKey();
            BitSet shown = entry.getValue();

            if (this.positions.isOffline(viewer)) {
                this.ledger.remove(viewer);
                continue;
            }

            Vec3 where = this.positions.positionOf(viewer);
            if (where == null || !within(where)) {
                if (!shown.isEmpty()) ViewerLedger.despawnAll(viewer, shown, this.particles);
                continue;
            }

            active.add(viewer);
        }

        return active;
    }

    private boolean within(Vec3 viewer) {
        Vec3 here = this.anchor.position();
        double deltaX = viewer.x() - here.x();
        double deltaY = viewer.y() - here.y();
        double deltaZ = viewer.z() - here.z();

        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ <= this.maxDistanceSquared;
    }
}
