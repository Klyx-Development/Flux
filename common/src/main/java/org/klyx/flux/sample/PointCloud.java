package org.klyx.flux.sample;

import org.klyx.flux.math.Transform;
import org.klyx.flux.math.Vec3;

import java.util.Arrays;
import java.util.Collection;

public final class PointCloud {
    public static final PointCloud EMPTY = new PointCloud(new float[0], 0);

    public static final int MAX_POINTS = 1_000_000;

    // the array is reused between calls, can't keep a reference to it!
    @FunctionalInterface
    public interface PointMapper {
        void map(double[] point);
    }

    private final float[] xyz;
    private final int size;

    private PointCloud(float[] xyz, int size) {
        this.xyz = xyz;
        this.size = size;
    }

    public int size() {
        return this.size;
    }

    public float x(int index) {
        return this.xyz[index * 3];
    }

    public float y(int index) {
        return this.xyz[index * 3 + 1];
    }

    public float z(int index) {
        return this.xyz[index * 3 + 2];
    }

    public PointCloud transformed(Transform transform) {
        float[] copy = Arrays.copyOf(this.xyz, this.size * 3);
        transform.apply(copy, this.size);
        return new PointCloud(copy, this.size);
    }

    // scaled on each axis, which Transform can't do, and then moved
    public PointCloud stretched(Vec3 scale, Vec3 offset) {
        float[] stretched = new float[this.size * 3];

        for (int i = 0; i < stretched.length; i += 3) {
            stretched[i] = (float) (this.xyz[i] * scale.x() + offset.x());
            stretched[i + 1] = (float) (this.xyz[i + 1] * scale.y() + offset.y());
            stretched[i + 2] = (float) (this.xyz[i + 2] * scale.z() + offset.z());
        }

        return new PointCloud(stretched, this.size);
    }

    public PointCloud map(PointMapper mapper) {
        float[] mapped = new float[this.size * 3];
        double[] point = new double[3];

        for (int i = 0; i < mapped.length; i += 3) {
            point[0] = this.xyz[i];
            point[1] = this.xyz[i + 1];
            point[2] = this.xyz[i + 2];

            mapper.map(point);

            mapped[i] = (float) point[0];
            mapped[i + 1] = (float) point[1];
            mapped[i + 2] = (float) point[2];
        }

        return new PointCloud(mapped, this.size);
    }

    public static PointCloud concat(Collection<PointCloud> clouds) {
        int total = 0;
        for (PointCloud cloud : clouds) {
            total += cloud.size;
        }

        if (total == 0) return EMPTY;
        if (total > MAX_POINTS) throw tooMany();

        float[] combined = new float[total * 3];
        int offset = 0;

        for (PointCloud cloud : clouds) {
            System.arraycopy(cloud.xyz, 0, combined, offset, cloud.size * 3);
            offset += cloud.size * 3;
        }

        return new PointCloud(combined, total);
    }

    private static IllegalStateException tooMany() {
        return new IllegalStateException(
                "particle would be more than " + MAX_POINTS + " points. Lower the density/size");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private float[] data = new float[96];
        private int size;

        public Builder add(double x, double y, double z) {
            if (this.size >= MAX_POINTS) throw tooMany();

            int offset = this.size * 3;
            if (offset + 3 > this.data.length) {
                this.data = Arrays.copyOf(this.data, this.data.length * 2);
            }

            this.data[offset] = (float) x;
            this.data[offset + 1] = (float) y;
            this.data[offset + 2] = (float) z;
            this.size++;

            return this;
        }

        public PointCloud build() {
            return this.size == 0 ? EMPTY : new PointCloud(Arrays.copyOf(this.data, this.size * 3), this.size);
        }
    }
}
