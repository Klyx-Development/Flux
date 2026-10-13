package org.klyx.flux.blueprint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.support.KeyCheck;
import org.klyx.flux.blueprint.support.NodeValues;
import org.klyx.flux.math.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Node {
    private final JsonElement json;
    private final String path;
    private final List<BlueprintProblem> problems;

    public Node(JsonElement json, String path, List<BlueprintProblem> problems) {
        this.json = json;
        this.path = path;
        this.problems = problems;
    }

    public static String childPath(String parent, String key) {
        return parent.isEmpty() ? key : parent + "." + key;
    }

    public static <E extends Enum<E>> String names(Class<E> type) {
        return String.join(", ", Arrays.stream(type.getEnumConstants())
                .map(constant -> constant.name().toLowerCase(Locale.ROOT))
                .toList());
    }

    public String path() {
        return this.path;
    }

    public JsonElement json() {
        return this.json;
    }

    public void error(String message) {
        this.problems.add(new BlueprintProblem(this.path, message));
    }

    public int problemCount() {
        return this.problems.size();
    }

    public boolean isObject() {
        return this.json.isJsonObject();
    }

    public boolean isArray() {
        return this.json.isJsonArray();
    }

    public boolean isString() {
        return this.json.isJsonPrimitive() && this.json.getAsJsonPrimitive().isString();
    }

    public boolean isNumber() {
        return this.json.isJsonPrimitive() && this.json.getAsJsonPrimitive().isNumber();
    }

    public @Nullable Node child(String key) {
        if (!this.json.isJsonObject()) return null;

        JsonElement found = this.json.getAsJsonObject().get(key);
        if (found == null || found.isJsonNull()) return null;

        return new Node(found, childPath(this.path, key), this.problems);
    }

    public List<Node> items() {
        if (!this.json.isJsonArray()) {
            error("expected a list");
            return List.of();
        }

        JsonArray array = this.json.getAsJsonArray();
        List<Node> items = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            items.add(new Node(array.get(i), this.path + "[" + i + "]", this.problems));
        }

        return items;
    }

    public Map<String, Node> entries() {
        if (!this.json.isJsonObject()) {
            error("expected an object");
            return Map.of();
        }

        Map<String, Node> entries = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : this.json.getAsJsonObject().entrySet()) {
            entries.put(entry.getKey(), new Node(entry.getValue(), childPath(this.path, entry.getKey()), this.problems));
        }

        return entries;
    }

    public void onlyAllow(String... allowed) {
        if (!this.json.isJsonObject()) {
            error("expected an object");
            return;
        }

        this.problems.addAll(KeyCheck.unknownKeys(this.json.getAsJsonObject(), this.path, allowed));
    }

    public String string(String key, String fallback) {
        Node node = child(key);
        return node == null ? fallback : node.asString(fallback);
    }

    public String requireString(String key) {
        Node node = child(key);
        if (node == null) {
            error("missing \"" + key + "\"");
            return "";
        }

        return node.asString("");
    }

    public double number(String key, double fallback) {
        Node node = child(key);
        return node == null ? fallback : node.asNumber(fallback);
    }

    public double requireNumber(String key) {
        Node node = child(key);
        if (node == null) {
            error("missing \"" + key + "\"");
            return Double.NaN;
        }

        return node.asNumber(Double.NaN);
    }

    public int requireInt(String key) {
        double value = requireNumber(key);
        if (Double.isNaN(value)) return 0;

        if (value != Math.rint(value) || Math.abs(value) > Integer.MAX_VALUE) {
            Node node = child(key);
            if (node != null) node.error("expected a whole number but got " + value);
            return 0;
        }

        return (int) value;
    }

    public boolean bool(String key, boolean fallback) {
        Node node = child(key);
        return node == null ? fallback : node.asBool(fallback);
    }

    public <E extends Enum<E>> E enumValue(String key, Class<E> type, E fallback) {
        Node node = child(key);
        return node == null ? fallback : node.asEnum(type, fallback);
    }

    public <E extends Enum<E>> E requireEnum(String key, Class<E> type) {
        Node node = child(key);
        if (node == null) {
            error("missing \"" + key + "\", use one of: " + names(type));
            return type.getEnumConstants()[0];
        }

        return node.asEnum(type, type.getEnumConstants()[0]);
    }

    public Vec3 vec3(String key, Vec3 fallback, boolean allowScalar) {
        Node node = child(key);
        return node == null ? fallback : node.asVec3(fallback, allowScalar);
    }

    public Vec3 requireVec3(String key, boolean allowScalar) {
        Node node = child(key);
        if (node == null) {
            error("missing \"" + key + "\"");
            return Vec3.ZERO;
        }

        return node.asVec3(Vec3.ZERO, allowScalar);
    }

    public String asString(String fallback) {
        return NodeValues.string(this, fallback);
    }

    public boolean asBool(boolean fallback) {
        return NodeValues.bool(this, fallback);
    }

    public double asNumber(double fallback) {
        return NodeValues.number(this, fallback);
    }

    public <E extends Enum<E>> E asEnum(Class<E> type, E fallback) {
        return NodeValues.enumValue(this, type, fallback);
    }

    public Vec3 asVec3(Vec3 fallback, boolean allowScalar) {
        return NodeValues.vec3(this, fallback, allowScalar);
    }

    public String describe() {
        if (this.json.isJsonObject()) return "an object";
        if (this.json.isJsonArray()) return "a list";
        if (this.json.isJsonNull()) return "null";

        JsonPrimitive primitive = this.json.getAsJsonPrimitive();
        if (primitive.isBoolean()) return "a boolean";

        return primitive.isString() ? "text \"" + primitive.getAsString() + "\"" : "a number";
    }

    public JsonObject asObjectOrEmpty() {
        return this.json.isJsonObject() ? this.json.getAsJsonObject() : new JsonObject();
    }
}
