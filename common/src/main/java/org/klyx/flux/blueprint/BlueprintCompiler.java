package org.klyx.flux.blueprint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.klyx.flux.blueprint.reader.CompileContext;
import org.klyx.flux.blueprint.reader.EffectReader;
import org.klyx.flux.blueprint.reader.system.ParticleSystemReader;
import org.klyx.flux.effect.Effect;
import org.klyx.flux.effect.ParticleLike;
import org.klyx.flux.effect.AdvancedParticle;
import org.klyx.flux.importer.snowstorm.SnowstormImporter;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public final class BlueprintCompiler {

    public static Effect compile(Reader reader, EffectResolver resolver) throws BlueprintException, IOException {
        JsonElement json;
        try {
            json = JsonParser.parseReader(reader);
        } catch (JsonParseException e) {
            throw invalidJson(e);
        }

        return compile(json, resolver);
    }

    public static Effect compile(String text, EffectResolver resolver) throws BlueprintException {
        return compile(parse(text), resolver);
    }

    public static Effect compile(JsonElement json, EffectResolver resolver) throws BlueprintException {
        List<BlueprintProblem> problems = new ArrayList<>();

        Effect effect = new EffectReader(new CompileContext(problems, resolver)).read(new Node(json, "", problems));
        if (!problems.isEmpty() || effect == null) {
            throw new BlueprintException(
                    problems.isEmpty()
                            ? List.of(new BlueprintProblem("", "the blueprint couldn't be read"))
                            : problems
            );
        }

        return effect;
    }

    // a file is a bedrock/snowstorm particle if it has a particle_effect key
    // native emitters if it has an emitter key
    // effect otherwise
    public static ParticleLike compileAny(String text, EffectResolver resolver) throws BlueprintException {
        JsonElement json = parse(text);

        if (hasKey(json, "particle_effect")) return SnowstormImporter.read(json);
        if (hasKey(json, "emitter")) return compileSystem(json);

        return compile(json, resolver);
    }

    public static AdvancedParticle compileSystem(String text) throws BlueprintException {
        return compileSystem(parse(text));
    }

    public static AdvancedParticle compileSystem(JsonElement json) throws BlueprintException {
        List<BlueprintProblem> problems = new ArrayList<>();

        AdvancedParticle system = new ParticleSystemReader(problems).read(new Node(json, "", problems));
        if (!problems.isEmpty() || system == null) {
            throw new BlueprintException(
                    problems.isEmpty()
                            ? List.of(new BlueprintProblem("", "the particle couldn't be read"))
                            : problems
            );
        }

        return system;
    }

    private static JsonElement parse(String text) throws BlueprintException {
        try {
            return JsonParser.parseString(text);
        } catch (JsonParseException e) {
            throw invalidJson(e);
        }
    }

    private static boolean hasKey(JsonElement json, String key) {
        if (!json.isJsonObject()) return false;

        JsonObject object = json.getAsJsonObject();
        return object.has(key);
    }

    private static BlueprintException invalidJson(JsonParseException e) {
        return new BlueprintException(new BlueprintProblem("", "invalid JSON: " + rootMessage(e)));
    }

    private static String rootMessage(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        String message = String.valueOf(cause.getMessage());
        return message.lines().findFirst().orElse(message);
    }
}
