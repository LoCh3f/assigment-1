package it.unibo.sampleapp.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Vector2DTest {
    private static final double EPSILON = 1.0E-10;
    private static final double EXPECTED_X = 1.0;
    private static final double EXPECTED_Y = 2.0;
    private static final double EXPECTED_SUM_X = 4.0;
    private static final double EXPECTED_SUM_Y = 6.0;
    private final Vector2D vector2D = new Vector2D(1, 2);

    @Test
    void testXY() {
        assertEquals(EXPECTED_X, vector2D.x(), EPSILON);
        assertEquals(EXPECTED_Y, vector2D.y(), EPSILON);
    }

    @Test
    void add() {
        final Vector2D other = new Vector2D(3, 4);
        final Vector2D result = vector2D.add(other);
        assertEquals(EXPECTED_SUM_X, result.x(), EPSILON);
        assertEquals(EXPECTED_SUM_Y, result.y(), EPSILON);
    }

    @Test
    void scale() {
        final Vector2D result = vector2D.scale(2.0);
        assertEquals(2.0, result.x(), EPSILON);
        assertEquals(4.0, result.y(), EPSILON);
    }

    @Test
    void magnitude() {
        final double result = vector2D.magnitude();
        assertEquals(Math.sqrt(1 + 4), result, EPSILON);
    }

    @Test
    void normalize() {
        final Vector2D result = vector2D.normalize();
        final double mag = vector2D.magnitude();
        assertEquals(1.0 / mag, result.x(), EPSILON);
        assertEquals(2.0 / mag, result.y(), EPSILON);

        // Test zero vector
        final Vector2D zero = new Vector2D(0, 0);
        final Vector2D zeroNormalized = zero.normalize();
        assertEquals(0.0, zeroNormalized.x(), EPSILON);
        assertEquals(0.0, zeroNormalized.y(), EPSILON);
    }

    @Test
    void dot() {
        final Vector2D other = new Vector2D(3, 4);
        final double result = vector2D.dot(other);
        assertEquals(1 * 3 + 2 * 4, result, EPSILON);
    }
}
