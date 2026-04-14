package it.unibo.sampleapp.model.physics.collision.taskbased;

import it.unibo.sampleapp.model.domain.ball.Ball;
import it.unibo.sampleapp.model.physics.collision.CollisionResolver;
import it.unibo.sampleapp.model.physics.collision.common.CollisionPairResolver;
import it.unibo.sampleapp.model.physics.collision.common.CollisionPartitioning;
import it.unibo.sampleapp.model.physics.collision.common.CollisionResolverConstants;
import it.unibo.sampleapp.model.physics.collision.sequential.SequentialCollisionResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;

/**
 * Task-based collision resolver using an external {@link ExecutorService}.
 * Follows the same paradigm as TaskBasedController: stateless resolution logic,
 * external executor lifecycle management, and submission-based task orchestration.
 */
public final class TaskBasedCollisionResolver implements CollisionResolver {

    private final ExecutorService executor;
    private final CollisionResolver fallbackResolver;

    /**
     * Builds a task-based resolver delegating to external executor.
     * Executor lifecycle is managed externally (typically by TaskBasedController).
     *
     * @param executor external executor service for submitting collision tasks
     * @param fallbackResolver resolver used for small inputs and fallback paths
     */
    public TaskBasedCollisionResolver(final ExecutorService executor, final CollisionResolver fallbackResolver) {
        this.executor = executor;
        this.fallbackResolver = fallbackResolver;
    }

    /**
     * Builds a task-based resolver with a default thread pool executor.
     */
    public TaskBasedCollisionResolver() {
        this(
            java.util.concurrent.Executors.newFixedThreadPool(
                Math.max(2, Runtime.getRuntime().availableProcessors() - 1)
            ),
            new SequentialCollisionResolver()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolve(final List<Ball> balls) {
        if (balls.size() < CollisionResolverConstants.MIN_BALLS_FOR_CONCURRENT_COLLISIONS) {
            fallbackResolver.resolve(balls);
            return;
        }

        final List<List<Ball>> partitions = CollisionPartitioning.buildByXAxis(
                balls,
                CollisionResolverConstants.BALLS_PER_PARTITION
        );

        final List<Future<?>> futures = new ArrayList<>(partitions.size());
        for (final List<Ball> partition : partitions) {
            futures.add(executor.submit(() -> resolvePartition(partition)));
        }

        if (!waitForTasks(futures, balls)) {
            return;
        }

        CollisionPartitioning.resolveCrossPartitionBoundaries(partitions);
    }

    /**
     * Waits for all pending tasks to complete.
     * On interruption or execution error, falls back to sequential resolution.
     *
     * @param futures list of pending partition resolution tasks
     * @param balls balls list for fallback resolution
     * @return true if all tasks completed successfully, false if fallback was triggered
     */
    private boolean waitForTasks(final List<Future<?>> futures, final List<Ball> balls) {
        try {
            for (final Future<?> future : futures) {
                future.get();
            }
            return true;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fallbackResolver.resolve(balls);
            return false;
        } catch (final ExecutionException e) {
            fallbackResolver.resolve(balls);
            return false;
        }
    }

    /**
     * Resolves all pairwise collisions within a partition in place.
     *
     * @param partition list of balls in the spatial partition
     */
    private void resolvePartition(final List<Ball> partition) {
        for (int i = 0; i < partition.size(); i++) {
            for (int j = i + 1; j < partition.size(); j++) {
                CollisionPairResolver.resolve(partition.get(i), partition.get(j));
            }
        }
    }
}


