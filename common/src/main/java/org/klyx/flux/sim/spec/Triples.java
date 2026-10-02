package org.klyx.flux.sim.spec;

import org.klyx.flux.expr.Expression;

import java.util.List;

public final class Triples {
    public static List<Expression> require(String name, List<Expression> list) {
        if (list.size() != 3) {
            throw new IllegalArgumentException(name + " needs 3 expressions but has " + list.size());
        }

        return List.copyOf(list);
    }
}
