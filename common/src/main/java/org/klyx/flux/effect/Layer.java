package org.klyx.flux.effect;

import org.klyx.flux.math.Transform;
import org.klyx.flux.modifier.PointModifier;
import org.klyx.flux.sample.SamplingStyle;
import org.klyx.flux.shader.PointShader;
import org.klyx.flux.shader.SolidShader;
import org.klyx.flux.shape.Shape;
import org.klyx.flux.timeline.Frame;

import java.util.function.Function;

public record Layer(
        Shape shape, SamplingStyle style, double density,
        Function<Frame, PointModifier> modifier, Function<Frame, PointShader> shader,
        Transform transform, ParticleBrush particle, double interval, Fit fit, double margin
) {

    public static final int DEFAULT_COLOR = 0xFFFFFF;

    public Layer {
        if (!(density > 0) || !Double.isFinite(density)) {
            throw new IllegalArgumentException("density must be positive and finite: " + density);
        }

        if (!(interval >= 0) || !Double.isFinite(interval)) {
            throw new IllegalArgumentException("interval has to be greater than or equal to 0 and finite: " + interval);
        }

        if (!(margin >= 0) || !Double.isFinite(margin)) {
            throw new IllegalArgumentException("margin has to be greater than or equal to 0 and finite: " + margin);
        }
    }

    public static Builder of(Shape shape) {
        return new Builder(shape);
    }

    public static final class Builder {
        private final Shape shape;
        private SamplingStyle style = SamplingStyle.OUTLINE;
        private double density = 4;
        private Function<Frame, PointModifier> modifier = _ -> cloud -> cloud;
        private Function<Frame, PointShader> shader = _ -> new SolidShader(DEFAULT_COLOR);
        private Transform transform = Transform.IDENTITY;
        private ParticleBrush particle = ParticleBrush.dust(ParticleBrush.DEFAULT_SIZE);
        private double interval;
        private Fit fit = Fit.NONE;
        private double margin;

        private Builder(Shape shape) {
            this.shape = shape;
        }

        public Builder style(SamplingStyle style) {
            this.style = style;
            return this;
        }

        public Builder density(double density) {
            this.density = density;
            return this;
        }

        public Builder modifier(PointModifier modifier) {
            this.modifier = _ -> modifier;
            return this;
        }

        public Builder animatedModifier(Function<Frame, PointModifier> modifier) {
            this.modifier = modifier;
            return this;
        }

        public Builder shader(PointShader shader) {
            this.shader = _ -> shader;
            return this;
        }

        public Builder animatedShader(Function<Frame, PointShader> shader) {
            this.shader = shader;
            return this;
        }

        public Builder transform(Transform transform) {
            this.transform = transform;
            return this;
        }

        public Builder particle(ParticleBrush particle) {
            this.particle = particle;
            return this;
        }

        public Builder interval(double interval) {
            this.interval = interval;
            return this;
        }

        public Builder fit(Fit fit) {
            this.fit = fit;
            return this;
        }

        public Builder margin(double margin) {
            this.margin = margin;
            return this;
        }

        public Layer build() {
            return new Layer(shape, style, density, modifier, shader, transform, particle, interval, fit, margin);
        }
    }
}
