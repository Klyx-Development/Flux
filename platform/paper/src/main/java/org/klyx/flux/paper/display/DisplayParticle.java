package org.klyx.flux.paper.display;

import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
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

    private float size;
    private float roll;
    private int ramp;

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
        this.meta.setTeleportDuration(style.moveTicks());
        this.meta.setInterpolationDuration(style.interpolationTicks());
        this.ramp = style.interpolationTicks();
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
        applyRamp(this.style.interpolationTicks());
        applyAppearance(size, rollDegrees);
        applyColor(rgb);
    }

    public boolean update(int changed, float size, float rollDegrees, int rgb, int rampTicks) {
        boolean metaChanged = false;

        if ((changed & EmitterView.APPEARANCE) != 0) {
            applyAppearance(size, rollDegrees);
            applyRamp(rampTicks);
            metaChanged = true;
        }

        if ((changed & EmitterView.COLOR) != 0 && this.style.usesColor()) {
            applyColor(rgb);
            metaChanged = true;
        }

        // setting this forces the metadata to be sent so only do it when something's really changed
        if ((changed & EmitterView.APPEARANCE) != 0) this.meta.setInterpolationDelay(0);

        return metaChanged;
    }

    public List<MetaEntry<?>> takeAll() {
        List<MetaEntry<?>> entries = this.meta.toEntries();
        this.meta.markSynced();

        return entries;
    }

    public List<MetaEntry<?>> snapshot(float size, float rollDegrees, int rampTicks) {
        float keptSize = this.size;
        float keptRoll = this.roll;
        int keptRamp = this.ramp;

        applyAppearance(size, rollDegrees);
        applyRamp(rampTicks);
        List<MetaEntry<?>> entries = this.meta.toEntries();

        applyAppearance(keptSize, keptRoll);
        applyRamp(keptRamp);

        return entries;
    }

    public void markSynced() {
        this.meta.markSynced();
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

    private void applyRamp(int ticks) {
        this.ramp = ticks;
        this.meta.setInterpolationDuration(ticks);
    }

    private void applyAppearance(float size, float rollDegrees) {
        this.size = size;
        this.roll = rollDegrees;

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
