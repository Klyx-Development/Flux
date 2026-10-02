package org.klyx.flux.sim.curve;

import org.klyx.flux.expr.Expression;

import java.util.Comparator;
import java.util.List;

public sealed interface Curve permits Curve.Bezier, Curve.CatmullRom, Curve.Chain, Curve.Linear {
    Expression input();

    Expression horizontalRange();

    double valueAt(double inputValue, double range);

    private static double position(double inputValue, double range) {
        if (!(range > 1.0e-9) || !Double.isFinite(inputValue)) return 0;

        return Math.clamp(inputValue / range, 0, 1);
    }

    record Linear(Expression input, Expression horizontalRange, double[] nodes) implements Curve {
        public Linear {
            if (nodes.length < 2) {
                throw new IllegalArgumentException("a linear curve needs at least 2 nodes but has " + nodes.length + " instead");
            }

            nodes = nodes.clone();
        }

        @Override
        public double valueAt(double inputValue, double range) {
            double at = Curve.position(inputValue, range) * (this.nodes.length - 1);
            int index = Math.min((int) at, this.nodes.length - 2);
            double fraction = at - index;

            return this.nodes[index] + (this.nodes[index + 1] - this.nodes[index]) * fraction;
        }
    }

    record CatmullRom(Expression input, Expression horizontalRange, double[] nodes) implements Curve {
        public CatmullRom {
            if (nodes.length < 4) {
                throw new IllegalArgumentException("a catmull rom curve needs at least 4 nodes but has " + nodes.length + " instead");
            }

            nodes = nodes.clone();
        }

        @Override
        public double valueAt(double inputValue, double range) {
            int segments = this.nodes.length - 3;
            double at = Curve.position(inputValue, range) * segments;
            int index = Math.min((int) at, segments - 1);

            double f = at - index;
            double p0 = this.nodes[index];
            double p1 = this.nodes[index + 1];
            double p2 = this.nodes[index + 2];
            double p3 = this.nodes[index + 3];

            return 0.5 * ((2 * p1)
                    + (-p0 + p2) * f
                    + (2 * p0 - 5 * p1 + 4 * p2 - p3) * f * f
                    + (-p0 + 3 * p1 - 3 * p2 + p3) * f * f * f);
        }
    }

    record Bezier(Expression input, Expression horizontalRange, double[] nodes) implements Curve {
        public Bezier {
            if (nodes.length != 4) {
                throw new IllegalArgumentException("a bezier curve needs 4 nodes but has " + nodes.length);
            }

            nodes = nodes.clone();
        }

        @Override
        public double valueAt(double inputValue, double range) {
            double t = Curve.position(inputValue, range);
            double u = 1 - t;

            return u * u * u * this.nodes[0] + 3 * u * u * t * this.nodes[1]
                    + 3 * u * t * t * this.nodes[2] + t * t * t * this.nodes[3];
        }
    }

    record Chain(Expression input, Expression horizontalRange, List<Key> keys) implements Curve {
        public record Key(double time, double leftValue, double rightValue, double leftSlope, double rightSlope) { }

        public Chain {
            if (keys.size() < 2) {
                throw new IllegalArgumentException("a bezier chain curve needs at least 2 nodes but has " + keys.size() + " instead");
            }

            keys = keys.stream().sorted(Comparator.comparingDouble(Key::time)).toList();
        }

        @Override
        public double valueAt(double inputValue, double range) {
            if (!Double.isFinite(inputValue)) return this.keys.getFirst().rightValue();

            Key first = this.keys.getFirst();

            if (inputValue <= first.time()) return first.rightValue();

            Key last = this.keys.getLast();

            if (inputValue >= last.time()) return last.leftValue();

            for (int i = 1; i < this.keys.size(); i++) {
                Key end = this.keys.get(i);

                if (inputValue > end.time()) continue;

                Key start = this.keys.get(i - 1);
                double span = end.time() - start.time();

                if (span <= 0) return end.leftValue();

                double s = (inputValue - start.time()) / span;
                double h00 = 2 * s * s * s - 3 * s * s + 1;
                double h10 = s * s * s - 2 * s * s + s;
                double h01 = -2 * s * s * s + 3 * s * s;
                double h11 = s * s * s - s * s;

                return h00 * start.rightValue() + h10 * span * start.rightSlope()
                        + h01 * end.leftValue() + h11 * span * end.leftSlope();
            }

            return last.leftValue();
        }
    }
}
