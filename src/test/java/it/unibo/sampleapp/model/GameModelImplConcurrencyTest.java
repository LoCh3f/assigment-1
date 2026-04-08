package it.unibo.sampleapp.model;

import it.unibo.sampleapp.util.Vector2D;
import it.unibo.sampleapp.model.status.GameStatus;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression tests for concurrent human/bot impulses.
 */
class GameModelImplConcurrencyTest {

    private static final int TIMEOUT_MILLISECONDS = 300;

    @Test
    void botImpulseDoesNotWaitForHumanBallToStop() {
        final GameModel model = new GameModel(900, 600, 0);
        model.applyImpulseToHuman(new Vector2D(1, 0));

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            final Future<?> future = executor.submit(() -> model.applyImpulseToBot(new Vector2D(-1, 0)));

            assertDoesNotThrow(() -> future.get(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS));
            assertEquals(GameStatus.PLAYING, model.getStatus());
        }
    }
}



