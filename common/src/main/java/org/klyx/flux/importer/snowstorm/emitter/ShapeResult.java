package org.klyx.flux.importer.snowstorm.emitter;

import org.klyx.flux.expr.Expression;
import org.klyx.flux.importer.snowstorm.reading.Values;
import org.klyx.flux.sim.emission.Direction;
import org.klyx.flux.sim.emission.EmitterShape;

import java.util.List;

public record ShapeResult(EmitterShape shape, Direction direction, List<Expression> offset) {
    public static ShapeResult point() {
        return new ShapeResult(new EmitterShape.Point(), new Direction.Outwards(), Values.zeroVector());
    }
}
