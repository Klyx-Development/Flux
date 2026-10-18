package org.klyx.flux.importer.snowstorm.emitter;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.blueprint.Scope;
import org.klyx.flux.expr.Expression;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.importer.snowstorm.reading.Scopes;
import org.klyx.flux.importer.snowstorm.reading.Values;
import org.klyx.flux.sim.curve.Curve;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class CurveReader {
    private final Values values;

    public CurveReader(ReadingContext context) {
        this.values = context.values();
    }

    public Map<String, Curve> read(@Nullable Node node, Set<String> custom) {
        Map<String, Curve> result = new LinkedHashMap<>();
        if (node == null) return result;

        Set<String> names = new TreeSet<>();
        for (String key : node.entries().keySet()) {
            names.add(key.toLowerCase(Locale.ROOT));
        }
        Scope curveScope = Scopes.allowing(names, custom);

        for (Map.Entry<String, Node> entry : node.entries().entrySet()) {
            String name = entry.getKey().toLowerCase(Locale.ROOT);
            if (!name.startsWith("variable.")) {
                entry.getValue().error("a curve's name has to start with \"variable.\"");
                continue;
            }
            Curve curve = curve(entry.getValue(), curveScope);
            if (curve != null) result.put(name, curve);
        }

        return result;
    }

    private @Nullable Curve curve(Node node, Scope curveScope) {
        if (!node.isObject()) {
            node.error("expected a curve object but got " + node.describe());
            return null;
        }
        int before = node.problemCount();
        String type = node.requireString("type").toLowerCase(Locale.ROOT);
        Expression input = this.values.expression(node.child("input"), curveScope, "variable.particle_age / variable.particle_lifetime");
        Expression range = node.child("horizontal_range") == null
                ? Expression.constant(1)
                : this.values.expression(node.child("horizontal_range"), curveScope, "1");

        Node nodes = node.child("nodes");
        if (nodes == null) {
            node.error("missing \"nodes\"");
            return null;
        }
        Curve curve = null;
        try {
            curve = switch (type) {
                case "linear" -> new Curve.Linear(input, range, evenNodes(nodes, curveScope));
                case "catmull_rom" -> new Curve.CatmullRom(input, range, evenNodes(nodes, curveScope));
                case "bezier" -> new Curve.Bezier(input, range, evenNodes(nodes, curveScope));
                case "bezier_chain" -> chain(input, range, nodes, curveScope);
                default -> {
                    Node typeNode = node.child("type");
                    if (typeNode != null) typeNode.error("\"" + type + "\" isn't a valid curve type. valid types: linear, bezier, bezier_chain or catmull_rom");
                    yield null;
                }
            };
        } catch (IllegalArgumentException e) {
            nodes.error(e.getMessage());
        }
        return node.problemCount() > before ? null : curve;
    }

    private double[] evenNodes(Node nodes, Scope curveScope) {
        List<Node> items = nodes.items();
        double[] numbers = new double[items.size()];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = this.values.constantNumber(items.get(i), curveScope);
        }
        return numbers;
    }

    private Curve chain(Expression input, Expression range, Node nodes, Scope curveScope) {
        if (!nodes.isObject()) {
            nodes.error("a bezier chain's nodes must be an object, but this is " + nodes.describe());
            throw new IllegalArgumentException("bezier_chain nodes are malformed");
        }

        List<Curve.Chain.Key> keys = new ArrayList<>();
        for (Map.Entry<String, Node> entry : nodes.entries().entrySet()) {
            double time;
            try {
                time = Double.parseDouble(entry.getKey());
            } catch (NumberFormatException e) {
                entry.getValue().error("\"" + entry.getKey() + "\" isn't a number");
                continue;
            }

            Curve.Chain.Key key = chainKey(time, entry.getValue(), curveScope);
            if (key != null) keys.add(key);
        }

        return new Curve.Chain(input, range, keys);
    }

    private Curve.Chain.@Nullable Key chainKey(double time, Node key, Scope curveScope) {
        if (!key.isObject()) {
            key.error("expected a node object but got " + key.describe());
            return null;
        }

        double value = optionalNumber(key, "value", Double.NaN, curveScope);
        double left = optionalNumber(key, "left_value", value, curveScope);
        double right = optionalNumber(key, "right_value", value, curveScope);
        double slope = optionalNumber(key, "slope", 0, curveScope);
        double leftSlope = optionalNumber(key, "left_slope", slope, curveScope);
        double rightSlope = optionalNumber(key, "right_slope", slope, curveScope);

        if (Double.isNaN(left) || Double.isNaN(right)) {
            key.error("needs a \"value\", or both \"left_value\" and \"right_value\"");
            return null;
        }

        return new Curve.Chain.Key(time, left, right, leftSlope, rightSlope);
    }

    private double optionalNumber(Node parent, String key, double fallback, Scope curveScope) {
        Node child = parent.child(key);
        return child == null ? fallback : this.values.constantNumber(child, curveScope);
    }
}
