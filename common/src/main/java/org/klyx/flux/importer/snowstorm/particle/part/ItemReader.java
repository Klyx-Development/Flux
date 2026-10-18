package org.klyx.flux.importer.snowstorm.particle.part;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.sim.spec.DisplayKind;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.util.Locale;
import java.util.regex.Pattern;

public final class ItemReader implements ParticlePart {
    private static final String PREFIX = "minecraft:";
    private static final Pattern ITEM_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    @Override
    public void apply(ReadingContext context, ParticleSpec.Builder particle) {
        Node node = context.component("flux:particle_appearance_item");
        if (node == null) return;

        node.onlyAllow("item", "model", "tint");

        Node itemNode = node.child("item");
        if (itemNode == null) {
            node.error("missing \"item\", use an id like \"minecraft:snowball\"");
            return;
        }

        String item = itemId(itemNode);
        Node modelNode = node.child("model");
        String model = modelNode == null ? null : itemId(modelNode);

        boolean tint = node.bool("tint", true);
        if (item == null || (modelNode != null && model == null)) return;

        particle.kind(DisplayKind.ITEM).item(item).model(model).tint(tint);
    }

    private @Nullable String itemId(Node node) {
        String text = node.asString("").trim().toLowerCase(Locale.ROOT);
        if (text.isEmpty()) {
            if (node.isString()) node.error("is empty, use an id like \"minecraft:egg\"");
            return null;
        }

        String id = text.contains(":") ? text : PREFIX + text;
        if (!ITEM_ID.matcher(id).matches()) {
            node.error("\"" + text + "\" isn't an id. use an id like \"minecraft:egg\" (namespace:name)");
            return null;
        }

        return id;
    }
}
