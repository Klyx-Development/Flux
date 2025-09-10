package org.klyx.flux.utils.player;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface PlayerManager<T extends FluxPlayer> {

    T wrapPlayer(Object nativePlayer);
    <U> U unwrapPlayer(T player, Class<U> nativeType);
    @Nullable T getPlayer(UUID playerUUID);
    void cleanup(UUID playerId);

}
