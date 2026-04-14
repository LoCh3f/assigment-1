package it.unibo.sampleapp.model;

import it.unibo.sampleapp.model.domain.ball.Ball;
import it.unibo.sampleapp.model.snapshot.GameSnapshot;
import it.unibo.sampleapp.model.state.BoardState;
import it.unibo.sampleapp.model.status.GameStatus;
import it.unibo.sampleapp.model.physics.PhysicsEngine;
import it.unibo.sampleapp.model.rules.ScoreBoard;
import it.unibo.sampleapp.util.Vector2D;

import java.util.List;

import static it.unibo.sampleapp.model.constants.GameModelConstants.IMPULSE_STRENGTH;

/**
 * Base game monitor. Single point of truth for all mutable game state.
 * Every public method is synchronized — this is the custom monitor
 * required by the assignment (no library support).
 *
 * <p>
 * Thread interaction:
 *  - GameLoopThread → calls applyPhysicsStep() every tick
 *  - Swing EDT → calls applyImpulseToHuman() on key press or mouse release
 *  - BotThread → calls applyImpulseToBot() on its own schedule
 *  - View → calls getSnapshot() each repaint
 */
public class GameModel implements Model {
    private final BoardState boardState;
    private final ScoreBoard scoreBoard;
    private GameStatus status;

    private final PhysicsEngine physicsEngine;

    /**
     * Builds the shared model state while delegating collision strategy to subclasses.
     *
     * @param boardWidth board width
     * @param boardHeight board height
     * @param numSmallBalls number of small balls to spawn
     * @param physicsEngine concrete physics engine to use
     */
    protected GameModel(final int boardWidth, final int boardHeight, final int numSmallBalls,
                        final PhysicsEngine physicsEngine) {
        final int totalSmallBalls = Math.max(0, numSmallBalls);
        this.boardState = new BoardState(boardWidth, boardHeight, totalSmallBalls);
        this.scoreBoard = new ScoreBoard(totalSmallBalls);
        this.physicsEngine = physicsEngine;
        this.status = GameStatus.PLAYING;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void applyPhysicsStep(final double dt) {
        if (status != GameStatus.PLAYING) {
            return;
        }

        final List<Ball> pocketed = boardState.applyPhysicsStep(physicsEngine, dt);
        handlePocketedBalls(pocketed);
        checkWinCondition();

        notifyAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void applyImpulseToHuman(final Vector2D direction) {
        if (status != GameStatus.PLAYING) {
            return;
        }

        boardState.applyImpulseToHuman(direction.normalize().scale(IMPULSE_STRENGTH));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void applyImpulseToBot(final Vector2D direction) {
        if (status != GameStatus.PLAYING) {
            return;
        }

        boardState.applyImpulseToBot(direction.normalize().scale(IMPULSE_STRENGTH));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized GameSnapshot getSnapshot() {
        return boardState.toSnapshot(status, scoreBoard.getHumanScore(), scoreBoard.getBotScore());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized GameStatus getStatus() {
        return status;
    }

    /**
     * Blocking wait — callers sleep until the game ends.
     * Uses wait()/notifyAll() — the custom monitor pattern.
     * Called by BotThread to wait for game over before terminating.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public synchronized void waitUntilGameOver() throws InterruptedException {
        while (status == GameStatus.PLAYING) {
            wait();  // releases lock and sleeps
        }
    }

    /**
     * Handles balls that fell into holes this step.
     * Player balls → immediate game over (opponent wins).
     * Small balls → score the player whose ball actually caused the collision.
     * If a small ball was hit by another small ball, no score is awarded.
     *
     * @param pocketed the list of balls that fell into holes
     */
    private void handlePocketedBalls(final List<Ball> pocketed) {
        for (final Ball b : pocketed) {
            final GameStatus immediateWinner = scoreBoard.winnerForPocketedPlayerBall(b.getType());
            if (immediateWinner != GameStatus.PLAYING) {
                status = immediateWinner;
                notifyAll();
                return;
            }

            if (b.getType() == Ball.Type.SMALL) {
                scoreBoard.assignPointForSmallBall(getLastCollisionType(b));
                final GameStatus majorityWinner = scoreBoard.winnerByMajority();
                if (majorityWinner != GameStatus.PLAYING) {
                    status = majorityWinner;
                }
                notifyAll();
            }
        }
    }

    /**
     * Retrieves the type of ball that last collided with a given ball.
     * Used for determining scoring when small balls are pocketed.
     *
     * @param b the ball to check
     * @return the type of the last colliding ball, or null if tracking is unavailable
     */
    private Ball.Type getLastCollisionType(final Ball b) {
        return b.getLastCollidedWithType();
    }

    /**
     * Checks if the board is empty (all small balls pocketed).
     * If so, decides the winner by score.
     */
    private void checkWinCondition() {
        if (boardState.hasNoSmallBallsLeft() && status == GameStatus.PLAYING) {
            status = scoreBoard.winnerWhenBoardIsCleared();
            notifyAll();
        }
    }
}
