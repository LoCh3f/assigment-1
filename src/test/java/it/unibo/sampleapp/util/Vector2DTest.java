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
        Vector2D other = new Vector2D(3, 4);
        Vector2D result = vector2D.add(other);
        assertEquals(4.0, result.getX(), EPSILON);
        assertEquals(6.0, result.getY(), EPSILON);
    }

    @Test
    void scale() {
        Vector2D result = vector2D.scale(2.0);
        assertEquals(2.0, result.getX(), EPSILON);
        assertEquals(4.0, result.getY(), EPSILON);
    }

    @Test
    void magnitude() {
        double result = vector2D.magnitude();
        assertEquals(Math.sqrt(1 + 4), result, EPSILON);
    }

    @Test
    void normalize() {
        Vector2D result = vector2D.normalize();
        double mag = vector2D.magnitude();
        assertEquals(1.0 / mag, result.getX(), EPSILON);
        assertEquals(2.0 / mag, result.getY(), EPSILON);

        // Test zero vector
        Vector2D zero = new Vector2D(0, 0);
        Vector2D zeroNormalized = zero.normalize();
        assertEquals(0.0, zeroNormalized.getX(), EPSILON);
        assertEquals(0.0, zeroNormalized.getY(), EPSILON);
    }

    @Test
    void dot() {
        Vector2D other = new Vector2D(3, 4);
        double result = vector2D.dot(other);
        assertEquals(1 * 3 + 2 * 4, result, EPSILON);
    }
}
