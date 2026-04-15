package it.unibo.sampleapp.model.physics;

import it.unibo.sampleapp.model.domain.ball.Ball;
import it.unibo.sampleapp.model.domain.hole.Hole;
import it.unibo.sampleapp.model.physics.collision.CollisionResolver;
import it.unibo.sampleapp.model.physics.collision.multithread.CollisionBag;
import it.unibo.sampleapp.model.physics.collision.multithread.ConcurrentCollisionResolver;
import it.unibo.sampleapp.model.physics.collision.sequential.SequentialCollisionResolver;
import it.unibo.sampleapp.model.physics.step.PhysicsStepResolver;
import it.unibo.sampleapp.model.physics.step.multithread.ConcurrentPhysicsStepResolver;
import it.unibo.sampleapp.model.physics.step.multithread.PhysicsStepBag;
import it.unibo.sampleapp.model.physics.step.sequential.SequentialPhysicsStepResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Physics engine orchestrator.
 *
 * <p>
 * The engine delegates independent per-ball updates (movement, friction, borders)
 * to a {@link PhysicsStepResolver} and ball-to-ball interactions to a
 * {@link CollisionResolver}. Callers must still synchronize access to shared
 * game state externally (done by GameModel).
 */
public final class PhysicsEngine {
    private final PhysicsStepResolver stepResolver;
    private final CollisionResolver collisionResolver;

    /**
     * Creates a physics engine with fully sequential strategies.
     */
    public PhysicsEngine() {
        this.stepResolver = new SequentialPhysicsStepResolver();
        this.collisionResolver = new SequentialCollisionResolver();
    }

    /**
     * Creates a physics engine with worker-backed step and collision resolution.
     *
     * @param collisionBag shared monitor used by collision workers
     * @param stepBag shared monitor used by step workers
     */
    public PhysicsEngine(final CollisionBag collisionBag, final PhysicsStepBag stepBag) {
        final PhysicsStepResolver sequentialStepResolver = new SequentialPhysicsStepResolver();
        this.stepResolver = new ConcurrentPhysicsStepResolver(stepBag, sequentialStepResolver);
        final CollisionResolver sequentialResolver = new SequentialCollisionResolver();
        this.collisionResolver = new ConcurrentCollisionResolver(collisionBag, sequentialResolver);
    }

    /**
     * Creates a physics engine with sequential step resolution and a custom collision strategy.
     *
     * @param collisionResolver collision strategy to use at each step
     */
    public PhysicsEngine(final CollisionResolver collisionResolver) {
        this.stepResolver = new SequentialPhysicsStepResolver();
        this.collisionResolver = collisionResolver;
    }

    /**
     * Main simulation step — call this every tick from the game loop to advance physics.
     *
     * <p>
     * This method orchestrates the entire physics simulation by:
     * <ol>
     *   <li>Applying per-ball step updates via {@link PhysicsStepResolver}</li>
     *   <li>Resolving ball-to-ball interactions via {@link CollisionResolver}</li>
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
        stepResolver.resolve(balls, boardW, boardH, dt);
        collisionResolver.resolve(balls);
        return checkHoles(balls, holes);
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
