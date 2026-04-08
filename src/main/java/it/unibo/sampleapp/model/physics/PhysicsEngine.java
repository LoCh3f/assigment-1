package it.unibo.sampleapp.model.physics;

import it.unibo.sampleapp.model.ball.Ball;
import it.unibo.sampleapp.model.hole.Hole;
import it.unibo.sampleapp.util.Vector2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stateless physics engine. All methods are pure functions — no fields,
 * no threading. Safe to call from any thread as long as callers
 * synchronize access to the ball list externally (done by GameModel).
 */
public class PhysicsEngine {
    private static final double CELL_SIZE = 40.0;
    private static final double FRICTION_COEFFICIENT = 0.1;
    private static final double MIN_SPEED = 0.1;
    private static final double EPSILON = 0.01;
    private static final long LOWER_32_MASK = 0xffffffffL;

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

            double newVx = vel.x();
            double newVy = vel.y();
            double newPx = pos.x();
            double newPy = pos.y();

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

    /**
     * Detects and resolves all ball-to-ball collisions.
     *
     * <p>
     * Uses a uniform spatial grid as a broad-phase optimization.
     * Balls are bucketed by cell, then narrow-phase collision checks are executed only:
     * <ul>
     *   <li>within the same cell (pairwise i &lt; j)</li>
     *   <li>between a cell and its 8 neighboring cells</li>
     * </ul>
     * This reduces unnecessary pair checks compared to full all-vs-all scanning,
     * while preserving the same collision response semantics.
     *
     * @param balls the list of all active balls
     */
    private void handleBallCollisions(final List<Ball> balls) {
        // Build grid: key = (cx, cy) packed into long, value = list of balls
        final Map<Long, List<Ball>> grid = new HashMap<>();

        for (final Ball b : balls) {
            final int cx = (int) (b.getPosition().x() / CELL_SIZE);
            final int cy = (int) (b.getPosition().y() / CELL_SIZE);
            final long key = (((long) cx) << 32) | (cy & LOWER_32_MASK);
            grid.computeIfAbsent(key, k -> new ArrayList<>()).add(b);
        }

        // For each cell, test collisions inside it and neighbouring cells
        for (final Map.Entry<Long, List<Ball>> entry : grid.entrySet()) {
            final long key = entry.getKey();
            final int cx = (int) (key >> 32);
            final int cy = (int) key;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    final long neighbourKey = (((long) (cx + dx)) << 32) | ((cy + dy) & LOWER_32_MASK);
                    final List<Ball> cellBalls = entry.getValue();
                    final List<Ball> neighbourBalls = grid.get(neighbourKey);
                    if (neighbourBalls == null) {
                        continue;
                    }

                    // To avoid double work, use <= condition carefully
                    if (neighbourKey == key) {
                        // Same cell: classic i<j loop
                        for (int i = 0; i < cellBalls.size(); i++) {
                            for (int j = i + 1; j < cellBalls.size(); j++) {
                                resolveCollision(cellBalls.get(i), cellBalls.get(j));
                            }
                        }
                    } else if (neighbourKey > key) {
                        // Different cell pairs: all combinations, but only once
                        for (final Ball a : cellBalls) {
                            for (final Ball b : neighbourBalls) {
                                resolveCollision(a, b);
                            }
                        }
                    }
                }
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
        Vector2D posA = a.getPosition();
        Vector2D posB = b.getPosition();

        // Vector from A to B
        final Vector2D delta = posB.subtract(posA);
        final double dist = delta.magnitude();
        final double minDist = a.getRadius() + b.getRadius();

        // No collision
        if (dist == 0 || dist >= minDist) {
            return;
        }

        // --- 1. Positional correction (push apart) ---
        final double overlap = minDist - dist + EPSILON;
        final Vector2D n = delta.scale(1.0 / dist); // unit normal A→B

        posA = posA.subtract(n.scale(overlap / 2));
        posB = posB.add(n.scale(overlap / 2));
        a.setPosition(posA);
        b.setPosition(posB);

        // --- 2. Velocity resolution in normal / tangent basis ---

        final Vector2D vA = a.getVelocity();
        final Vector2D vB = b.getVelocity();

        // Tangent (perpendicular to n)
        final Vector2D t = new Vector2D(-n.y(), n.x());

        // Decompose velocities
        final double normala = vA.dot(n);
        final double tangenta = vA.dot(t);
        final double normalb = vB.dot(n);
        final double tangentb = vB.dot(t);

        // If balls are separating along normal, don't resolve
        if (normala - normalb <= 0) {
            return;
        }

        // For equal mass: swap normal components, keep tangents [web:63][web:36]
        final double normalaftera = normalb;
        final double normalafterb = normala;

        final Vector2D velocityaftera = n.scale(normalaftera).add(t.scale(tangenta));
        final Vector2D velocityafterb = n.scale(normalafterb).add(t.scale(tangentb));

        a.setVelocity(velocityaftera);
        b.setVelocity(velocityafterb);

        // Record collision for scoring purposes:
        // If one ball is a player ball and the other is small, record the collision on the small ball
        if (b instanceof it.unibo.sampleapp.model.ball.impl.ImplBall implB
                && a.getType() != Ball.Type.SMALL) {
            implB.recordCollision(a.getType());
        }
        if (a instanceof it.unibo.sampleapp.model.ball.impl.ImplBall implA
                && b.getType() != Ball.Type.SMALL) {
            implA.recordCollision(b.getType());
        }
    }

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
                final double dist = ball.getPosition().subtract(hole.getPosition2D()).magnitude();
                // Ball is pocketed when its center reaches the hole center
                if (dist < hole.getRadius()) {
                    pocketed.add(ball);
                }
            }
        }
        balls.removeAll(pocketed);
        return pocketed;
    }
}
