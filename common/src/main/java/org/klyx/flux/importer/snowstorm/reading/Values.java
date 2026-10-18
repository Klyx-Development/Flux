package org.klyx.flux.importer.snowstorm.reading;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.Scope;
import org.klyx.flux.expr.Expression;
import org.klyx.flux.expr.ExpressionException;
import org.klyx.flux.math.Vec3;

import java.util.List;

public final class Values {
    private final ReadingContext context;

    public Values(ReadingContext context) {
        this.context = context;
    }

    public static List<Expression> zeroVector() {
        return List.of(Expression.constant(0), Expression.constant(0), Expression.constant(0));
    }

    public Expression expression(@Nullable Node node, Scope scope, String fallbackSource) {
        if (node == null) return parseFixed(fallbackSource);

        if (node.isNumber()) return Expression.constant(node.asNumber(0));
        if (node.isString()) return parseText(node, scope, fallbackSource);
        if (node.isObject() && node.child("expression") != null) return expression(node.child("expression"), scope, fallbackSource);

        node.error("expected a number or an expression but got " + node.describe());
        return parseFixed(fallbackSource);
    }

    public Expression expression(@Nullable Node node, String fallbackSource) {
        return expression(node, this.context.scope(), fallbackSource);
    }

    public double constantNumber(Node node, Scope scope) {
        Expression expression = expression(node, scope, "0");
        if (!expression.isConstant()) {
            node.error("this must be a plain number here, not an expression that changes");
            return 0;
        }

        return expression.evalConstant();
    }

    public double nonNegative(Node parent, String key, double fallback) {
        Node child = parent.child(key);
        if (child == null) return fallback;

        double value = constantNumber(child, this.context.scope());
        if (!(value >= 0)) {
            child.error(key + " can't be negative but is " + value);
            return fallback;
        }

        return value;
    }

    public Vec3 constantVector(Node parent, String key, Vec3 fallback) {
        Node child = parent.child(key);
        if (child == null) return fallback;

        List<Node> items = threeItems(child);
        if (items == null) return fallback;

        Scope scope = this.context.scope();
        return new Vec3(constantNumber(items.get(0), scope), constantNumber(items.get(1), scope), constantNumber(items.get(2), scope));
    }

    public List<Expression> vector(@Nullable Node node, List<Expression> fallback) {
        if (node == null) return fallback;

        List<Node> items = threeItems(node);
        if (items == null) return fallback;

        return List.of(expression(items.get(0), "0"), expression(items.get(1), "0"), expression(items.get(2), "0"));
    }

    private @Nullable List<Node> threeItems(Node node) {
        if (!node.isArray()) {
            node.error("expected [x, y, z] but got " + node.describe());
            return null;
        }

        List<Node> items = node.items();
        if (items.size() != 3) {
            node.error("expected three values but got " + items.size());
            return null;
        }

        return items;
    }

    private Expression parseText(Node node, Scope scope, String fallbackSource) {
        String source = node.json().getAsString();

        Expression parsed;
        try {
            parsed = Expression.parse(source);
        } catch (ExpressionException e) {
            node.error("bad expression \"" + source + "\": " + e.getMessage());
            return parseFixed(fallbackSource);
        }

        boolean valid = true;
        for (String variable : parsed.variables()) {
            String problem = scope.problem(variable);
            if (problem != null) {
                node.error(problem);
                valid = false;
            }
        }

        return valid ? parsed : parseFixed(fallbackSource);
    }

    private static Expression parseFixed(String source) {
        try {
            return Expression.parse(source);
        } catch (ExpressionException e) {
            throw new IllegalStateException("bad built-in expression \"" + source + "\"", e);
        }
    }
}
