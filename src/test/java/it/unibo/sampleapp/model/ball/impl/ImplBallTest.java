package it.unibo.sampleapp.model.ball.impl;

import it.unibo.sampleapp.model.domain.ball.Ball;
import it.unibo.sampleapp.model.domain.ball.impl.ImplBall;
import it.unibo.sampleapp.util.Vector2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImplBallTest {
    private static final double EPSILON = 1.0E-10;
    private static final Vector2D POSITION = new Vector2D(1, 2);
    private static final Vector2D VELOCITY = new Vector2D(3, 4);
    private static final double RADIUS = 5.0;
    private static final Ball.Type TYPE = Ball.Type.SMALL;
    private final Ball ball = new ImplBall(POSITION, VELOCITY, RADIUS, TYPE);

    @Test
    void testConstructorAndGetters() {
        assertEquals(POSITION, ball.getPosition());
        assertEquals(VELOCITY, ball.getVelocity());
        assertEquals(RADIUS, ball.getRadius(), EPSILON);
        assertEquals(TYPE, ball.getType());
    }

    @Test
    void move() {
        final double dt = 0.5;
        final Vector2D expectedPosition = POSITION.add(VELOCITY.scale(dt));
        ball.move(dt);
        assertEquals(expectedPosition.x(), ball.getPosition().x(), EPSILON);
        assertEquals(expectedPosition.y(), ball.getPosition().y(), EPSILON);
    }

    @Test
    void applyFriction() {
        final double friction = 0.1;
        final double dt = 0.5;
        final Vector2D expectedVelocity = VELOCITY.scale(Math.max(0, 1 - friction * dt));
        ball.applyFriction(friction, dt);
        assertEquals(expectedVelocity.x(), ball.getVelocity().x(), EPSILON);
        assertEquals(expectedVelocity.y(), ball.getVelocity().y(), EPSILON);
    }
}
