package org.klyx.flux.effect.anchor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class Followers {
    public interface Source {
        boolean isValid();

        double x();

        double y();

        double z();

        default double width() {
            return 0;
        }

        default double height() {
            return 0;
        }
    }

    private record Follow(Source source, double yOffset, DynamicAnchor anchor, BooleanSupplier ended) { }

    private final List<Follow> follows = new ArrayList<>();

    public void add(Source source, double yOffset, DynamicAnchor anchor, BooleanSupplier ended) {
        this.follows.add(new Follow(source, yOffset, anchor, ended));
    }

    public int tick() {
        for (int i = this.follows.size() - 1; i >= 0; i--) {
            Follow follow = this.follows.get(i);

            if (!follow.source().isValid() || follow.ended().getAsBoolean()) {
                follow.anchor().invalidate();
                this.follows.remove(i);
            } else {
                follow.anchor().update(follow.source().x(), follow.source().y() + follow.yOffset(), follow.source().z());
                follow.anchor().updateBounds(
                        follow.source().width(), follow.source().height(), follow.source().height() / 2 - follow.yOffset());
            }
        }

        return this.follows.size();
    }

    public boolean isEmpty() {
        return this.follows.isEmpty();
    }

    public int size() {
        return this.follows.size();
    }
}
