package org.klyx.flux.paper.display;

import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import org.klyx.exo.entity.LightweightExoEntity;
import org.klyx.exo.entity.data.ExoEntityType;
import org.klyx.exo.entity.meta.impl.AbstractEntityMeta;
import org.klyx.exo.entity.meta.impl.MetaEntry;
import org.klyx.exo.entity.meta.types.entity.display.DisplayMeta;
import org.klyx.exo.entity.meta.types.entity.display.ItemDisplayMeta;
import org.klyx.exo.entity.meta.types.entity.display.TextDisplayMeta;
import org.klyx.exo.paper.entity.PaperEntityTypes;
import org.klyx.exo.paper.meta.PaperMetaPackets;
import org.klyx.flux.sim.EmitterView;
import org.klyx.flux.sim.spec.DisplayKind;

import java.util.List;

public final class DisplayParticle extends LightweightExoEntity {
    private static final float TEXT_X = 8f;
    private static final float TEXT_Y = 4f;

    private final DisplayStyle style;
    private final DisplayMeta meta;
    private final @Nullable TextDisplayMeta text;
    private final @Nullable ItemDisplayMeta item;

    public DisplayParticle(DisplayStyle style) {
        this.style = style;

        if (style.kind() == DisplayKind.TEXT) {
            TextDisplayMeta textMeta = new TextDisplayMeta();
            textMeta.setText(Component.text(" "));

            this.text = textMeta;
            this.item = null;
            this.meta = textMeta;
        } else {
            ItemDisplayMeta itemMeta = new ItemDisplayMeta();

            this.text = null;
            this.item = itemMeta;
            this.meta = itemMeta;
        }

        this.meta.setBillboardConstraints(style.billboard());
        this.meta.setViewRange(style.viewRange());
        this.meta.setShadowRadius(0f);
        this.meta.setTeleportDuration(style.interpolationTicks());
        this.meta.setInterpolationDuration(style.interpolationTicks());
        if (style.emissive()) this.meta.setBrightnessOverride(15);
    }

    @Override
    public ExoEntityType entityType() {
        return this.style.entityType();
    }

    @Override
    public AbstractEntityMeta meta() {
        return this.meta;
    }

    public void reset(float size, float rollDegrees, int rgb) {
        this.meta.setInterpolationDelay(0);
        applyAppearance(size, rollDegrees);
        applyColor(rgb);
    }

    public boolean update(int changed, float size, float rollDegrees, int rgb) {
        boolean metaChanged = false;

        if ((changed & EmitterView.APPEARANCE) != 0) {
            applyAppearance(size, rollDegrees);
            metaChanged = true;
        }

        if ((changed & EmitterView.COLOR) != 0 && this.style.usesColor()) {
            applyColor(rgb);
            metaChanged = true;
        }

        // setting this forces the metadata to be sent so only do it when something's really changed
        if (metaChanged) this.meta.setInterpolationDelay(0);

        return metaChanged;
    }

    public List<MetaEntry<?>> takeAll() {
        List<MetaEntry<?>> entries = this.meta.toEntries();
        this.meta.markSynced();

        return entries;
    }

    public List<MetaEntry<?>> takeDirty() {
        List<MetaEntry<?>> dirty = this.meta.dirtyEntries();
        this.meta.markSynced();

        return dirty;
    }

    public Packet<?> addPacket(double x, double y, double z) {
        return new ClientboundAddEntityPacket(
                entityId(), uuid(), x, y, z, 0f, 0f,
                PaperEntityTypes.toNms(this.style.entityType()), 0, Vec3.ZERO, 0f);
    }

    public @Nullable Packet<?> dataPacket(List<MetaEntry<?>> entries, int protocolVersion) {
        return PaperMetaPackets.toPacket(entityId(), entries, protocolVersion);
    }

    public Packet<?> positionPacket(double x, double y, double z) {
        PositionMoveRotation position = new PositionMoveRotation(new Vec3(x, y, z), Vec3.ZERO, 0f, 0f);
        return new ClientboundEntityPositionSyncPacket(entityId(), position, false);
    }

    private void applyAppearance(float size, float rollDegrees) {
        if (this.text != null) {
            this.meta.setScale(new Vector3f(size * TEXT_X, size * TEXT_Y, 1f));
        } else {
            this.meta.setScale(new Vector3f(size, size, size));
        }

        this.meta.setLeftRotation(new Quaternionf().rotateZ((float) Math.toRadians(rollDegrees)));
    }

    private void applyColor(int rgb) {
        if (this.text != null) {
            this.text.setBackgroundColor(0xFF000000 | (rgb & 0xFFFFFF));
        } else if (this.item != null) {
            this.item.setItem(this.style.stack(rgb));
        }
    }
}
