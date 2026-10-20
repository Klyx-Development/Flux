package org.klyx.flux;

import org.jspecify.annotations.Nullable;

public final class Flux {
    private static volatile @Nullable FluxPlatform platform;

    public static synchronized void init(FluxPlatform platform) {
        if (Flux.platform != null) {
            throw new IllegalStateException("Flux is already initialised");
        }

        Flux.platform = platform;
    }

    public static synchronized void destroy() {
        Flux.platform = null;
    }

    public static FluxPlatform platform() {
        FluxPlatform current = Flux.platform;
        if (current == null) {
            throw new IllegalStateException("Flux hasn't been initialised");
        }

        return current;
    }
}
