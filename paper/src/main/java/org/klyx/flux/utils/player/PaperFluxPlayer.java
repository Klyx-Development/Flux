package org.klyx.flux.utils.player;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.klyx.flux.utils.PaperConversion;
import org.klyx.flux.utils.Position;

public class PaperFluxPlayer implements FluxPlayer {

    private final Player bukkitPlayer;

    @ApiStatus.Internal
    Player getBukkitPlayer() {
        return bukkitPlayer;
    }

    public PaperFluxPlayer(Player player) {
        this.bukkitPlayer = player;
    }

    @Override
    public Position getPosition() {
        return PaperConversion.fromBukkit(bukkitPlayer.getLocation());
    }

    @Override
    public void sendPacket(Object packet) {
        if (!(packet instanceof Packet<?> nmsPacket)) {
            throw new IllegalArgumentException("Expected an NMS packet, instead got: " + packet.getClass());
        }

        ServerPlayer serverPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
        serverPlayer.connection.send(nmsPacket);
    }

    public Location getLocation() {
        return PaperConversion.toBukkit(this.getPosition());
    }
}
