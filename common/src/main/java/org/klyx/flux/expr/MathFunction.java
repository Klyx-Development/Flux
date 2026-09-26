package org.klyx.flux.expr;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum MathFunction {
    SIN(1, a -> Math.sin(Math.toRadians(a[0]))),
    COS(1, a -> Math.cos(Math.toRadians(a[0]))),
    TAN(1, a -> Math.tan(Math.toRadians(a[0]))),
    ASIN(1, a -> Math.toDegrees(Math.asin(a[0]))),
    ACOS(1, a -> Math.toDegrees(Math.acos(a[0]))),
    ATAN(1, a -> Math.toDegrees(Math.atan(a[0]))),
    ATAN2(2, a -> Math.toDegrees(Math.atan2(a[0], a[1]))),
    ABS(1, a -> Math.abs(a[0])),
    SQRT(1, a -> Math.sqrt(a[0])),
    FLOOR(1, a -> Math.floor(a[0])),
    CEIL(1, a -> Math.ceil(a[0])),
    ROUND(1, a -> Math.rint(a[0])),
    TRUNC(1, a -> a[0] < 0 ? Math.ceil(a[0]) : Math.floor(a[0])),
    SIGN(1, a -> Math.signum(a[0])),
    EXP(1, a -> Math.exp(a[0])),
    LN(1, a -> Math.log(a[0])),
    POW(2, a -> Math.pow(a[0], a[1])),
    MOD(2, a -> a[1] == 0 ? 0 : a[0] % a[1]),
    MIN(2, a -> Math.min(a[0], a[1])),
    MAX(2, a -> Math.max(a[0], a[1])),
    CLAMP(3, a -> Math.clamp(a[0], a[1], a[2])),
    LERP(3, a -> a[0] + (a[1] - a[0]) * a[2]),
    RANDOM(2, a -> a[0] + (a[1] - a[0]) * ThreadLocalRandom.current().nextDouble());

    private static final Map<String, MathFunction> BY_NAME = Stream.of(values())
            .collect(Collectors.toUnmodifiableMap(function -> function.name().toLowerCase(Locale.ROOT), function -> function));

    private final int arity;
    private final Impl impl;

    MathFunction(int arity, Impl impl) {
        this.arity = arity;
        this.impl = impl;
    }

    public int arity() {
        return this.arity;
    }

    public double apply(double[] arguments) {
        return this.impl.apply(arguments);
    }

    public static MathFunction byName(String name) {
        return BY_NAME.get(name);
    }

    public static String names() {
        return String.join(", ", BY_NAME.keySet().stream().sorted().toList());
    }

    @FunctionalInterface
    private interface Impl {
        double apply(double[] arguments);
    }
}
