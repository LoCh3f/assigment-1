package it.unibo.sampleapp.model.snapshot;

import it.unibo.sampleapp.model.status.GameStatus;
import it.unibo.sampleapp.model.hole.Hole;

import java.util.Collections;
import java.util.List;

/**
 * Immutable snapshot of the game state.
 */
public record GameSnapshot(
        List<BallSnapshot> balls,
        int humanScore,
        int botScore,
        GameStatus status,
        List<Hole> holes,
        int width,
        int height,
        Turn currentTurn
) {
    /**
     * Turn indicator enum.
     */
    public enum Turn { HUMAN, BOT }

    /**
     * Compact constructor that creates defensive copies of mutable lists.
     */
    public GameSnapshot {
        balls = List.copyOf(balls);
        holes = List.copyOf(holes);
    }

    /**
     * Returns an unmodifiable view of the balls list.
     *
     * @return unmodifiable list of balls
     */
    @Override
    public List<BallSnapshot> balls() {
        return Collections.unmodifiableList(balls);
    }

    /**
     * Returns an unmodifiable view of the holes list.
     *
     * @return unmodifiable list of holes
     */
    @Override
    public List<Hole> holes() {
        return Collections.unmodifiableList(holes);
    }
}
