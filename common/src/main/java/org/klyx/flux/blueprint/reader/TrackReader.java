package org.klyx.flux.blueprint.reader;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.math.Easing;
import org.klyx.flux.timeline.Keyframe;
import org.klyx.flux.timeline.Track;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TrackReader extends SectionReader<Map<String, Track>> {
    public TrackReader(CompileContext context) {
        super(context);
    }

    @Override
    public Map<String, Track> read(@Nullable Node node) {
        Map<String, Track> tracks = new LinkedHashMap<>();
        if (node == null) return tracks;

        for (Map.Entry<String, Node> entry : node.entries().entrySet()) {
            Track track = track(entry.getValue());
            if (track != null) tracks.put(entry.getKey(), track);
        }

        return tracks;
    }

    private @Nullable Track track(Node node) {
        if (node.isNumber()) {
            double value = node.asNumber(Double.NaN);
            return Double.isNaN(value) ? null : Track.constant(value);
        }

        if (!node.isArray()) {
            node.error("expected a number or a list of keyframes but got " + node.describe() + " instead");
            return null;
        }

        List<Keyframe> keyframes = new ArrayList<>();
        for (Node item : node.items()) {
            Keyframe keyframe = keyframe(item);
            if (keyframe != null) keyframes.add(keyframe);
        }

        if (keyframes.isEmpty()) {
            if (node.items().isEmpty()) node.error("track needs at least one keyframe");
            return null;
        }

        return new Track(keyframes);
    }

    private @Nullable Keyframe keyframe(Node node) {
        if (!node.isObject()) {
            node.error("expected a keyframe but got " + node.describe() + " instead");
            return null;
        }

        node.onlyAllow("time", "value", "easing");
        double time = node.requireNumber("time");
        double value = node.requireNumber("value");
        Easing easing = node.enumValue("easing", Easing.class, Easing.LINEAR);

        if (Double.isNaN(time) || Double.isNaN(value)) return null;

        if (time < 0) {
            Node timeNode = node.child("time");
            if (timeNode != null) timeNode.error("time can't be negative but got " + time);
            return null;
        }

        return new Keyframe(time, value, easing);
    }
}
