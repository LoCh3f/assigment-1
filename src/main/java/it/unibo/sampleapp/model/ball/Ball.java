package it.unibo.sampleapp.model.ball;

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

}
