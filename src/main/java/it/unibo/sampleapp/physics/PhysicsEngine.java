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

    /**
     * Main simulation step — call this every tick from the game loop to advance physics.
     *
     * <p>
     * This method orchestrates the entire physics simulation by:
     * <ol>
     *   <li>Moving all balls based on their current velocity</li>
     *   <li>Applying friction to reduce ball velocities</li>
     *   <li>Handling collisions with board borders</li>
     *   <li>Handling ball-to-ball elastic collisions</li>
     *   <li>Detecting which balls have fallen into holes</li>
     * </ol>
     *
     * @param balls     mutable list of all active balls
     * @param boardW    board width in pixels or world units
     * @param boardH    board height in pixels or world units
     * @param holes     list of holes on the board
     * @param dt        time delta in seconds (e.g. 0.016 for 60fps)
     * @return          list of balls that fell into a hole during this step
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

    /**
     * Moves all balls based on their velocity and the time delta.
     *
     * <p>
     * Updates each ball's position using: newPosition = currentPosition + velocity * dt
     * This implements simple Euler integration for ballistics.
     *
     * @param balls the list of balls to move
     * @param dt    the time delta in seconds
     */
    private void moveBalls(final List<Ball> balls, final double dt) {
        for (final Ball b : balls) {
            b.setPosition(b.getPosition().add(b.getVelocity().scale(dt)));
        }
    }

    /**
     * Applies friction to all balls to reduce their velocity over time.
     *
     * <p>
     * Implements exponential decay: newVelocity = velocity * (1 - FRICTION_COEFFICIENT * dt)
     * Balls with speed below MIN_SPEED are stopped completely.
     *
     * @param balls the list of balls to apply friction to
     * @param dt    the time delta in seconds
     */
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

    /**
     * Handles collisions between balls and the board's borders.
     *
     * <p>
     * When a ball hits a wall, its velocity component perpendicular to that wall is reversed
     * (reflected), while the position is clamped to prevent the ball from leaving the board.
     * This simulates realistic bouncing off rigid walls.
     * <ul>
     *   <li>Left/Right walls: newVx = abs(oldVx) or -abs(oldVx)</li>
     *   <li>Top/Bottom walls: newVy = abs(oldVy) or -abs(oldVy)</li>
     * </ul>
     *
     * @param balls   the list of balls to check and update
     * @param boardW  the width of the board
     * @param boardH  the height of the board
     */
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
    // Ball-ball elastic collisions
    // -----------------------------------------------------------------------

    /**
     * Detects and resolves all ball-to-ball collisions.
     *
     * <p>
     * Uses an O(n²) brute-force approach to check all pairs of balls for collisions.
     * For large numbers of balls (thousands+), consider spatial optimization such as
     * grid partitioning or spatial hashing for better performance.
     *
     * @param balls the list of all active balls
     */
    private void handleBallCollisions(final List<Ball> balls) {
        final int n = balls.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                resolveCollision(balls.get(i), balls.get(j));
            }
        }
    }

    /**
     * Resolves a collision between two balls using elastic collision physics.
     *
     * <p>
     * This method performs two key steps:
     * <ol>
     *   <li><b>Positional Correction:</b> Separates overlapping balls along their collision normal
     *       proportional to their mass ratios</li>
     *   <li><b>Velocity Update:</b> Applies impulse-based response to exchange momentum based on
     *       relative velocity, mass, and collision normal</li>
     * </ol>
     *
     * <p>
     * The collision is only resolved if the balls are approaching each other (relative velocity
     * toward collision normal is positive). If they are separating, no action is taken.
     *
     * @param a the first ball involved in the collision
     * @param b the second ball involved in the collision
     */
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
    // Hole detection
    // -----------------------------------------------------------------------

    /**
     * Detects and removes balls that have fallen into holes.
     *
     * <p>
     * Iterates through all holes and checks if any ball's center is within the hole's radius.
     * Balls that fall into holes are removed from the active balls list and returned separately.
     * A ball is considered pocketed when the distance from its center to the hole's center
     * is less than the hole's radius.
     *
     * @param balls the mutable list of active balls (will be modified)
     * @param holes the list of holes on the board
     * @return a list of balls that were pocketed during this check
     */
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
    // Helper methods
    // -----------------------------------------------------------------------

    /**
     * Determines the mass of a ball based on its type.
     *
     * <p>
     * Different ball types have different masses to simulate realistic physics:
     * <ul>
     *   <li>{@link Ball.Type#SMALL SMALL}: lightweight ball with mass 1.0</li>
     *   <li>{@link Ball.Type#HUMAN HUMAN}: regular ball with mass 3.0</li>
     *   <li>{@link Ball.Type#BOT BOT}: regular ball with mass 3.0</li>
     * </ul>
     *
     * <p>
     * Heavier balls (HUMAN and BOT) are more realistic and will have more momentum
     * compared to the lighter SMALL balls.
     *
     * @param b the ball whose mass is to be determined
     * @return the mass value appropriate for the ball's type
     */
    private double getMass(final Ball b) {
        return switch (b.getType()) {
            case HUMAN, BOT -> PLAYER_BALL_MASS;
            case SMALL -> SMALL_BALL_MASS;
        };
    }
}
