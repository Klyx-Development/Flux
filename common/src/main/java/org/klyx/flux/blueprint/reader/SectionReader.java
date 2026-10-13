package org.klyx.flux.blueprint.reader;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;

public abstract class SectionReader<T> {
    protected final CompileContext context;

    protected SectionReader(CompileContext context) {
        this.context = context;
    }

    public abstract T read(@Nullable Node node);
}
