package it.unibo.sampleapp.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Vector2DTest {
    private static final double EPSILON = 1.0E-10;
    private static final double EXPECTED_X = 1.0;
    private static final double EXPECTED_Y = 2.0;
    private final Vector2D vector2D = new Vector2D(1, 2);

    @Test
    void testXY() {
        assertEquals(EXPECTED_X, vector2D.getX(), EPSILON);
        assertEquals(EXPECTED_Y, vector2D.getY(), EPSILON);
    }

    @Test
    void add() {
    }

    @Test
    void scale() {
    }

    @Test
    void magnitude() {
    }

    @Test
    void normalize() {
    }

    @Test
    void dot() {
    }
}
