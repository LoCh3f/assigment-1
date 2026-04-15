package it.unibo.sampleapp.model.physics.step.common;

import it.unibo.sampleapp.model.domain.ball.Ball;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for splitting balls into fixed-size chunks for concurrent step processing.
 */
public final class PhysicsStepPartitioning {

    private PhysicsStepPartitioning() {
    }

    /**
     * Splits the provided list into ordered sublists of at most chunkSize elements.
     *
     * @param balls input balls list
     * @param chunkSize max number of balls per chunk
     * @return chunked view of the input list
     */
    public static List<List<Ball>> buildChunks(final List<Ball> balls, final int chunkSize) {
        final List<List<Ball>> chunks = new ArrayList<>();
        if (chunkSize <= 0 || balls.isEmpty()) {
            return chunks;
        }
        for (int i = 0; i < balls.size(); i += chunkSize) {
            chunks.add(balls.subList(i, Math.min(i + chunkSize, balls.size())));
        }
        return chunks;
    }
}

