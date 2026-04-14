package it.unibo.sampleapp.model.domain.hole;

import it.unibo.sampleapp.util.Vector2D;

/**
 * Hole representation.
 */
public interface Hole {

    /**
     * Gets the 2D position of the hole.
     *
     * @return the position vector
     */
    Vector2D getPosition2D();

    /**
     * Gets the radius of the hole.
     *
     * @return the radius
     */
    double getRadius();

}

