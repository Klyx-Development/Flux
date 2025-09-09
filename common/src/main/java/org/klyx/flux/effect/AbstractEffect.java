package org.klyx.flux.effect;

import org.klyx.flux.utils.Vector3;
import org.klyx.flux.effects.DisplayMode;
import org.klyx.flux.effects.EffectConfiguration;
import org.klyx.flux.effects.EffectFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AbstractEffect implements EffectFormat {

    protected final UUID shapeId;
    protected final String shapeType;

    private Collection<Vector3> cachedPoints;
    private DisplayMode lastMode;
    private Vector3 lastRotation = new Vector3();
    private double lastScale = 1.0;
    private Vector3 lastOffset = new Vector3();
    private double lastSpacing = 0.2;
    private boolean dirty = true;

    protected EffectConfiguration configuration;

    public AbstractEffect(String shapeType) {
        this.shapeId = UUID.randomUUID();
        this.shapeType = shapeType;
    }

    private boolean needsRecalculation() {
        if (dirty || cachedPoints == null || configuration == null) return true;

        return !Objects.equals(lastMode, configuration.getDisplayMode()) ||
                !Objects.equals(lastRotation, configuration.getRotation()) ||
                lastScale != configuration.getScale() ||
                !Objects.equals(lastOffset, configuration.getOffset()) ||
                lastSpacing != configuration.getParticleSpacing();
    }

    private void recalculatePoints() {
        if (configuration == null) {
            cachedPoints = Collections.emptyList();
            return;
        }

        Collection<Vector3> points = new ArrayList<>();
        generatePoints(configuration.getDisplayMode(), points);

        cachedPoints = transformPoints(points);

        lastMode = configuration.getDisplayMode();
        lastRotation = configuration.getRotation().clone();
        lastScale = configuration.getScale();
        lastOffset = configuration.getOffset().clone();
        lastSpacing = configuration.getParticleSpacing();
        dirty = false;
    }

    private Collection<Vector3> transformPoints(Collection<Vector3> points) {
        List<Vector3> transformedPoints = new ArrayList<>();

        double scale = configuration.getScale();
        Vector3 rotation = configuration.getRotation();
        Vector3 offset = configuration.getOffset();

        for (Vector3 point : points) {
            Vector3 transformedPoint = point.clone();

            transformedPoint = transformedPoint.multiply(scale);

            if (rotation.lengthSquared() > 0) {
                transformedPoint = rotateVector(transformedPoint, rotation);
            }

            transformedPoint = transformedPoint.add(offset);

            transformedPoints.add(transformedPoint);
        }

        return limitCount(transformedPoints);
    }

    private Vector3 rotateVector(Vector3 vector, Vector3 rotation) {
        Vector3 result = vector.clone();

        if (rotation.x() != 0) {
            double cos = Math.cos(rotation.x());
            double sin = Math.sin(rotation.x());

            double y = result.y() * cos - result.z() * sin;
            double z = result.y() * sin + result.z() * cos;

            result.setY(y).setZ(z);
        }

        if (rotation.y() != 0) {
            double cos = Math.cos(rotation.y());
            double sin = Math.sin(rotation.y());

            double x = result.x() * cos - result.z() * sin;
            double z = result.x() * sin + result.z() * cos;

            result.setX(x).setZ(z);
        }

        if (rotation.z() != 0) {
            double cos = Math.cos(rotation.z());
            double sin = Math.sin(rotation.z());

            double x = result.x() * cos - result.y() * sin;
            double y = result.x() * sin + result.y() * cos;

            result.setX(x).setY(y);
        }

        return result;
    }

    private Collection<Vector3> limitCount(Collection<Vector3> points) {
        int maxCount = configuration.getMaxParticleCount();
        if (points.size() <= maxCount) {
            return points;
        }

        List<Vector3> limited = new ArrayList<>(maxCount);
        List<Vector3> pointsList = new ArrayList<>(points);

        double step = (double) pointsList.size() / maxCount;
        for (int i = 0; i < maxCount; i++) {
            int index = (int) (i * step);
            limited.add(pointsList.get(index));
        }

        return limited;
    }

    @Override
    public Collection<Vector3> getPoints() {
        if (needsRecalculation()) {
            recalculatePoints();
        }
        return new ArrayList<>(cachedPoints);
    }

    @Override
    public void invalidatePoints() {
        this.dirty = true;
        this.cachedPoints = null;
    }

    @Override
    public void generatePoints(DisplayMode mode, Collection<Vector3> points) {
        switch (mode) {
            case OUTLINE -> generateOutline(points);
            case SURFACE -> generateSurface(points);
            case FILLED -> generateFilled(points);
        }
    }

    @Override
    public String getEffectType() {
        return shapeType;
    }

    @Override
    public UUID getEffectId() {
        return shapeId;
    }

    @Override
    public boolean getDirty() {
        return dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void setConfiguration(EffectConfiguration configuration) {
        this.configuration = configuration;
        setDirty(true);
    }

    public EffectConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public EffectFormat clone() {
        try {
            AbstractEffect cloned = (AbstractEffect) super.clone();
            cloned.invalidatePoints();
            if (this.configuration != null) {
                cloned.configuration = cloneConfiguration();
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    protected EffectConfiguration cloneConfiguration() {
        return new DefaultEffectConfiguration(this.configuration);
    }
}
