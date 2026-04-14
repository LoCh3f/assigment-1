package it.unibo.sampleapp.model.physics.collision.sequential;

import it.unibo.sampleapp.model.domain.ball.Ball;
import it.unibo.sampleapp.model.physics.collision.CollisionResolver;
import it.unibo.sampleapp.model.physics.collision.common.CollisionPairResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sequential collision resolver using a uniform spatial grid broad phase.
 */
public final class SequentialCollisionResolver implements CollisionResolver {
    private static final double CELL_SIZE = 40.0;
    private static final long LOWER_32_MASK = 0xffffffffL;

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolve(final List<Ball> balls) {
        final Map<Long, List<Ball>> grid = new HashMap<>();

        for (final Ball b : balls) {
            final int cx = (int) (b.getPosition().x() / CELL_SIZE);
            final int cy = (int) (b.getPosition().y() / CELL_SIZE);
            final long key = (((long) cx) << 32) | (cy & LOWER_32_MASK);
            grid.computeIfAbsent(key, k -> new ArrayList<>()).add(b);
        }

        for (final Map.Entry<Long, List<Ball>> entry : grid.entrySet()) {
            final long key = entry.getKey();
            final int cx = (int) (key >> 32);
            final int cy = (int) key;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    final long neighbourKey = (((long) (cx + dx)) << 32) | ((cy + dy) & LOWER_32_MASK);
                    final List<Ball> cellBalls = entry.getValue();
                    final List<Ball> neighbourBalls = grid.get(neighbourKey);
                    if (neighbourBalls == null) {
                        continue;
                    }

                    if (neighbourKey == key) {
                        for (int i = 0; i < cellBalls.size(); i++) {
                            for (int j = i + 1; j < cellBalls.size(); j++) {
                                CollisionPairResolver.resolve(cellBalls.get(i), cellBalls.get(j));
                            }
                        }
                    } else if (neighbourKey > key) {
                        for (final Ball a : cellBalls) {
                            for (final Ball b : neighbourBalls) {
                                CollisionPairResolver.resolve(a, b);
                            }
                        }
                    }
                }
            }
        }
    }
}


