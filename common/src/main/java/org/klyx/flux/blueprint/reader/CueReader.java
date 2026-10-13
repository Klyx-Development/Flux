package org.klyx.flux.blueprint.reader;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.timeline.Cue;

import java.util.ArrayList;
import java.util.List;

public final class CueReader extends SectionReader<List<Cue>> {
    private final double duration;

    public CueReader(CompileContext context, double duration) {
        super(context);
        this.duration = duration;
    }

    @Override
    public List<Cue> read(@Nullable Node node) {
        List<Cue> cues = new ArrayList<>();
        if (node == null) return cues;

        for (Node item : node.items()) {
            Cue cue = cue(item);
            if (cue != null) cues.add(cue);
        }

        return cues;
    }

    private @Nullable Cue cue(Node item) {
        if (!item.isObject()) {
            item.error("expected a cue but got " + item.describe() + " instead");
            return null;
        }

        item.onlyAllow("time", "name");
        double time = item.requireNumber("time");
        String name = item.requireString("name");

        if (Double.isNaN(time)) return null;

        if (name.isBlank()) {
            Node nameNode = item.child("name");
            if (nameNode != null) nameNode.error("a cue needs a name");
            return null;
        }

        if (time < 0 || (!Double.isNaN(this.duration) && time > this.duration)) {
            Node timeNode = item.child("time");
            if (timeNode != null) {
                timeNode.error("time " + time + " is outside the effect, which lasts " + this.duration + " seconds");
            }

            return null;
        }

        return new Cue(time, name);
    }
}
