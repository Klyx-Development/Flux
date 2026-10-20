package org.klyx.flux.paper.display.viewer;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.math.Vec3;

import java.util.UUID;

public final class WorldViewerPositions implements ViewerPositions {
    private final PlayerPositions players;
    private final String world;

    public WorldViewerPositions(PlayerPositions players, String world) {
        this.players = players;
        this.world = world;
    }

    @Override
    public boolean isOffline(UUID viewer) {
        return this.players.isOffline(viewer);
    }

    @Override
    public @Nullable Vec3 positionOf(UUID viewer) {
        return this.players.positionIn(viewer, this.world);
    }
}
