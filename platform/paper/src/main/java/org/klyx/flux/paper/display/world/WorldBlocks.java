package org.klyx.flux.paper.display.world;

import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.klyx.flux.sim.collision.VoxelCollider;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WorldBlocks implements VoxelCollider.Blocks {
    private static final long MAX_AGE = 100;
    private static final long IDLE = 200;

    private final World world;
    private final int minY;
    private final int maxY;
    private final Map<Long, Entry> snapshots = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> wanted = new ConcurrentHashMap<>();

    private volatile long tick;

    public WorldBlocks(World world) {
        this.world = world;
        this.minY = world.getMinHeight();
        this.maxY = world.getMaxHeight();
    }

    @Override
    public boolean isSolid(int x, int y, int z) {
        if (y < this.minY || y >= this.maxY) return false;

        long key = key(x >> 4, z >> 4);
        Entry entry = this.snapshots.get(key);
        if (entry == null) {
            this.wanted.put(key, Boolean.TRUE);
            return false;
        }

        entry.lastUsed = this.tick;
        if (this.tick - entry.takenAt > MAX_AGE) this.wanted.put(key, Boolean.TRUE);

        return entry.snapshot.getBlockType(x & 15, y, z & 15).isSolid();
    }

    public void refresh() {
        long now = ++this.tick;
        int taken = 0;

        if (!this.wanted.isEmpty()) {
            Iterator<Long> iterator = this.wanted.keySet().iterator();

            while (iterator.hasNext() && taken < 4) {
                long key = iterator.next();
                iterator.remove();

                int chunkX = (int) (key >> 32);
                int chunkZ = (int) key;

                if (!this.world.isChunkLoaded(chunkX, chunkZ)) continue;

                // use chunk snapshots for collisions rather than loading chunks
                ChunkSnapshot snapshot = this.world.getChunkAt(chunkX, chunkZ, false)
                        .getChunkSnapshot(false, false, false);
                this.snapshots.put(key, new Entry(snapshot, now));
                taken++;
            }
        }

        if (now % 20 == 0) {
            this.snapshots.values().removeIf(entry -> now - entry.lastUsed > IDLE);
        }
    }

    private static long key(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    private static final class Entry {
        private final ChunkSnapshot snapshot;
        private final long takenAt;
        private volatile long lastUsed;

        private Entry(ChunkSnapshot snapshot, long takenAt) {
            this.snapshot = snapshot;
            this.takenAt = takenAt;
            this.lastUsed = takenAt;
        }
    }
}
