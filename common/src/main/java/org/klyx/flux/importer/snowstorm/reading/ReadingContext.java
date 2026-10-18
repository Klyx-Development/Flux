package org.klyx.flux.importer.snowstorm.reading;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.BlueprintProblem;
import org.klyx.flux.blueprint.Dynamic;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class ReadingContext {

    private final List<BlueprintProblem> problems;
    private final Map<String, Node> components;
    private final List<BlueprintProblem> ignored = new ArrayList<>();
    private final Set<String> used = new TreeSet<>();
    private final Values values = new Values(this);
    private Scope scope = Dynamic.EMITTER;
    private int capacity = 256;

    public ReadingContext(List<BlueprintProblem> problems, Map<String, Node> components) {
        this.problems = problems;
        this.components = components;
    }

    public Values values() {
        return this.values;
    }

    public boolean hasProblems() {
        return !this.problems.isEmpty();
    }

    public List<BlueprintProblem> ignored() {
        return this.ignored;
    }

    public Scope scope() {
        return this.scope;
    }

    public void scope(Scope scope) {
        this.scope = scope;
    }

    public int capacity() {
        return this.capacity;
    }

    public void capacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean has(String name) {
        return this.components.containsKey(name);
    }

    public @Nullable Node component(String name) {
        Node node = this.components.get(name);
        if (node != null) this.used.add(name);
        return node;
    }

    public void ignore(Node at, String reason) {
        this.ignored.add(new BlueprintProblem(at.path(), reason));
    }

    public void ignoreUnused() {
        for (Map.Entry<String, Node> entry : this.components.entrySet()) {
            if (!this.used.contains(entry.getKey())) ignoreUnused(entry.getKey(), entry.getValue());
        }
    }

    private void ignoreUnused(String name, Node node) {
        boolean grouped = name.startsWith("emitter_rate_") || name.startsWith("emitter_lifetime_") || name.startsWith("emitter_shape_")
                || name.equals("particle_motion_dynamic") || name.equals("particle_motion_parametric");

        ignore(node, grouped
                ? "only one component of this kind is used, and another takes priority over \"" + name + "\""
                : "\"" + name + "\" is unused");
    }
}
