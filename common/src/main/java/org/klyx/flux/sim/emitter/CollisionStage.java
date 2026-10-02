package org.klyx.flux.sim.emitter;

import org.klyx.flux.sim.Numbers;
import org.klyx.flux.sim.SimVariables;
import org.klyx.flux.sim.collision.Collider;
import org.klyx.flux.sim.collision.Collision;
import org.klyx.flux.sim.spec.ParticleSpec;

public final class CollisionStage {
    private static final double SURFACE_EPSILON = 1.0e-4;

    private final ParticleSpec particle;
    private final SimVariables variables;
    private final Collider.Hit hit = new Collider.Hit();
    private Collider collider = Collider.NONE;

    public CollisionStage(ParticleSpec particle, SimVariables variables) {
        this.particle = particle;
        this.variables = variables;
    }

    public void collider(Collider collider) {
        this.collider = collider;
    }

    public boolean travel(ParticleStore store, int slot, double deltaSeconds) {
        Collision collision = this.particle.collision();

        if (collision == null || !isColliding(collision)) {
            move(store, slot, store.vx[slot] * deltaSeconds, store.vy[slot] * deltaSeconds, store.vz[slot] * deltaSeconds, 1);
            return true;
        }

        return sweepAndBounce(store, slot, collision, deltaSeconds);
    }

    private boolean isColliding(Collision collision) {
        return this.collider != Collider.NONE
                && Numbers.finiteOr(collision.enabled().eval(this.variables), 0) != 0;
    }

    private boolean sweepAndBounce(ParticleStore store, int slot, Collision collision, double deltaSeconds) {
        double remaining = 1;

        // limit the bounces so it doesn't just go forever
        for (int bounce = 0; bounce < 4 && remaining > 1.0e-6; bounce++) {
            double dx = store.vx[slot] * deltaSeconds * remaining;
            double dy = store.vy[slot] * deltaSeconds * remaining;
            double dz = store.vz[slot] * deltaSeconds * remaining;

            boolean blocked = this.collider.sweep(
                    store.x[slot], store.y[slot], store.z[slot], dx, dy, dz, collision.radius(), this.hit
            );

            if (!blocked) {
                move(store, slot, dx, dy, dz, 1);
                return true;
            }

            move(store, slot, dx, dy, dz, this.hit.t);
            if (collision.expireOnContact()) return false;

            double into = store.vx[slot] * this.hit.nx + store.vy[slot] * this.hit.ny + store.vz[slot] * this.hit.nz;
            if (into >= 0) {
                move(store, slot, dx, dy, dz, 1 - this.hit.t);
                return true;
            }

            if (bounceOff(store, slot, collision, into, deltaSeconds)) return true;
            remaining *= 1 - this.hit.t;
        }

        return true;
    }

    private static void move(ParticleStore store, int slot, double dx, double dy, double dz, double fraction) {
        store.x[slot] += dx * fraction;
        store.y[slot] += dy * fraction;
        store.z[slot] += dz * fraction;
    }

    private boolean bounceOff(ParticleStore store, int slot, Collision collision, double into, double deltaSeconds) {
        double tx = store.vx[slot] - into * this.hit.nx;
        double ty = store.vy[slot] - into * this.hit.ny;
        double tz = store.vz[slot] - into * this.hit.nz;
        double slide = Math.exp(-collision.drag() * deltaSeconds);
        double bounceSpeed = -into * collision.restitution();

        if (bounceSpeed < 0.2) bounceSpeed = 0;

        store.vx[slot] = tx * slide + this.hit.nx * bounceSpeed;
        store.vy[slot] = ty * slide + this.hit.ny * bounceSpeed;
        store.vz[slot] = tz * slide + this.hit.nz * bounceSpeed;

        store.x[slot] += this.hit.nx * SURFACE_EPSILON;
        store.y[slot] += this.hit.ny * SURFACE_EPSILON;
        store.z[slot] += this.hit.nz * SURFACE_EPSILON;

        return bounceSpeed == 0 && slide == 1 && tx == 0 && ty == 0 && tz == 0;
    }
}
