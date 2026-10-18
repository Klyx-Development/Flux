package org.klyx.flux.importer.snowstorm;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.BlueprintException;
import org.klyx.flux.blueprint.BlueprintProblem;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.effect.AdvancedParticle;
import org.klyx.flux.importer.snowstorm.emitter.EmitterReader;
import org.klyx.flux.importer.snowstorm.particle.ParticleReader;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.sim.spec.EmitterSpec;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Reads bedrock particle files into emitters and particles
 */
public final class SnowstormImporter {
    private static final String PREFIX = "minecraft:";

    public static AdvancedParticle read(Reader reader) throws BlueprintException, IOException {
        JsonElement json;
        try {
            json = JsonParser.parseReader(reader);
        } catch (JsonParseException e) {
            throw invalidJson(e);
        }
        return read(json);
    }

    public static AdvancedParticle read(String text) throws BlueprintException {
        JsonElement json;
        try {
            json = JsonParser.parseString(text);
        } catch (JsonParseException e) {
            throw invalidJson(e);
        }

        return read(json);
    }

    public static AdvancedParticle read(JsonElement json) throws BlueprintException {
        List<BlueprintProblem> problems = new ArrayList<>();
        AdvancedParticle result = readFile(new Node(json, "", problems), problems);
        if (!problems.isEmpty() || result == null) {
            throw new BlueprintException(problems.isEmpty() ? List.of(new BlueprintProblem("", "the file couldn't be read")) : problems);
        }

        return result;
    }

    private static @Nullable AdvancedParticle readFile(Node root, List<BlueprintProblem> problems) {
        if (!root.isObject()) {
            root.error("a particle file must be an object but this is " + root.describe());
            return null;
        }

        Node effect = root.child("particle_effect");
        if (effect == null) {
            root.error("missing \"particle_effect\"?");
            return null;
        }

        Node components = effect.child("components");
        if (components == null) {
            effect.error("missing \"components\"");
            return null;
        }

        if (!components.isObject()) {
            components.error("expected an object of components but got " + components.describe());
            return null;
        }

        ReadingContext context = new ReadingContext(problems, withoutPrefix(components));

        Node events = effect.child("events");
        if (events != null) context.ignore(events, "events aren't supported");

        Supplier<EmitterSpec> emitter = new EmitterReader(effect.child("curves")).read(context);
        ParticleSpec particle = new ParticleReader().read(context);
        context.ignoreUnused();

        if (!problems.isEmpty()) return null;
        return new AdvancedParticle(identifier(effect), emitter.get(), particle, context.ignored());
    }

    private static Map<String, Node> withoutPrefix(Node components) {
        Map<String, Node> all = new LinkedHashMap<>();
        for (Map.Entry<String, Node> entry : components.entries().entrySet()) {
            String key = entry.getKey();
            all.put(key.startsWith(PREFIX) ? key.substring(PREFIX.length()) : key, entry.getValue());
        }

        return all;
    }

    private static String identifier(Node effect) {
        Node description = effect.child("description");
        if (description == null) {
            return AdvancedParticle.UNNAMED;
        }

        Node id = description.child("identifier");
        return id == null ? AdvancedParticle.UNNAMED : id.asString(AdvancedParticle.UNNAMED);
    }

    private static BlueprintException invalidJson(JsonParseException e) {
        return new BlueprintException(new BlueprintProblem("", "invalid JSON: " + rootMessage(e)));
    }

    private static String rootMessage(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) cause = cause.getCause();

        String message = String.valueOf(cause.getMessage());
        return message.lines().findFirst().orElse(message);
    }
}
