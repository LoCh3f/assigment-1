package it.unibo.sampleapp.util;

/**
 * A 2D vector utility class.
 *
 * @param x component
 * @param y component
 */
public record Vector2D(double x, double y) {
    /**
     * Constructs a new Vector2D with the given x and y components.
     *
     * @param x the x component
     * @param y the y component
     */
    public Vector2D {
    }

    /**
     * Gets the x component.
     *
     * @return the x value
     */
    @Override
    public double x() {
        return x;
    }

    /**
     * Gets the y component.
     *
     * @return the y value
     */
    @Override
    public double y() {
        return y;
    }

    /**
     * Adds another vector to this one.
     *
     * @param other another 2D vector
     * @return the sum of this vector and the other
     */
    public Vector2D add(final Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }

    /**
     * Scales this vector by a factor.
     *
     * @param factor the scaling factor
     * @return the scaled vector
     */
    public Vector2D scale(final double factor) {
        return new Vector2D(x * factor, y * factor);
    }

    /**
     * Computes the magnitude of this vector.
     *
     * @return the magnitude
     */
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Normalizes this vector.
     *
     * @return the normalized vector
     */
    public Vector2D normalize() {
        final double mag = magnitude();
        return mag == 0 ? new Vector2D(0, 0) : scale(1.0 / mag);
    }

    /**
     * Computes the dot product with another vector.
     *
     * @param other another vector 2D
     * @return the dot product
     */
    public double dot(final Vector2D other) {
        return this.x * other.x + this.y * other.y;
    }

    /**
     * Subtracts another vector from this vector.
     *
     * @param other the vector to subtract
     * @return the difference of this vector and the other
     */
    public Vector2D subtract(final Vector2D other) {
        return new Vector2D(x - other.x, y - other.y);
    }

    /**
     * Computes the distance between this point and another point.
     *
     * @param other the other point
     * @return the Euclidean distance between the two points
     */
    public double distance(final Vector2D other) {
        return subtract(other).magnitude();
    }
}
