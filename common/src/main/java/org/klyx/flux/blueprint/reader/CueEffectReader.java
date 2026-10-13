package org.klyx.flux.blueprint.reader;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.BlueprintException;
import org.klyx.flux.blueprint.BlueprintProblem;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.effect.Effect;
import org.klyx.flux.timeline.Cue;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class CueEffectReader extends SectionReader<Map<String, Effect>> {
    private final Set<String> cueNames = new HashSet<>();

    public CueEffectReader(CompileContext context, List<Cue> cues) {
        super(context);
        cues.forEach(cue -> this.cueNames.add(cue.name()));
    }

    @Override
    public Map<String, Effect> read(@Nullable Node node) {
        Map<String, Effect> cueEffects = new LinkedHashMap<>();
        if (node == null) return cueEffects;

        for (Map.Entry<String, Node> entry : node.entries().entrySet()) {
            Node target = entry.getValue();
            if (!this.cueNames.contains(entry.getKey())) {
                target.error("there's no cue called \"" + entry.getKey() + "\", " + knownCues());
                continue;
            }

            String effectName = target.asString("");
            if (effectName.isBlank()) {
                if (target.isString()) target.error("effect name is empty");
                continue;
            }

            Effect effect = resolve(target, effectName);
            if (effect != null) cueEffects.put(entry.getKey(), effect);
        }

        return cueEffects;
    }

    private String knownCues() {
        if (this.cueNames.isEmpty()) return "the effect has no cues";
        return "the effects cues are: " + String.join(", ", new TreeSet<>(this.cueNames));
    }

    private @Nullable Effect resolve(Node target, String effectName) {
        try {
            return this.context.resolver().resolve(effectName);
        } catch (BlueprintException e) {
            for (BlueprintProblem inner : e.problems()) {
                String where = inner.path().isEmpty() ? "" : " (in \"" + effectName + "\" at " + inner.path() + ")";
                this.context.problems().add(new BlueprintProblem(
                        target.path(),
                        "can't use effect \"" + effectName + "\": " + inner.message() + where
                ));
            }

            return null;
        }
    }
}
