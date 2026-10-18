package org.klyx.flux.importer.snowstorm.emitter;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.expr.Expression;
import org.klyx.flux.importer.snowstorm.reading.Reader;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.sim.spec.EmitterSpec;

public final class LifetimeReader implements Reader<EmitterSpec.Lifetime> {

    @Override
    public EmitterSpec.Lifetime read(ReadingContext context) {
        Node looping = context.component("emitter_lifetime_looping");
        if (looping != null) {
            looping.onlyAllow("active_time", "sleep_time");
            return new EmitterSpec.Lifetime.Looping(
                    context.values().expression(looping.child("active_time"), "0"),
                    context.values().expression(looping.child("sleep_time"), "0"));
        }

        Node once = context.component("emitter_lifetime_once");
        if (once != null) {
            once.onlyAllow("active_time");
            return new EmitterSpec.Lifetime.Once(context.values().expression(once.child("active_time"), "10"));
        }

        Node expression = context.component("emitter_lifetime_expression");
        if (expression != null) {
            expression.onlyAllow("activation_expression", "expiration_expression");
            return fromExpressions(context, expression);
        }

        return new EmitterSpec.Lifetime.Once(Expression.constant(10));
    }

    private EmitterSpec.Lifetime fromExpressions(ReadingContext context, Node node) {
        Expression activation = context.values().expression(node.child("activation_expression"), "1");
        Expression expiration = context.values().expression(node.child("expiration_expression"), "0");

        boolean alwaysOn = activation.isConstant() && activation.evalConstant() != 0;
        boolean neverExpires = expiration.isConstant() && expiration.evalConstant() == 0;
        if (!alwaysOn || !neverExpires) {
            context.ignore(node, "activation and expiration expressions that change over time aren't supported");
        }

        return new EmitterSpec.Lifetime.Once(Expression.constant(60 * 60));
    }
}
