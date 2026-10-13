package org.klyx.flux.blueprint.reader;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.effect.Effect;
import org.klyx.flux.effect.Layer;
import org.klyx.flux.expr.Expression;
import org.klyx.flux.timeline.Cue;
import org.klyx.flux.timeline.LoopMode;
import org.klyx.flux.timeline.Timeline;
import org.klyx.flux.timeline.Track;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EffectReader {
    private final CompileContext context;

    public EffectReader(CompileContext context) {
        this.context = context;
    }

    public @Nullable Effect read(Node root) {
        if (!root.isObject()) {
            root.error("a blueprint must be an object but this is " + root.describe());
            return null;
        }

        root.onlyAllow("description", "duration", "loop", "tracks", "expressions", "cues", "layers", "on_cue");

        double duration = duration(root);
        LoopMode loop = root.enumValue("loop", LoopMode.class, LoopMode.ONCE);

        Map<String, Track> tracks = new TrackReader(this.context).read(root.child("tracks"));
        Set<String> trackNames = new HashSet<>(tracks.keySet());
        Map<String, Expression> expressions = new ExpressionReader(this.context, trackNames).read(root.child("expressions"));

        Set<String> readable = new HashSet<>(trackNames);
        readable.addAll(expressions.keySet());

        List<Cue> cues = new CueReader(this.context, duration).read(root.child("cues"));
        List<Layer> layers = new LayerReader(this.context, root, readable).read(root.child("layers"));
        Map<String, Effect> cueEffects = new CueEffectReader(this.context, cues).read(root.child("on_cue"));

        if (Double.isNaN(duration)) return null;
        if (!this.context.problems().isEmpty()) return null;

        Timeline timeline = timeline(duration, loop, tracks, expressions, cues);

        Effect.Builder effect = Effect.builder(timeline);
        layers.forEach(effect::layer);
        cueEffects.forEach(effect::onCue);

        return effect.build();
    }

    private static Timeline timeline(
            double duration, LoopMode loop, Map<String, Track> tracks, Map<String, Expression> expressions, List<Cue> cues
    ) {
        Timeline.Builder timeline = Timeline.builder(duration).loop(loop);
        tracks.forEach(timeline::track);
        expressions.forEach(timeline::expression);
        cues.forEach(cue -> timeline.cue(cue.time(), cue.name()));

        return timeline.build();
    }

    private double duration(Node root) {
        double duration = root.requireNumber("duration");
        if (Double.isNaN(duration) || duration > 0) return duration;

        Node node = root.child("duration");
        if (node != null) node.error("duration must be more than 0 but is " + duration);

        return Double.NaN;
    }
}
