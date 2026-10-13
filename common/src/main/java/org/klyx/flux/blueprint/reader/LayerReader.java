package org.klyx.flux.blueprint.reader;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.effect.Fit;
import org.klyx.flux.effect.Layer;
import org.klyx.flux.effect.ParticleBrush;
import org.klyx.flux.math.Transform;
import org.klyx.flux.modifier.PointModifier;
import org.klyx.flux.sample.SamplingStyle;
import org.klyx.flux.shader.PointShader;
import org.klyx.flux.shader.SolidShader;
import org.klyx.flux.shape.Shape;
import org.klyx.flux.timeline.Frame;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class LayerReader extends SectionReader<List<Layer>> {
    private final Node root;
    private final Set<String> readable;

    public LayerReader(CompileContext context, Node root, Set<String> readable) {
        super(context);
        this.root = root;
        this.readable = readable;
    }

    @Override
    public List<Layer> read(@Nullable Node node) {
        List<Layer> layers = new ArrayList<>();
        if (node == null) {
            this.root.error("missing \"layers\", an effect needs at least one");
            return layers;
        }

        for (Node item : node.items()) {
            Layer layer = layer(item);
            if (layer != null) layers.add(layer);
        }

        if (node.isArray() && node.items().isEmpty()) node.error("an effect needs at least one layer");

        return layers;
    }

    private @Nullable Layer layer(Node node) {
        if (!node.isObject()) {
            node.error("expected a layer object but got " + node.describe());
            return null;
        }

        node.onlyAllow("shape", "style", "density", "transform", "modifiers", "shader", "particle", "interval", "fit", "margin");
        int before = this.context.problems().size();

        Shape shape = shape(node);
        SamplingStyle style = node.enumValue("style", SamplingStyle.class, SamplingStyle.OUTLINE);
        double density = density(node);
        double interval = interval(node);
        Fit fit = node.enumValue("fit", Fit.class, Fit.NONE);
        double margin = margin(node);

        Node transformNode = node.child("transform");
        Transform transform = transformNode == null ? Transform.IDENTITY : TransformReader.read(transformNode);

        Node modifiersNode = node.child("modifiers");
        Function<Frame, PointModifier> modifier =
                modifiersNode == null ? null : StyleReader.modifiers(modifiersNode, this.readable);

        Node shaderNode = node.child("shader");
        Function<Frame, PointShader> shader =
                shaderNode == null ? null : StyleReader.shader(shaderNode, this.readable);

        Node particleNode = node.child("particle");
        ParticleBrush particle = particleNode == null ? null : ParticleBrushReader.read(particleNode);

        if (this.context.problems().size() > before || shape == null) return null;
        if (!sampleOnce(node, shape, style, density)) return null;

        Layer.Builder layer = Layer.of(shape).style(style).density(density).transform(transform).interval(interval).fit(fit).margin(margin);
        if (modifier != null) layer.animatedModifier(modifier);
        if (particle != null) layer.particle(particle);

        layer.animatedShader(shader != null ? shader : _ -> new SolidShader(Layer.DEFAULT_COLOR));

        return layer.build();
    }

    private @Nullable Shape shape(Node layerNode) {
        Node shapeNode = layerNode.child("shape");
        if (shapeNode == null) {
            layerNode.error("missing \"shape\"");
            return null;
        }

        return ShapeReader.read(shapeNode, this.context.problems());
    }

    private double density(Node layerNode) {
        double density = layerNode.number("density", 4);
        if (!(density > 0)) {
            Node densityNode = layerNode.child("density");
            if (densityNode != null) densityNode.error("density must be more than 0 but got " + density);
        }

        return density;
    }

    private double interval(Node layerNode) {
        double interval = layerNode.number("interval", 0);
        if (!(interval >= 0)) {
            Node intervalNode = layerNode.child("interval");
            if (intervalNode != null) intervalNode.error("interval must be 0 or more but got " + interval);
        }

        return interval;
    }

    private double margin(Node layerNode) {
        double margin = layerNode.number("margin", 0);
        if (!(margin >= 0)) {
            Node marginNode = layerNode.child("margin");
            if (marginNode != null) marginNode.error("margin must be 0 or more but got " + margin);
        }

        return margin;
    }

    // a shape too big for its density should just fail to load
    private boolean sampleOnce(Node node, Shape shape, SamplingStyle style, double density) {
        try {
            shape.sample(style, density);
            return true;
        } catch (IllegalArgumentException | IllegalStateException e) {
            node.error(e.getMessage());
            return false;
        }
    }
}
