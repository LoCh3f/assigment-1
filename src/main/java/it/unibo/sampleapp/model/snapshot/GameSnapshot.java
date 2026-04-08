package it.unibo.sampleapp.model.snapshot;

import it.unibo.sampleapp.model.status.GameStatus;
import it.unibo.sampleapp.model.hole.Hole;

import java.util.Collections;
import java.util.List;

/**
 * Immutable snapshot of the game state.
 *
 * @param balls        list of ball snapshots currently on the board
 * @param humanScore   the human player's current score
 * @param botScore     the bot player's current score
 * @param status       the current game status
 * @param holes        list of holes on the board
 * @param width        the width of the game board
 * @param height       the height of the game board
 */
public record GameSnapshot(
        List<BallSnapshot> balls,
        int humanScore,
        int botScore,
        GameStatus status,
        List<Hole> holes,
        int width,
        int height
) {

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
