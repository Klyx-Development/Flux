package org.klyx.flux.shader;

import org.klyx.flux.math.Easing;

import java.util.Comparator;
import java.util.List;

public record Gradient(List<ColorStop> stops, Easing easing) {
    public Gradient {
        if (stops.isEmpty()) throw new IllegalArgumentException("gradient needs at least one stop");

        stops = stops.stream().sorted(Comparator.comparingDouble(ColorStop::position)).toList();
    }

    public static Gradient of(int... rgb) {
        if (rgb.length == 0) throw new IllegalArgumentException("gradient needs at least one colour");

        ColorStop[] stops = new ColorStop[rgb.length];
        for (int i = 0; i < rgb.length; i++) {
            stops[i] = new ColorStop(rgb.length == 1 ? 0 : (double) i / (rgb.length - 1), rgb[i]);
        }

        return new Gradient(List.of(stops), Easing.LINEAR);
    }

    public int at(double progress) {
        progress = easing.apply(progress);

        ColorStop first = stops.getFirst();
        if (progress <= first.position()) return first.rgb();

        ColorStop last = stops.getLast();
        if (progress >= last.position()) return last.rgb();

        for (int i = 1; i < stops.size(); i++) {
            ColorStop to = stops.get(i);
            if (progress > to.position()) continue;

            ColorStop from = stops.get(i - 1);
            double span = to.position() - from.position();
            return blend(from.rgb(), to.rgb(), span <= 0 ? 1 : (progress - from.position()) / span);
        }

        return last.rgb();
    }

    private static int blend(int from, int to, double factor) {
        int red = channel(from >> 16, to >> 16, factor);
        int green = channel(from >> 8, to >> 8, factor);
        int blue = channel(from, to, factor);

        return red << 16 | green << 8 | blue;
    }

    private static int channel(int from, int to, double factor) {
        from &= 0xFF;
        to &= 0xFF;

        return (int) Math.round(from + (to - from) * factor);
    }

    public record ColorStop(double position, int rgb) {
        public ColorStop {
            if (!(position >= 0 && position <= 1)) {
                throw new IllegalArgumentException("position must be between 0 and 1: " + position);
            }
        }
    }
}
