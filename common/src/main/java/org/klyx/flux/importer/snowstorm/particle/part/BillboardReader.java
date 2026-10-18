package org.klyx.flux.importer.snowstorm.particle.part;

import org.klyx.flux.blueprint.Node;
import org.klyx.flux.expr.Expression;
import org.klyx.flux.importer.snowstorm.reading.ReadingContext;
import org.klyx.flux.sim.spec.Billboard;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.util.Locale;

public final class BillboardReader implements ParticlePart {

    @Override
    public void apply(ReadingContext context, ParticleSpec.Builder particle) {
        Node node = context.component("particle_appearance_billboard");
        if (node == null) return;
        node.onlyAllow("size", "facing_camera_mode", "direction", "uv");

        Node size = node.child("size");
        if (size != null) readSize(context, size, particle);

        Node facing = node.child("facing_camera_mode");
        if (facing != null) particle.billboard(facing(context, facing));

        Node direction = node.child("direction");
        if (direction != null) context.ignore(direction, "particles don't turn to follow their velocity");

        Node uv = node.child("uv");
        if (uv != null) {
            context.ignore(uv, "textures from bedrock particles aren't supported");
        }
    }

    private void readSize(ReadingContext context, Node size, ParticleSpec.Builder particle) {
        boolean pair = size.isArray() && size.items().size() == 2;
        Expression width = pair ? context.values().expression(size.items().getFirst(), "0.1") : null;
        if (width == null) {
            if (size.isNumber() || size.isString()) {
                width = context.values().expression(size, "0.1");
            } else {
                size.error("expected the size as [width, height] but got " + size.describe());
            }
        }

        if (width == null) return;

        if (pair) {
            Expression height = context.values().expression(size.items().get(1), "0.1");
            if (!sameValue(width, height)) context.ignore(size, "particles are square, so only the width is used");
        }

        particle.size(width.map(0, 1.0));
    }

    private static boolean sameValue(Expression first, Expression second) {
        if (first.isConstant() && second.isConstant()) return first.evalConstant() == second.evalConstant();
        return first.source().equals(second.source());
    }

    private Billboard facing(ReadingContext context, Node node) {
        String mode = node.asString("lookat_xyz").toLowerCase(Locale.ROOT);
        return switch (mode) {
            case "lookat_xyz", "rotate_xyz" -> Billboard.CENTER;
            case "lookat_y", "rotate_y" -> Billboard.VERTICAL;
            case "lookat_direction", "direction_x", "direction_y", "direction_z",
                 "emitter_transform_xy", "emitter_transform_xz", "emitter_transform_yz" -> {
                context.ignore(node, "\"" + mode + "\" orients the particle in a way that java display entities can't, so it will use CENTER instead");
                yield Billboard.CENTER;
            }
            default -> {
                node.error("\"" + mode + "\" isn't a facing mode. Use lookat_xyz, lookat_y, rotate_xyz, rotate_y, lookat_direction, "
                        + "direction_x, direction_y, direction_z, emitter_transform_xy, emitter_transform_xz or emitter_transform_yz");
                yield Billboard.CENTER;
            }
        };
    }
}
