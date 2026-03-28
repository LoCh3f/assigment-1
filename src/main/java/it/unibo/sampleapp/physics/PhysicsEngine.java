package it.unibo.sampleapp.physics;

import it.unibo.sampleapp.model.ball.Ball;
import it.unibo.sampleapp.model.hole.Hole;
import it.unibo.sampleapp.util.Vector2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Stateless physics engine. All methods are pure functions — no fields,
 * no threading. Safe to call from any thread as long as callers
 * synchronize access to the ball list externally (done by GameModel).
 */
public class PhysicsEngine {

    private static final double FRICTION_COEFFICIENT = 0.8; // tune this
    private static final double MIN_SPEED = 0.5;           // below this → ball stops
    private static final double SMALL_BALL_MASS = 1.0;
    private static final double PLAYER_BALL_MASS = 3.0;    // heavier = more realistic

    // -----------------------------------------------------------------------
    // Main step — call this every tick from the game loop
    // -----------------------------------------------------------------------

    /**
     * Advances the simulation by dt seconds.
     *
     * @param balls     mutable list of all active balls
     * @param boardW    board width  (pixels or world units)
     * @param boardH    board height
     * @param holes     list of holes on the board
     * @param dt        time delta in seconds (e.g. 0.016 for 60fps)
     * @return          list of balls that fell into a hole this step
     */
    public List<Ball> step(final List<Ball> balls, final double boardW, final double boardH,
                           final List<Hole> holes, final double dt) {
        moveBalls(balls, dt);
        applyFriction(balls, dt);
        handleBorderCollisions(balls, boardW, boardH);
        handleBallCollisions(balls);
        return checkHoles(balls, holes);
    }

    // -----------------------------------------------------------------------
    // Movement and friction
    // -----------------------------------------------------------------------

    private void moveBalls(final List<Ball> balls, final double dt) {
        for (final Ball b : balls) {
            b.setPosition(b.getPosition().add(b.getVelocity().scale(dt)));
        }
    }

    private void applyFriction(final List<Ball> balls, final double dt) {
        for (final Ball b : balls) {
            final Vector2D v = b.getVelocity();
            final double speed = v.magnitude();
            if (speed < MIN_SPEED) {
                b.setVelocity(new Vector2D(0, 0));
            } else {
                // exponential decay: v' = v * (1 - friction * dt)
                b.setVelocity(v.scale(Math.max(0, 1.0 - FRICTION_COEFFICIENT * dt)));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Border collisions — reflect velocity on each wall
    // -----------------------------------------------------------------------

    private void handleBorderCollisions(final List<Ball> balls, final double boardW, final double boardH) {
        for (final Ball b : balls) {
            final Vector2D pos = b.getPosition();
            final Vector2D vel = b.getVelocity();
            final double r = b.getRadius();

            double newVx = vel.getX();
            double newVy = vel.getY();
            double newPx = pos.getX();
            double newPy = pos.getY();

            // Left / Right walls
            if (newPx - r < 0) {
                newPx = r;
                newVx = Math.abs(newVx);    // bounce right
            } else if (newPx + r > boardW) {
                newPx = boardW - r;
                newVx = -Math.abs(newVx);   // bounce left
            }

            // Top / Bottom walls
            if (newPy - r < 0) {
                newPy = r;
                newVy = Math.abs(newVy);    // bounce down
            } else if (newPy + r > boardH) {
                newPy = boardH - r;
                newVy = -Math.abs(newVy);   // bounce up
            }

            b.setPosition(new Vector2D(newPx, newPy));
            b.setVelocity(new Vector2D(newVx, newVy));
        }
    }

    // -----------------------------------------------------------------------
    // Ball-ball elastic collisions — O(n²), good for thousands of balls
    // with spatial optimisation possible later (grid partitioning)
    // -----------------------------------------------------------------------

    private void handleBallCollisions(final List<Ball> balls) {
        final int n = balls.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                resolveCollision(balls.get(i), balls.get(j));
            }
        }
    }

    private void resolveCollision(final Ball a, final Ball b) {
        final Vector2D posA = a.getPosition();
        final Vector2D posB = b.getPosition();

        // Vector from A to B
        final Vector2D delta = posB.subtract(posA);
        final double dist = delta.magnitude();
        final double minDist = a.getRadius() + b.getRadius();

        // No collision
        if (dist >= minDist || dist == 0) {
            return;
        }

        // --- 1. Positional correction: push balls apart so they don't overlap ---
        final Vector2D normal = delta.normalize();
        final double overlap = minDist - dist;
        final double mA = getMass(a);
        final double mB = getMass(b);
        final double totalMass = mA + mB;

        a.setPosition(posA.subtract(normal.scale(overlap * (mB / totalMass))));
        b.setPosition(posB.add(normal.scale(overlap * (mA / totalMass))));

        // --- 2. Velocity update via impulse formula ---
        final Vector2D velA = a.getVelocity();
        final Vector2D velB = b.getVelocity();

        final Vector2D relVel = velA.subtract(velB);
        final double relVelAlongNormal = relVel.dot(normal);

        // Only resolve if balls are approaching each other
        if (relVelAlongNormal > 0) {
            return;
        }

        // Impulse scalar: p = 2 * (vA - vB)·n / (mA + mB)
        final double impulse = 2.0 * relVelAlongNormal / totalMass;

        a.setVelocity(velA.subtract(normal.scale(impulse * mB)));
        b.setVelocity(velB.add(normal.scale(impulse * mA)));
    }

    // -----------------------------------------------------------------------
    // Hole detection — returns and removes balls that fell in
    // -----------------------------------------------------------------------

    private List<Ball> checkHoles(final List<Ball> balls, final List<Hole> holes) {
        final List<Ball> pocketed = new ArrayList<>();
        for (final Hole hole : holes) {
            for (final Ball ball : balls) {
                final double dist = ball.getPosition().subtract(hole.getPosition()).magnitude();
                // Ball is pocketed when its center reaches the hole center
                if (dist < hole.getRadius()) {
                    pocketed.add(ball);
                }
            }
        }
        balls.removeAll(pocketed);
        return pocketed;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private double getMass(final Ball b) {
        return switch (b.getType()) {
            case HUMAN, BOT -> PLAYER_BALL_MASS;
            case SMALL -> SMALL_BALL_MASS;
        };
    }
}
