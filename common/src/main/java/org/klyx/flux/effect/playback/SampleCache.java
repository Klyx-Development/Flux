package org.klyx.flux.effect.playback;

import org.klyx.flux.sample.PointCloud;
import org.klyx.flux.sample.SamplingStyle;
import org.klyx.flux.shape.Shape;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SampleCache {
    private record Key(Shape shape, SamplingStyle style, double density) { }

    private final Map<Key, PointCloud> entries;

    public SampleCache(int maxEntries) {
        if (maxEntries < 1) throw new IllegalArgumentException("maxEntries has to be positive: " + maxEntries);

        this.entries = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Key, PointCloud> eldest) {
                return size() > maxEntries;
            }
        };
    }

    public PointCloud get(Shape shape, SamplingStyle style, double density) {
        Key key = new Key(shape, style, density);

        synchronized (this.entries) {
            PointCloud cached = this.entries.get(key);
            if (cached != null) return cached;
        }

        // sample outside the lock so a slow shape can't block other effects
        PointCloud sampled = shape.sample(style, density);

        synchronized (this.entries) {
            this.entries.putIfAbsent(key, sampled);
            return this.entries.get(key);
        }
    }

    public int size() {
        synchronized (this.entries) {
            return this.entries.size();
        }
    }
}
