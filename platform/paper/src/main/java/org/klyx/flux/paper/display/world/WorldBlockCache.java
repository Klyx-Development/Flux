package org.klyx.flux.paper.display.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.klyx.flux.sim.collision.Collider;
import org.klyx.flux.sim.collision.VoxelCollider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WorldBlockCache {
    private final Map<String, WorldBlocks> worlds = new ConcurrentHashMap<>();

    public Collider colliderFor(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return Collider.NONE;

        WorldBlocks blocks = this.worlds.computeIfAbsent(worldName, _ -> new WorldBlocks(world));
        return new VoxelCollider(blocks);
    }

    public void refresh() {
        for (WorldBlocks blocks : this.worlds.values()) {
            blocks.refresh();
        }
    }
}
