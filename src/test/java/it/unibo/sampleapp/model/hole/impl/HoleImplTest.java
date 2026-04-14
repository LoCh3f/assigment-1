package it.unibo.sampleapp.model.hole.impl;

import it.unibo.sampleapp.model.domain.hole.Hole;
import it.unibo.sampleapp.model.domain.hole.impl.HoleImpl;
import it.unibo.sampleapp.util.Vector2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HoleImplTest {
    private static final double EPSILON = 1.0E-10;
    private static final Vector2D POSITION = new Vector2D(1, 2);
    private static final double RADIUS = 5.0;
    private final Hole hole = new HoleImpl(POSITION, RADIUS);

    @Test
    void testConstructorAndGetters() {
        assertEquals(POSITION, hole.getPosition2D());
        assertEquals(RADIUS, hole.getRadius(), EPSILON);
    }
}
