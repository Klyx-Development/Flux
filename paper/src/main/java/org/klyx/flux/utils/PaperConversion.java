package org.klyx.flux.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.klyx.flux.particle.ParticleType;
import org.klyx.flux.utils.player.FluxPlayer;
import org.klyx.flux.utils.player.PaperFluxPlayer;
import org.klyx.flux.utils.player.PaperPlayerManager;

public class PaperConversion {
    private static final PaperPlayerManager playerManager = new PaperPlayerManager();

    public static Location toBukkit(Position position) {
        return new Location(
                Bukkit.getWorld(position.worldName()),
                position.x(), position.y(), position.z()
        );
    }

    public static Position fromBukkit(Location location) {
        return new Position(
                location.getX(), location.getY(), location.getZ(), location.getWorld().getName()
        );
    }

    public static Vector toBukkit(Vector3 vector3) {
        return new Vector(vector3.x(), vector3.y(), vector3.z());
    }

    public static Vector3 fromBukkit(Vector vector) {
        return new Vector3(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Particle toBukkit(ParticleType particleType) {
        return Particle.valueOf(particleType.name());
    }

    public static ParticleType fromBukkit(Particle particle) {
        return ParticleType.valueOf(particle.name());
    }

    public static Player toBukkit(PaperFluxPlayer fluxPlayer) {
        return playerManager.unwrapPlayer(fluxPlayer, Player.class);
    }

    public static PaperFluxPlayer fromBukkit(Player player) {
        return playerManager.wrapPlayer(player);
    }

}
