package org.klyx.flux.blueprint.reader;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.math.Transform;
import org.klyx.flux.math.Vec3;

public final class TransformReader {
    public static Transform read(Node node) {
        if (!node.isObject()) {
            node.error("expected an object like {\"translation\": [0, 1, 0]} but got " + node.describe() + " instead");
            return Transform.IDENTITY;
        }

        node.onlyAllow("translation", "rotation", "scale");

        Vec3 translation = node.vec3("translation", Vec3.ZERO, false);
        Vec3 rotation = node.vec3("rotation", Vec3.ZERO, false);
        double scale = node.number("scale", 1);
        if (!(scale > 0)) {
            Node scaleNode = node.child("scale");
            if (scaleNode != null) scaleNode.error("scale must be greater than 0 but got " + scale + " instead");
            scale = 1;
        }

        return Transform.IDENTITY
                .translation(translation)
                .eulerRotation(Math.toRadians(rotation.x()), Math.toRadians(rotation.y()), Math.toRadians(rotation.z()))
                .scale(scale);
    }
}
