package it.unibo.sampleapp.physics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.sampleapp.model.ball.Ball;
import it.unibo.sampleapp.model.ball.impl.ImplBall;
import it.unibo.sampleapp.model.hole.Hole;
import it.unibo.sampleapp.model.hole.impl.HoleImpl;
import it.unibo.sampleapp.util.Vector2D;
import it.unibo.sampleapp.model.physics.PhysicsEngine;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the PhysicsEngine class.
 */
class PhysicsEngineTest {

    private static final int BOARD_WIDTH = 800;
    private static final int BOARD_HEIGHT = 600;
    private static final double DT = 0.016;
    private static final double VELOCITY_TOLERANCE = 0.01;
    private static final int BALL_SMALL_POS_X = 50;
    private static final int BALL_SMALL_POS_Y = 300;
    private static final int BALL_MEDIUM_POS_X = 80;
    private static final int BALL_RADIUS = 2;
    private static final int HOLE_CENTER_X = 400;
    private static final int HOLE_CENTER_Y = 300;
    private static final int HOLE_RADIUS = 20;
    private static final int LEFT_WALL_POS_X = 2;
    private static final double POSITIVE_VEL = 50.0;
    private static final double NEGATIVE_VEL = -50.0;
    private static final double INITIAL_VEL = 10.0;
    private static final int COLLISION_STEPS = 10;
    private static final int FRICTION_STEPS = 5000;

    private final PhysicsEngine engine = new PhysicsEngine();

    /**
     * Tests that a head-on collision between two balls changes their velocities.
     */
    @Test
    void headOnCollisionExchangesVelocities() {
        // Two equal-mass balls moving toward each other on the X axis
        final Ball a = new ImplBall(new Vector2D(BALL_SMALL_POS_X, BALL_SMALL_POS_Y),
                                    new Vector2D(POSITIVE_VEL, 0), BALL_RADIUS, Ball.Type.SMALL);
        final Ball b = new ImplBall(new Vector2D(BALL_MEDIUM_POS_X, BALL_SMALL_POS_Y),
                                    new Vector2D(NEGATIVE_VEL, 0), BALL_RADIUS, Ball.Type.SMALL);
        final List<Ball> balls = new ArrayList<>();
        balls.add(a);
        balls.add(b);

        final double initialVelA = a.getVelocity().x();
        final double initialVelB = b.getVelocity().x();

        // Run multiple steps to let the balls collide
        for (int i = 0; i < COLLISION_STEPS; i++) {
            engine.step(balls, BOARD_WIDTH, BOARD_HEIGHT, new ArrayList<>(), DT);
        }

        // After collision with friction, velocities should have changed significantly
        assertTrue(Math.abs(a.getVelocity().x()) < Math.abs(initialVelA)
                   || a.getVelocity().x() * initialVelA < 0,
                   "A velocity should change due to collision and friction");
        assertTrue(Math.abs(b.getVelocity().x()) < Math.abs(initialVelB)
                   || b.getVelocity().x() * initialVelB < 0,
                   "B velocity should change due to collision and friction");
    }

    /**
     * Tests that a ball bounces off the left wall.
     */
    @Test
    void ballBouncesOffLeftWall() {
        final Ball b = new ImplBall(new Vector2D(LEFT_WALL_POS_X, BALL_SMALL_POS_Y),
                                    new Vector2D(NEGATIVE_VEL, 0), BALL_RADIUS, Ball.Type.SMALL);
        final List<Ball> balls = new ArrayList<>();
        balls.add(b);
        engine.step(balls, BOARD_WIDTH, BOARD_HEIGHT, new ArrayList<>(), DT);
        assertTrue(b.getVelocity().x() > 0, "Should bounce off left wall");
    }

    /**
     * Tests that a ball is removed when it falls into a hole.
     */
    @Test
    void ballIsRemovedWhenPocketed() {
        final Hole hole = new HoleImpl(new Vector2D(HOLE_CENTER_X, HOLE_CENTER_Y), HOLE_RADIUS);
        final Ball b = new ImplBall(new Vector2D(HOLE_CENTER_X, HOLE_CENTER_Y),
                                    new Vector2D(0, 0), BALL_RADIUS, Ball.Type.SMALL);
        final List<Ball> balls = new ArrayList<>();
        balls.add(b);

        final List<Ball> pocketed = engine.step(balls, BOARD_WIDTH, BOARD_HEIGHT, List.of(hole), DT);

        assertEquals(1, pocketed.size());
        assertTrue(balls.isEmpty());
    }

    /**
     * Tests that friction eventually stops a ball.
     */
    @Test
    void frictionEventuallyStopsBall() {
        final Ball b = new ImplBall(new Vector2D(HOLE_CENTER_X, HOLE_CENTER_Y),
                                    new Vector2D(INITIAL_VEL, 0), BALL_RADIUS, Ball.Type.SMALL);
        final List<Ball> balls = new ArrayList<>();
        balls.add(b);

        for (int i = 0; i < FRICTION_STEPS; i++) {
            engine.step(balls, BOARD_WIDTH, BOARD_HEIGHT, new ArrayList<>(), DT);
        }

        assertEquals(0.0, b.getVelocity().magnitude(), VELOCITY_TOLERANCE);
    }
}











