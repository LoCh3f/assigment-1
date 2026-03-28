package it.unibo.sampleapp.model.ball.impl;

import it.unibo.sampleapp.model.ball.Ball;
import it.unibo.sampleapp.util.Vector2D;

/**
 * Implementation of the Ball interface.
 */
public final class ImplBall implements Ball {

    private volatile Vector2D position;
    private volatile Vector2D velocity;
    private final double radius;
    private final Type type;

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
    }

    @Override
    public void move(final double dt) {
        this.position = position.add(velocity.scale(dt));
    }

    @Override
    public Vector2D getPosition() {
        return this.position;
    }

    @Override
    public Vector2D getVelocity() {
        return this.velocity;
    }

    @Override
    public void applyFriction(final double friction, final double dt) {
        this.velocity = velocity.scale(Math.max(0, 1 - friction * dt));
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public Type getType() {
        return type;
    }
}
