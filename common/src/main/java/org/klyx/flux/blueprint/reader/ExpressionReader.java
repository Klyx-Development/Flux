package org.klyx.flux.blueprint.reader;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Dynamic;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.expr.Expression;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class ExpressionReader extends SectionReader<Map<String, Expression>> {
    private final Set<String> trackNames;

    public ExpressionReader(CompileContext context, Set<String> trackNames) {
        super(context);
        this.trackNames = trackNames;
    }

    @Override
    public Map<String, Expression> read(@Nullable Node node) {
        Map<String, Expression> expressions = new LinkedHashMap<>();
        if (node == null) return expressions;

        for (Map.Entry<String, Node> entry : node.entries().entrySet()) {
            if (this.trackNames.contains(entry.getKey())) {
                entry.getValue().error("\"" + entry.getKey() + "\" is already a keyframe track, can't also be an expression");
                continue;
            }

            expressions.put(entry.getKey(), Dynamic.read(entry.getValue(), this.trackNames, 0).expression());
        }

        return expressions;
    }
}
