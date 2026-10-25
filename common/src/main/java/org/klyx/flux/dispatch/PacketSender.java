package org.klyx.flux.dispatch;

import java.util.List;
import java.util.UUID;

public interface PacketSender<P> {
    void sendBatch(UUID viewer, List<P> packets);

    default double congestion(UUID viewer) {
        return 0;
    }

    default void onFlushError(UUID viewer, Exception exception) {
    }
}
