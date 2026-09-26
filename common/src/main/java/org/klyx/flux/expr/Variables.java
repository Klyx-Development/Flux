package org.klyx.flux.expr;

@FunctionalInterface
public interface Variables {
    Variables NONE = _ -> 0;

    double get(String name);
}
