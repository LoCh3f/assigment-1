package it.unibo.sampleapp.model.physics;

import it.unibo.sampleapp.model.domain.ball.Ball;
import it.unibo.sampleapp.model.domain.hole.Hole;
import it.unibo.sampleapp.model.physics.collision.CollisionResolver;
import it.unibo.sampleapp.model.physics.collision.multithread.CollisionBag;
import it.unibo.sampleapp.model.physics.collision.multithread.ConcurrentCollisionResolver;
import it.unibo.sampleapp.model.physics.collision.sequential.SequentialCollisionResolver;
import it.unibo.sampleapp.util.Vector2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Stateless physics engine. All methods are pure functions — no fields,
 * no threading. Safe to call from any thread as long as callers
 * synchronize access to the ball list externally (done by GameModel).
 */
public final class PhysicsEngine {
    private static final double FRICTION_COEFFICIENT = 0.1;
    private static final double MIN_SPEED = 0.1;

    private final CollisionResolver collisionResolver;

    /**
     * Creates a physics engine with sequential collision resolution.
     */
    public PhysicsEngine() {
        this.collisionResolver = new SequentialCollisionResolver();
    }

    /**
     * Creates a physics engine with worker-backed collision resolution.
     *
     * @param bag shared monitor used by collision workers
     */
    public PhysicsEngine(final CollisionBag bag) {
        final CollisionResolver sequentialResolver = new SequentialCollisionResolver();
        this.collisionResolver = new ConcurrentCollisionResolver(bag, sequentialResolver);
    }

    /**
     * Creates a physics engine with a custom collision strategy.
     *
     * @param collisionResolver collision strategy to use at each step
     */
    public PhysicsEngine(final CollisionResolver collisionResolver) {
        this.collisionResolver = collisionResolver;
    }

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
        collisionResolver.resolve(balls);
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
