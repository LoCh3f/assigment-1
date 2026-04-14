package it.unibo.sampleapp.model.physics.collision;

import it.unibo.sampleapp.model.ball.Ball;

import java.util.List;

/**
 * Strategy for resolving ball-to-ball collisions.
 */
@FunctionalInterface
public interface CollisionResolver {

    /**
     * Resolves collisions on the provided mutable ball list.
     *
     * @param balls active balls in the current simulation step
     */
    void resolve(List<Ball> balls);
}

