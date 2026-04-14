package it.unibo.sampleapp.controller.concurrent.bot;

/**
 * Shared bot AI tuning constants used by both bot implementations.
 */
public final class BotAIConstants {
    public static final double MIN_DISTANCE_TO_CONSIDER = 50.0;
    public static final double TARGET_NOISE_AMOUNT = 0.15;
    public static final double DEFENSIVE_NOISE_AMOUNT = 0.2;
    public static final double PI_MULTIPLE = 2.0;
    public static final long BOT_THINK_TIME_MS = 250L;
    public static final long BOT_MOVE_DELAY_MS = 3500L;
    public static final long NANOS_PER_MILLIS = 1_000_000L;

    private BotAIConstants() {
    }
}


