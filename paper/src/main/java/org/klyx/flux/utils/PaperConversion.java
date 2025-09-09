package org.klyx.flux.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class PaperConversion {
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

}
