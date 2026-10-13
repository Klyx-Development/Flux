package org.klyx.flux.blueprint.reader.system;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.BlueprintProblem;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.effect.AdvancedParticle;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.sim.spec.EmitterSpec;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.util.List;
import java.util.Map;

public final class ParticleSystemReader {
    private final List<BlueprintProblem> problems;

    public ParticleSystemReader(List<BlueprintProblem> problems) {
        this.problems = problems;
    }

    public @Nullable AdvancedParticle read(Node root) {
        if (!root.isObject()) {
            root.error("a particle system must be an object but this is " + root.describe());
            return null;
        }

        root.onlyAllow("description", "emitter", "particle");

        ReadingContext context = new ReadingContext(this.problems, Map.of());
        Node emitterNode = root.child("emitter");
        if (emitterNode == null) {
            root.error("missing \"emitter\"");
            return null;
        }

        EmitterSpec emitter = new EmitterSpecReader(context).read(emitterNode);
        ParticleSpec particle = new ParticleSpecReader(context).read(root.child("particle"));

        if (!this.problems.isEmpty() || emitter == null || particle == null) return null;
        return new AdvancedParticle(emitter, particle);
    }
}
