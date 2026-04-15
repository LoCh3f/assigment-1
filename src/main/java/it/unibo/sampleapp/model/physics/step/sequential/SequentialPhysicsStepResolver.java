package it.unibo.sampleapp.model.physics.step.sequential;

import it.unibo.sampleapp.model.domain.ball.Ball;
import it.unibo.sampleapp.model.physics.step.PhysicsStepResolver;
import it.unibo.sampleapp.util.Vector2D;

import java.util.List;

/**
 * Sequential resolver for per-ball step updates.
 */
public final class SequentialPhysicsStepResolver implements PhysicsStepResolver {
    private static final double FRICTION_COEFFICIENT = 0.1;
    private static final double MIN_SPEED = 0.1;

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolve(final List<Ball> balls, final double boardW, final double boardH, final double dt) {
        for (final Ball ball : balls) {
            moveBall(ball, dt);
            applyFriction(ball, dt);
            handleBorderCollision(ball, boardW, boardH);
        }
    }

    private void moveBall(final Ball ball, final double dt) {
        ball.setPosition(ball.getPosition().add(ball.getVelocity().scale(dt)));
    }

    private void applyFriction(final Ball ball, final double dt) {
        final Vector2D velocity = ball.getVelocity();
        final double speed = velocity.magnitude();
        if (speed < MIN_SPEED) {
            ball.setVelocity(new Vector2D(0, 0));
            return;
        }
        ball.setVelocity(velocity.scale(Math.max(0, 1.0 - FRICTION_COEFFICIENT * dt)));
    }

    private void handleBorderCollision(final Ball ball, final double boardW, final double boardH) {
        final Vector2D pos = ball.getPosition();
        final Vector2D vel = ball.getVelocity();
        final double radius = ball.getRadius();

        double newVx = vel.x();
        double newVy = vel.y();
        double newPx = pos.x();
        double newPy = pos.y();

        if (newPx - radius < 0) {
            newPx = radius;
            newVx = Math.abs(newVx);
        } else if (newPx + radius > boardW) {
            newPx = boardW - radius;
            newVx = -Math.abs(newVx);
        }

        if (newPy - radius < 0) {
            newPy = radius;
            newVy = Math.abs(newVy);
        } else if (newPy + radius > boardH) {
            newPy = boardH - radius;
            newVy = -Math.abs(newVy);
        }

        ball.setPosition(new Vector2D(newPx, newPy));
        ball.setVelocity(new Vector2D(newVx, newVy));
    }
}

