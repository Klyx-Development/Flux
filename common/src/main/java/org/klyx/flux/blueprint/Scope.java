package org.klyx.flux.blueprint;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface Scope {
    @Nullable String problem(String variable);
}
