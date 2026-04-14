package it.unibo.sampleapp.model.domain.ball;

import it.unibo.sampleapp.util.Vector2D;

/**
 * Model representation for the ball.
 */
public interface Ball {

    /**
     * Enum representing the type of ball.
     */
    enum Type { SMALL, HUMAN, BOT }

    /**
     * Sets the velocity of the ball.
     *
     * @param velocity the new velocity
     */
    void setVelocity(Vector2D velocity);

    /**
     * Sets the position of the ball.
     *
     * @param position the new position
     */
    void setPosition(Vector2D position);

    /**
     * Applies friction to the ball's velocity.
     *
     * @param friction the friction coefficient
     * @param dt the time delta
     */
    void applyFriction(double friction, double dt);

    /**
     * Moves the ball based on its velocity.
     *
     * @param dt the time delta
     */
    void move(double dt);

    /**
     * Gets the position of the ball.
     *
     * @return the position vector
     */
    Vector2D getPosition();

    /**
     * Gets the velocity of the ball.
     *
     * @return the velocity vector
     */
    Vector2D getVelocity();

    /**
     * Gets the radius of the ball.
     *
     * @return the radius
     */
    double getRadius();

    /**
     * Gets the type of the ball.
     *
     * @return the type
     */
    Type getType();

    /**
     * Records the type of the last ball that collided with this ball.
     *
     * @param otherType the type of the colliding ball
     */
    void recordCollision(Type otherType);

    /**
     * Gets the type of the last ball that collided with this ball.
     *
     * @return the type of the last colliding ball, or {@code null} if none
     */
    Type getLastCollidedWithType();

}
