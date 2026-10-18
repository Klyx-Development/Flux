package org.klyx.flux.importer.snowstorm.emitter;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.Node;
import org.klyx.flux.importer.snowstorm.reading.Reader;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.importer.snowstorm.reading.Scopes;
import org.klyx.flux.sim.curve.Curve;
import org.klyx.flux.sim.spec.EmitterSpec;

import java.util.Map;
import java.util.function.Supplier;

public final class EmitterReader implements Reader<Supplier<EmitterSpec>> {
    private final @Nullable Node curvesNode;
    private final RateReader rates = new RateReader();
    private final LifetimeReader lifetimes = new LifetimeReader();
    private final EmitterShapeReader shapes = new EmitterShapeReader();

    public EmitterReader(@Nullable Node curvesNode) {
        this.curvesNode = curvesNode;
    }

    @Override
    public Supplier<EmitterSpec> read(ReadingContext context) {
        Initialization init = Initialization.read(context.component("emitter_initialization"), this.curvesNode);

        Map<String, Curve> curves = new CurveReader(context).read(this.curvesNode, init.names());
        context.scope(Scopes.allowing(curves.keySet(), init.names()));
        init.checkReads(context.scope());

        EmitterSpec.Rate rate = this.rates.read(context);
        int capacity = context.capacity();
        EmitterSpec.Lifetime lifetime = this.lifetimes.read(context);

        ShapeResult shape = this.shapes.read(context);
        return () -> new EmitterSpec(rate, lifetime, shape.shape(), shape.direction(), capacity, shape.offset(),
                curves, init.creation(), init.perUpdate());
    }
}
