package org.klyx.flux.paper.display.viewer;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.math.Vec3;

import java.util.UUID;

public interface ViewerPositions {
    boolean isOffline(UUID viewer);

    @Nullable Vec3 positionOf(UUID viewer);
}
