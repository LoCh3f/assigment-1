package it.unibo.sampleapp.model.status;

/**
 * Represents the current state of the game.
 */
public enum GameStatus {

    /** The game is currently running. */
    PLAYING,

    /** The human player won (highest score or bot fell in a hole). */
    HUMAN_WINS,

    /** The bot won (highest score or human fell in a hole). */
    BOT_WINS,

    /** The game ended with equal scores. */
    DRAW
}

