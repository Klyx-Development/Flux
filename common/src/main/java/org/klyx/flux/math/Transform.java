package org.klyx.flux.math;

import org.joml.Quaterniond;
import org.joml.Vector3d;

public record Transform(Vec3 translation, Quaterniond rotation, double scale) {
    public static final Transform IDENTITY = new Transform(Vec3.ZERO, new Quaterniond(), 1.0);

    public Transform {
        rotation = new Quaterniond(rotation);
    }

    @Override
    public Quaterniond rotation() {
        return new Quaterniond(rotation);
    }

    public Transform translation(Vec3 translation) {
        return new Transform(translation, rotation, scale);
    }

    public Transform scale(double scale) {
        return new Transform(translation, rotation, scale);
    }

    public Transform eulerRotation(double pitch, double yaw, double roll) {
        return new Transform(translation, new Quaterniond().rotationYXZ(yaw, pitch, roll), scale);
    }

    public Transform then(Transform next) {
        Quaterniond combined = next.rotation.mul(rotation, new Quaterniond());
        Vector3d moved = new Vector3d(translation.x(), translation.y(), translation.z()).mul(next.scale);
        next.rotation.transform(moved);

        Vec3 shifted = new Vec3(
                moved.x + next.translation.x(),
                moved.y + next.translation.y(),
                moved.z + next.translation.z());

        return new Transform(shifted, combined, scale * next.scale);
    }

    public void apply(float[] xyz, int pointCount) {
        boolean rotate = rotation.x != 0 || rotation.y != 0 || rotation.z != 0 || rotation.w != 1;
        Vector3d point = new Vector3d();

        for (int i = 0, end = pointCount * 3; i < end; i += 3) {
            point.set(xyz[i] * scale, xyz[i + 1] * scale, xyz[i + 2] * scale);
            if (rotate) rotation.transform(point);

            xyz[i] = (float) (point.x + translation.x());
            xyz[i + 1] = (float) (point.y + translation.y());
            xyz[i + 2] = (float) (point.z + translation.z());
        }
    }
}
