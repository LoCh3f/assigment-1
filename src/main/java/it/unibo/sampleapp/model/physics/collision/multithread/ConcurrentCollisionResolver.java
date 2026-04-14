package it.unibo.sampleapp.model.physics.collision.multithread;

import it.unibo.sampleapp.model.domain.ball.Ball;
import it.unibo.sampleapp.model.physics.collision.CollisionResolver;
import it.unibo.sampleapp.model.physics.collision.common.CollisionPartitioning;
import it.unibo.sampleapp.model.physics.collision.common.CollisionResolverConstants;

import java.util.List;

/**
 * Concurrent collision resolver backed by {@link CollisionBag} and worker threads.
 */
public final class ConcurrentCollisionResolver implements CollisionResolver {
    private final CollisionBag bag;
    private final CollisionResolver fallbackResolver;

    /**
     * Builds a concurrent resolver.
     *
     * @param bag shared monitor used by workers
     * @param fallbackResolver resolver used for small inputs or interruption fallback
     */
    public ConcurrentCollisionResolver(final CollisionBag bag, final CollisionResolver fallbackResolver) {
        this.bag = bag;
        this.fallbackResolver = fallbackResolver;
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

        try {
            bag.submitPartitions(partitions);
            bag.awaitCompletion();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fallbackResolver.resolve(balls);
            return;
        }

        CollisionPartitioning.resolveCrossPartitionBoundaries(partitions);
    }
}


