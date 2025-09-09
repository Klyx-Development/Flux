package org.klyx.flux.effect.types;

import org.klyx.flux.effect.AbstractEffect;
import org.klyx.flux.effect.DefaultEffectConfiguration;
import org.klyx.flux.effects.EffectFormat;
import org.klyx.flux.utils.Vector3;

import java.util.Collection;

public class SphereEffect extends AbstractEffect {

    private final double radius;

    public SphereEffect(double radius) {
        super("sphere");
        this.radius = radius;
        this.configuration = new DefaultEffectConfiguration();
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public void generateOutline(Collection<Vector3> points) {
        double spacing = configuration != null ? configuration.getParticleSpacing() : 0.2;
        int segments =  Math.max(8, (int) (2 * Math.PI * radius / spacing));

        for (int i = 0; i < segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            points.add(new Vector3(radius * Math.cos(angle), 0, radius * Math.sin(angle)));
            points.add(new Vector3(radius * Math.cos(angle), radius * Math.sin(angle), 0));
        }
    }

    @Override
    public void generateSurface(Collection<Vector3> points) {
        double spacing = configuration != null ? configuration.getParticleSpacing() : 0.2;
        double step = spacing / radius;

        for (double phi = 0; phi < Math.PI; phi += step) {
            for (double theta = 0; theta < 2 * Math.PI; theta += step) {
                double x = radius * Math.sin(phi) * Math.cos(theta);
                double y = radius * Math.sin(phi) * Math.sin(theta);
                double z = radius * Math.cos(phi);
                points.add(new Vector3(x, y, z));
            }
        }
    }

    @Override
    public void generateFilled(Collection<Vector3> points) {
        double spacing = configuration != null ? configuration.getParticleSpacing() : 0.2;

        for (double x = -radius; x <= radius; x += spacing) {
            for (double y = -radius; y <= radius; y += spacing) {
                for (double z = -radius; z <= radius; z += spacing) {
                    if (x * x + y * y + z * z <= radius * radius) {
                        points.add(new Vector3(x, y, z));
                    }
                }
            }
        }
    }

    @Override
    public EffectFormat clone() {
        SphereEffect clonedEffect = new SphereEffect(radius);
        if (this.configuration != null) {
            clonedEffect.setConfiguration(this.configuration);
        }
        return clonedEffect;
    }
}
