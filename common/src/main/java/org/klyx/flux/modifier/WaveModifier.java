package org.klyx.flux.modifier;

import org.klyx.flux.math.Axis;
import org.klyx.flux.sample.PointCloud;

public record WaveModifier(Axis displace, Axis along, double amplitude, double wavelength, double phase)
        implements PointModifier {
    public WaveModifier {
        if (displace == along) throw new IllegalArgumentException("displace and along must be different axes");

        if (!(wavelength > 0) || !Double.isFinite(wavelength)) {
            throw new IllegalArgumentException("wavelength must be positive and finite: " + wavelength);
        }
    }

    @Override
    public PointCloud apply(PointCloud cloud) {
        int to = displace.index();
        int from = along.index();

        return cloud.map(point -> point[to] += amplitude * Math.sin(2 * Math.PI * point[from] / wavelength + phase));
    }
}
