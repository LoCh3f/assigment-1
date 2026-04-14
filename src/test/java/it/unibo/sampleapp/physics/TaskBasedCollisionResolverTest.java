package it.unibo.sampleapp.physics;

import it.unibo.sampleapp.model.domain.ball.Ball;
import it.unibo.sampleapp.model.domain.ball.impl.ImplBall;
import it.unibo.sampleapp.model.physics.PhysicsEngine;
import it.unibo.sampleapp.model.physics.collision.sequential.SequentialCollisionResolver;
import it.unibo.sampleapp.model.physics.collision.taskbased.TaskBasedCollisionResolver;
import it.unibo.sampleapp.util.Vector2D;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for task-based collision resolution wiring.
 */
class TaskBasedCollisionResolverTest {

    private static final int BOARD_WIDTH = 800;
    private static final int BOARD_HEIGHT = 600;
    private static final double DT = 0.016;
    private static final int STEPS = 10;

    @Test
    void headOnCollisionChangesVelocityWithTaskBasedResolver() {
        final PhysicsEngine engine = new PhysicsEngine(
                new TaskBasedCollisionResolver(
                    Executors.newFixedThreadPool(2),
                    new SequentialCollisionResolver()
                )
        );

        final Ball a = new ImplBall(new Vector2D(50, 300), new Vector2D(50, 0), 2, Ball.Type.SMALL);
        final Ball b = new ImplBall(new Vector2D(80, 300), new Vector2D(-50, 0), 2, Ball.Type.SMALL);
        final List<Ball> balls = new ArrayList<>(List.of(a, b));

        final double initialA = a.getVelocity().x();
        final double initialB = b.getVelocity().x();

        for (int i = 0; i < STEPS; i++) {
            engine.step(balls, BOARD_WIDTH, BOARD_HEIGHT, List.of(), DT);
        }

        assertTrue(Math.abs(a.getVelocity().x()) < Math.abs(initialA) || a.getVelocity().x() * initialA < 0);
        assertTrue(Math.abs(b.getVelocity().x()) < Math.abs(initialB) || b.getVelocity().x() * initialB < 0);
    }
}

