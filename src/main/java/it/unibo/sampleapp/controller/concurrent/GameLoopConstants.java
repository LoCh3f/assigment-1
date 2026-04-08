package it.unibo.sampleapp.controller.concurrent;

/**
 * Shared constants for game loop timing across concurrency implementations.
 */
public final class GameLoopConstants {
    public static final long TARGET_FPS = 60;
    public static final long TICK_MS = 1000 / TARGET_FPS;
    public static final double TICK_S = TICK_MS / 1000.0;

    private GameLoopConstants() {
    }
}

