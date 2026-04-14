package it.unibo.sampleapp.model.constants;

import it.unibo.sampleapp.model.GameModel;

/**
 * Constants used by {@link GameModel}.
 */
public final class GameModelConstants {
    public static final double IMPULSE_STRENGTH = 200.0;
    public static final double PLAYER_BALL_RADIUS = 18.0;
    public static final double SMALL_BALL_RADIUS = 6.0;
    public static final double HOLE_RADIUS = 18.0;

    public static final double HUMAN_BALL_X_RATIO = 0.25;
    public static final double HUMAN_BALL_Y_RATIO = 0.75;
    public static final double BOT_BALL_X_RATIO = 0.75;
    public static final double BOT_BALL_Y_RATIO = 0.75;
    public static final double TOP_HALF_RATIO = 0.5;
    public static final double SPAWN_CLEARANCE = 0.8;
    public static final int MAX_SPAWN_ATTEMPTS_PER_BALL = 16;

    private GameModelConstants() {
    }
}

