package it.unibo.sampleapp.model.physics.collision.common;

/**
 * Shared constants for collision resolution strategies.
 */
public final class CollisionResolverConstants {

    /** Minimum number of balls before parallel collision strategies are activated. */
    public static final int MIN_BALLS_FOR_CONCURRENT_COLLISIONS = 24;
    /** Target ball count used to build spatial partitions for parallel work. */
    public static final int BALLS_PER_PARTITION = 12;

    private CollisionResolverConstants() {
    }
}




