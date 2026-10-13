package org.klyx.flux.blueprint.support.named;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class NamedRegistry<F extends Named> {
    private final Map<String, F> byName = new LinkedHashMap<>();

    public NamedRegistry(List<? extends F> entries) {
        for (F entry : entries) {
            this.byName.put(entry.name(), entry);
        }
    }

    public String names() {
        return String.join(", ", this.byName.keySet());
    }

    public @Nullable F find(Node node) {
        Node typeNode = node.child("type");
        if (typeNode == null) {
            node.error("missing \"type\", use one of: " + names());
            return null;
        }

        if (!typeNode.isString()) {
            typeNode.error("expected one of: " + names() + " but got " + typeNode.describe() + " instead");
            return null;
        }

        String raw = typeNode.json().getAsString();
        F found = this.byName.get(raw.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_'));
        if (found == null) typeNode.error("\"" + raw + "\" isn't valid, use one of: " + names());

        return found;
    }
}
