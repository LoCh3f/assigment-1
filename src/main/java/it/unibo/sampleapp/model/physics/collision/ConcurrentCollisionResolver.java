package it.unibo.sampleapp.model.physics.collision;

import it.unibo.sampleapp.model.ball.Ball;
import it.unibo.sampleapp.model.physics.concurrent.CollisionBag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Concurrent collision resolver backed by {@link CollisionBag} and worker threads.
 */
public final class ConcurrentCollisionResolver implements CollisionResolver {
    private static final int MIN_BALLS_FOR_CONCURRENT_COLLISIONS = 24;
    private static final int BALLS_PER_PARTITION = 12;

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
        if (balls.size() < MIN_BALLS_FOR_CONCURRENT_COLLISIONS) {
            fallbackResolver.resolve(balls);
            return;
        }

        final int nPartitions = Math.max(2, balls.size() / BALLS_PER_PARTITION);
        final List<Integer> sortedIds = new ArrayList<>(balls.size());
        for (int i = 0; i < balls.size(); i++) {
            sortedIds.add(i);
        }
        sortedIds.sort(Comparator.comparingDouble(i -> balls.get(i).getPosition().x()));

        final int chunk = Math.max(1, (int) Math.ceil((double) balls.size() / nPartitions));
        final List<List<Ball>> partitions = new ArrayList<>();

        for (int start = 0; start < sortedIds.size(); start += chunk) {
            final int end = Math.min(sortedIds.size(), start + chunk);
            final List<Ball> refs = new ArrayList<>(end - start);
            for (int i = start; i < end; i++) {
                refs.add(balls.get(sortedIds.get(i)));
            }
            partitions.add(refs);
        }

        try {
            bag.submitPartitions(partitions);
            bag.awaitCompletion();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fallbackResolver.resolve(balls);
            return;
        }

        // Reconcile interactions at partition boundaries (cross-partition pairs).
        for (int p = 0; p < partitions.size() - 1; p++) {
            final List<Ball> left = partitions.get(p);
            final List<Ball> right = partitions.get(p + 1);
            for (final Ball a : left) {
                for (final Ball b : right) {
                    final double maxDx = a.getRadius() + b.getRadius();
                    final double dx = Math.abs(a.getPosition().x() - b.getPosition().x());
                    if (dx <= maxDx) {
                        CollisionPairResolver.resolve(a, b);
                    }
                }
            }
        }
    }
}

