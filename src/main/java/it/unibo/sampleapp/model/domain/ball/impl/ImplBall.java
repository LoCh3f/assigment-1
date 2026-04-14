package it.unibo.sampleapp.model.domain.ball.impl;

import it.unibo.sampleapp.model.domain.ball.Ball;
import it.unibo.sampleapp.util.Vector2D;

/**
 * Implementation of the Ball interface.
 */
public final class ImplBall implements Ball {

    private volatile Vector2D position;
    private volatile Vector2D velocity;
    private final double radius;
    private final Type type;
    private volatile Type lastCollidedWithType;

    /**
     * Constructs a new ImplBall with the given parameters.
     *
     * @param position the initial position
     * @param velocity the initial velocity
     * @param radius the radius
     * @param type the type
     */
    public ImplBall(final Vector2D position, final Vector2D velocity, final double radius, final Type type) {
        this.position = position;
        this.velocity = velocity;
        this.radius = radius;
        this.type = type;
        this.lastCollidedWithType = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(final double dt) {
        this.position = position.add(velocity.scale(dt));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector2D getPosition() {
        return this.position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector2D getVelocity() {
        return this.velocity;
    }

    @Override
    public void setVelocity(final Vector2D velocity) {
        this.velocity = velocity;
    }

    @Override
    public void setPosition(final Vector2D position) {
        this.position = position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyFriction(final double friction, final double dt) {
        this.velocity = velocity.scale(Math.max(0, 1 - friction * dt));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getRadius() {
        return radius;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type getType() {
        return type;
    }

    /**
     * Records that this ball collided with another ball of the given type.
     * Used for tracking which player ball last hit this ball.
     *
     * @param otherType the type of ball that collided with this one
     */
    @Override
    public void recordCollision(final Type otherType) {
        this.lastCollidedWithType = otherType;
    }

    /**
     * Returns the type of ball that last collided with this ball.
     *
     * @return the type of the last colliding ball, or null if no collision recorded
     */
    @Override
    public Type getLastCollidedWithType() {
        return lastCollidedWithType;
    }
}

