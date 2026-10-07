package org.klyx.flux.effect.playback;

import org.klyx.flux.effect.LayerCloud;
import org.klyx.flux.math.Vec3;

import java.util.List;

@FunctionalInterface
public interface EffectSink {
    void render(List<LayerCloud> layers, Vec3 origin);
}
