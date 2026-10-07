package org.klyx.flux.effect;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.effect.anchor.Bounds;
import org.klyx.flux.effect.playback.SampleCache;
import org.klyx.flux.math.Transform;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.sample.PointCloud;
import org.klyx.flux.shader.StyledCloud;
import org.klyx.flux.timeline.Frame;
import org.klyx.flux.timeline.Timeline;
import org.klyx.flux.timeline.Tracks;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record Effect(List<Layer> layers, Timeline timeline, Map<String, Effect> cueEffects) implements ParticleLike {
    private static final double FIT_DENSITY_STEPS = 4;

    public Effect {
        if (layers.isEmpty()) throw new IllegalArgumentException("an effect needs at least one layer");

        layers = List.copyOf(layers);
        cueEffects = Map.copyOf(cueEffects);
    }

    public static Builder builder(Timeline timeline) {
        return new Builder(timeline);
    }

    public StyledCloud compose(Frame frame, SampleCache cache) {
        return compose(frame, cache, Bounds.NONE);
    }

    public StyledCloud compose(Frame frame, SampleCache cache, Bounds bounds) {
        List<StyledCloud> parts = new ArrayList<>(this.layers.size());

        for (LayerCloud layer : composeLayers(frame, cache, allLayers(), bounds)) {
            parts.add(layer.cloud());
        }

        return StyledCloud.concat(parts);
    }

    public List<LayerCloud> composeLayers(Frame frame, SampleCache cache) {
        return composeLayers(frame, cache, allLayers(), Bounds.NONE);
    }

    // kept separate so each layer can be drawn with its own particles
    public List<LayerCloud> composeLayers(Frame frame, SampleCache cache, BitSet due, Bounds bounds) {
        Transform animated = animatedTransform(frame);
        List<LayerCloud> parts = new ArrayList<>(this.layers.size());

        for (int i = 0; i < this.layers.size(); i++) {
            if (!due.get(i)) continue;

            Layer layer = this.layers.get(i);
            Bounds box = layer.fit() == Fit.BOUNDS && !bounds.isEmpty() ? bounds.grown(layer.margin()) : null;

            PointCloud base = cache.get(layer.shape(), layer.style(), box == null ? layer.density() : fittedDensity(layer, box));
            PointCloud shaped = layer.modifier().apply(frame).apply(base);
            StyledCloud coloured = layer.shader().apply(frame).shade(shaped);

            parts.add(new LayerCloud(layer.particle(), place(coloured, layer, box, animated)));
        }

        return parts;
    }

    private StyledCloud place(StyledCloud coloured, Layer layer, @Nullable Bounds box, Transform animated) {
        if (box == null) return coloured.transformed(layer.transform().then(animated));

        return coloured.transformed(layer.transform())
                .stretched(box.size(), box.center())
                .transformed(animated);
    }

    private static double fittedDensity(Layer layer, Bounds box) {
        return layer.density() * Math.ceil(box.longest() * FIT_DENSITY_STEPS) / FIT_DENSITY_STEPS;
    }

    private BitSet allLayers() {
        BitSet all = new BitSet(this.layers.size());
        all.set(0, this.layers.size());

        return all;
    }

    private static Transform animatedTransform(Frame frame) {
        Vec3 offset = new Vec3(frame.value(Tracks.X, 0), frame.value(Tracks.Y, 0), frame.value(Tracks.Z, 0));

        return Transform.IDENTITY
                .translation(offset)
                .eulerRotation(
                        Math.toRadians(frame.value(Tracks.PITCH, 0)),
                        Math.toRadians(frame.value(Tracks.YAW, 0)),
                        Math.toRadians(frame.value(Tracks.ROLL, 0)))
                .scale(frame.value(Tracks.SCALE, 1));
    }

    public static final class Builder {
        private final Timeline timeline;
        private final List<Layer> layers = new ArrayList<>();
        private final Map<String, Effect> cueEffects = new LinkedHashMap<>();

        private Builder(Timeline timeline) {
            this.timeline = timeline;
        }

        public Builder layer(Layer layer) {
            this.layers.add(layer);
            return this;
        }

        public Builder layer(Layer.Builder layer) {
            return layer(layer.build());
        }

        public Builder onCue(String cue, Effect effect) {
            this.cueEffects.put(cue, effect);
            return this;
        }

        public Effect build() {
            return new Effect(this.layers, this.timeline, this.cueEffects);
        }
    }
}
