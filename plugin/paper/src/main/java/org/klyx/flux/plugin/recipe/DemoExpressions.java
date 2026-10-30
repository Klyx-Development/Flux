package org.klyx.flux.plugin.recipe;

import org.klyx.flux.expr.Expression;
import org.klyx.flux.expr.ExpressionException;

public final class DemoExpressions {
    public static Expression expr(String source) {
        try {
            return Expression.parse(source);
        } catch (ExpressionException e) {
            throw new IllegalStateException("Bad demo expression \"" + source + "\"", e);
        }
    }
}
