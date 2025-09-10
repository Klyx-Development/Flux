package org.klyx.flux.utils.player;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PaperPlayerManager implements PlayerManager<PaperFluxPlayer> {
    private final Map<UUID, PaperFluxPlayer> wrappedPlayers = new ConcurrentHashMap<>();

    @Override
    public PaperFluxPlayer wrapPlayer(Object nativePlayer) {
        if (!(nativePlayer instanceof Player player)) {
            throw new IllegalArgumentException("Expected a Bukkit player, instead got: " + nativePlayer.getClass());
        }

        return wrappedPlayers.computeIfAbsent(
                player.getUniqueId(), uuid ->
                new PaperFluxPlayer(player)
        );
    }

    @Override
    public <U> U unwrapPlayer(PaperFluxPlayer player, Class<U> nativeType) {
        if (nativeType == Player.class) {
            //noinspection unchecked
            return (U) player.getBukkitPlayer();
        }

        throw new IllegalArgumentException("Unsupported native type: " + nativeType);
    }

    @Override
    public @Nullable PaperFluxPlayer getPlayer(UUID playerUUID) {
        return wrappedPlayers.get(playerUUID);
    }


    @Override
    public void cleanup(UUID playerId) {
        wrappedPlayers.remove(playerId);
    }
}
