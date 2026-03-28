package it.unibo.sampleapp.model.hole.impl;

import it.unibo.sampleapp.model.hole.Hole;
import it.unibo.sampleapp.util.Vector2D;

/**
 * Implementation of the Hole interface.
 */
public final class HoleImpl implements Hole {
    private final Vector2D position;
    private final double radius;

    /**
     * Constructs a new HoleImpl with the given position and radius.
     *
     * @param position the position of the hole
     * @param radius the radius of the hole
     */
    public HoleImpl(final Vector2D position, final double radius) {
        this.position = position;
        this.radius = radius;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector2D getPosition2D() {
        return this.position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getRadius() {
        return this.radius;
    }
}
