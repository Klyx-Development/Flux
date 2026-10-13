package org.klyx.flux.blueprint;

import org.klyx.flux.expr.Expression;
import org.klyx.flux.expr.ExpressionException;
import org.klyx.flux.sim.SimVariables;
import org.klyx.flux.timeline.Frame;

import java.util.Set;
import java.util.TreeSet;

public final class Dynamic {
    public static final Scope EMITTER = variable -> SimVariables.isKnown(variable) ? null
            : "unknown variable \"" + variable + "\", use one of: "
            + String.join(", ", new TreeSet<>(SimVariables.NAMES));

    private static final Set<String> QUERIES = Set.of("query.time", "query.progress", "query.duration");

    private final Expression expression;

    private Dynamic(Expression expression) {
        this.expression = expression;
    }

    public static Scope timeline(Set<String> tracks) {
        return variable -> {
            if (QUERIES.contains(variable)) return null;

            if (variable.startsWith("variable.")) {
                String track = variable.substring("variable.".length());
                if (tracks.contains(track)) return null;

                String known = tracks.isEmpty()
                        ? "this effect has no tracks to use"
                        : "tracks you can use: " + String.join(", ", new TreeSet<>(tracks));
                return "unknown variable \"" + variable + "\", " + known;
            }

            if (variable.startsWith("query.")) {
                return "unknown variable \"" + variable + "\", use one of: query.time, query.progress, query.duration";
            }

            return "\"" + variable + "\" isn't available here, use query.* or variable.<track>";
        };
    }

    public static Dynamic constant(double value) {
        return new Dynamic(Expression.constant(value));
    }

    public static Dynamic read(Node node, Set<String> tracks, double fallback) {
        return read(node, timeline(tracks), fallback);
    }

    public static Dynamic read(Node node, Scope scope, double fallback) {
        if (node.isNumber()) return constant(node.asNumber(fallback));

        if (!node.isString()) {
            node.error("expected a number or an expression but got " + node.describe() + " instead");
            return constant(fallback);
        }

        Expression expression;
        try {
            expression = Expression.parse(node.json().getAsString());
        } catch (ExpressionException e) {
            node.error("bad expression \"" + node.json().getAsString() + "\": " + e.getMessage());
            return constant(fallback);
        }

        boolean valid = true;
        for (String variable : expression.variables()) {
            String problem = scope.problem(variable);
            if (problem == null) continue;

            node.error(problem);
            valid = false;
        }

        return valid ? new Dynamic(expression) : constant(fallback);
    }

    public Expression expression() {
        return this.expression;
    }

    public boolean isConstant() {
        return this.expression.isConstant();
    }

    public double constantValue() {
        return this.expression.evalConstant();
    }

    public double at(Frame frame) {
        double value = this.expression.eval(frame.variables());

        // a NaN or infinity would spread to every point that it touches
        return Double.isFinite(value) ? value : 0;
    }
}
