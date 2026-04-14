package it.unibo.sampleapp.model.physics.collision.common;

import it.unibo.sampleapp.model.domain.ball.Ball;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Shared partitioning logic for parallel collision resolvers.
 */
public final class CollisionPartitioning {

    private CollisionPartitioning() {
    }

    /**
     * Splits balls into X-axis partitions used by parallel resolvers.
     *
     * @param balls balls to partition
     * @param ballsPerPartition target amount of balls for each partition
     * @return partition list preserving shared ball references
     */
    public static List<List<Ball>> buildByXAxis(final List<Ball> balls, final int ballsPerPartition) {
        final int nPartitions = Math.max(2, balls.size() / ballsPerPartition);
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
        return partitions;
    }

    /**
     * Resolves collisions between adjacent partition boundaries.
     *
     * @param partitions contiguous partitions produced by {@link #buildByXAxis(List, int)}
     */
    public static void resolveCrossPartitionBoundaries(final List<List<Ball>> partitions) {
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




