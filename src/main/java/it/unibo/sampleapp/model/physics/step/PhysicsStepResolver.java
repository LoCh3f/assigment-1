package it.unibo.sampleapp.model.physics.step;

import it.unibo.sampleapp.model.domain.ball.Ball;

import java.util.List;

/**
 * Resolves per-ball physics step operations that do not require ball-to-ball interaction.
 */
@FunctionalInterface
public interface PhysicsStepResolver {

    /**
     * Applies movement, friction, and border handling to the provided balls.
     *
     * @param balls active balls to update
     * @param boardW board width
     * @param boardH board height
     * @param dt simulation time step in seconds
     */
    void resolve(List<Ball> balls, double boardW, double boardH, double dt);
}

