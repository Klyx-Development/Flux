package org.klyx.flux.utils.player;

import org.klyx.flux.utils.Position;

public interface FluxPlayer {

    Position getPosition();
    void sendPacket(Object packet);

}
