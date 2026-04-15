package it.unibo.sampleapp.model.physics.step.multithread;

import it.unibo.sampleapp.model.domain.ball.Ball;
import it.unibo.sampleapp.model.physics.step.PhysicsStepResolver;
import it.unibo.sampleapp.model.physics.step.common.PhysicsStepPartitioning;

import java.util.ArrayList;
import java.util.List;

/**
 * Concurrent resolver for per-ball step operations.
 */
public final class ConcurrentPhysicsStepResolver implements PhysicsStepResolver {
    private static final int MIN_BALLS_FOR_CONCURRENT = 20;
    private static final int BALLS_PER_CHUNK = 64;

    private final PhysicsStepBag bag;
    private final PhysicsStepResolver fallbackResolver;

    /**
     * Builds a concurrent step resolver.
     *
     * @param bag shared monitor used by workers
     * @param fallbackResolver resolver used for small inputs or interruption fallback
     */
    public ConcurrentPhysicsStepResolver(final PhysicsStepBag bag, final PhysicsStepResolver fallbackResolver) {
        this.bag = bag;
        this.fallbackResolver = fallbackResolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolve(final List<Ball> balls, final double boardW, final double boardH, final double dt) {
        if (balls.size() < MIN_BALLS_FOR_CONCURRENT) {
            fallbackResolver.resolve(balls, boardW, boardH, dt);
            return;
        }

        final List<List<Ball>> chunks = PhysicsStepPartitioning.buildChunks(balls, BALLS_PER_CHUNK);
        final List<PhysicsStepBag.StepTask> tasks = new ArrayList<>(chunks.size());
        for (final List<Ball> chunk : chunks) {
            tasks.add(new PhysicsStepBag.StepTask(chunk, boardW, boardH, dt));
        }

        try {
            bag.submitPartitions(tasks);
            bag.awaitCompletion();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fallbackResolver.resolve(balls, boardW, boardH, dt);
        }
    }
}

