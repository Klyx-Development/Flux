package org.klyx.flux.importer.snowstorm.emitter;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.Scope;
import org.klyx.flux.expr.ExpressionException;
import org.klyx.flux.expr.Script;
import org.klyx.flux.sim.SimVariables;

import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public record Initialization(@Nullable Node creationNode, Script creation, @Nullable Node updateNode, Script perUpdate, Set<String> names) {
    public static final Initialization NONE = new Initialization(null, Script.EMPTY, null, Script.EMPTY, Set.of());

    public static Initialization read(@Nullable Node node, @Nullable Node curvesNode) {
        if (node == null) return NONE;
        node.onlyAllow("creation_expression", "per_update_expression");

        Node creationNode = node.child("creation_expression");
        Node updateNode = node.child("per_update_expression");
        Script creation = script(creationNode);
        Script perUpdate = script(updateNode);

        Set<String> curveKeys = new TreeSet<>();
        if (curvesNode != null && curvesNode.isObject()) {
            for (String key : curvesNode.entries().keySet()) {
                curveKeys.add(key.toLowerCase(Locale.ROOT));
            }
        }

        checkTargets(creationNode, creation, curveKeys);
        checkTargets(updateNode, perUpdate, curveKeys);

        Set<String> names = new TreeSet<>(creation.assignedNames());
        names.addAll(perUpdate.assignedNames());

        return new Initialization(creationNode, creation, updateNode, perUpdate, names);
    }

    public void checkReads(Scope scope) {
        checkReads(this.creationNode, this.creation, scope);
        checkReads(this.updateNode, this.perUpdate, scope);
    }

    private static void checkReads(@Nullable Node node, Script script, Scope scope) {
        if (node == null) return;

        Set<String> reported = new TreeSet<>();
        for (Script.Assignment assignment : script.assignments()) {
            for (String variable : assignment.value().variables()) {
                String problem = scope.problem(variable);
                if (problem != null && reported.add(problem)) node.error(problem);
            }
        }
    }

    private static Script script(@Nullable Node node) {
        if (node == null) return Script.EMPTY;

        if (!node.isString()) {
            node.error("expected text like \"variable.size = 0.1\" but got " + node.describe());
            return Script.EMPTY;
        }

        try {
            return Script.parse(node.json().getAsString());
        } catch (ExpressionException e) {
            node.error(e.getMessage());
            return Script.EMPTY;
        }
    }

    private static void checkTargets(@Nullable Node node, Script script, Set<String> curveKeys) {
        if (node == null) return;

        for (String name : script.assignedNames()) {
            if (SimVariables.isKnown(name)) {
                node.error("\"" + name + "\" is built in and can't be assigned to");
            } else if (curveKeys.contains(name)) {
                node.error("\"" + name + "\" is already the name of a curve");
            }
        }
    }
}
