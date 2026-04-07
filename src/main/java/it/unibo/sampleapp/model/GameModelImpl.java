package it.unibo.sampleapp.model;

import it.unibo.sampleapp.model.ball.Ball;
import it.unibo.sampleapp.model.ball.impl.ImplBall;
import it.unibo.sampleapp.model.hole.Hole;
import it.unibo.sampleapp.model.hole.impl.HoleImpl;
import it.unibo.sampleapp.model.snapshot.BallSnapshot;
import it.unibo.sampleapp.model.snapshot.GameSnapshot;
import it.unibo.sampleapp.model.status.GameStatus;
import it.unibo.sampleapp.util.physics.PhysicsEngine;
import it.unibo.sampleapp.util.Vector2D;

import java.util.ArrayList;
import java.util.List;

/**
 * The game monitor. Single point of truth for all mutable game state.
 * Every public method is synchronized — this is the custom monitor
 * required by the assignment (no library support).
 *
 * <p>
 * Thread interaction:
 *  - GameLoopThread → calls applyPhysicsStep() every tick
 *  - Swing EDT → calls applyImpulseToHuman() on key press
 *  - BotThread → calls applyImpulseToBot() on its own schedule
 *  - View → calls getSnapshot() each repaint
 */
public final class GameModelImpl implements GameModel {
    /**
     * The turn enum.
     */
    public enum Turn { HUMAN, BOT }

    private static final double IMPULSE_STRENGTH = 200.0;
    private static final double PLAYER_BALL_RADIUS = 15.0;
    private static final double SMALL_BALL_RADIUS = 7.0;
    private static final double HOLE_RADIUS = 18.0;
    private static final double HUMAN_BALL_X_RATIO = 0.25;
    private static final double HUMAN_BALL_Y_RATIO = 0.75;
    private static final double BOT_BALL_X_RATIO = 0.75;
    private static final double BOT_BALL_Y_RATIO = 0.75;
    private static final double TOP_HALF_RATIO = 0.5;
    private static final double STOP_THRESHOLD = 5.0; // must match

    private Turn currentTurn = Turn.HUMAN;  // human starts

    private final List<Ball> balls;
    private final List<Hole> holes;
    private final Ball humanBall;
    private final Ball botBall;
    private final int boardWidth;
    private final int boardHeight;

    private int humanScore;
    private int botScore;
    private GameStatus status;

    private final PhysicsEngine physicsEngine;

    /**
     * Constructs a new GameModelImpl with the given board dimensions and number of small balls.
     *
     * @param boardWidth the width of the game board
     * @param boardHeight the height of the game board
     * @param numSmallBalls the number of small balls to spawn
     */
    public GameModelImpl(final int boardWidth, final int boardHeight, final int numSmallBalls) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.physicsEngine = new PhysicsEngine();
        this.balls = new ArrayList<>();
        this.holes = buildHoles();

        // Place human ball (bottom-left area) and bot ball (bottom-right area)
        this.humanBall = new ImplBall(
                new Vector2D(boardWidth * HUMAN_BALL_X_RATIO, boardHeight * HUMAN_BALL_Y_RATIO),
                new Vector2D(0, 0),
                PLAYER_BALL_RADIUS,
                Ball.Type.HUMAN
        );
        this.botBall = new ImplBall(
                new Vector2D(boardWidth * BOT_BALL_X_RATIO, boardHeight * BOT_BALL_Y_RATIO),
                new Vector2D(0, 0),
                PLAYER_BALL_RADIUS,
                Ball.Type.BOT
        );

        balls.add(humanBall);
        balls.add(botBall);
        spawnSmallBalls(numSmallBalls);

        this.humanScore = 0;
        this.botScore = 0;
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

        final List<Ball> pocketed = physicsEngine.step(balls, boardWidth, boardHeight, holes, dt);
        handlePocketedBalls(pocketed);
        checkWinCondition();

        // Asynchronous play: no turn switching, just wake up waiting players
        notifyAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void applyImpulseToHuman(final Vector2D direction) {
        // Asynchronous play: allow human to move whenever balls are stopped
        try {
            while (status == GameStatus.PLAYING && !allBallsStopped()) {
                wait();
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        if (status != GameStatus.PLAYING) {
            return;
        }

        humanBall.setVelocity(
                humanBall.getVelocity().add(direction.normalize().scale(IMPULSE_STRENGTH))
        );
    }

    @Override
    public synchronized void applyImpulseToBot(final Vector2D direction) {
        // Asynchronous play: allow bot to move whenever balls are stopped
        try {
            while (status == GameStatus.PLAYING && !allBallsStopped()) {
                wait();
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        if (status != GameStatus.PLAYING) {
            return;
        }

        botBall.setVelocity(
                botBall.getVelocity().add(direction.normalize().scale(IMPULSE_STRENGTH))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized GameSnapshot getSnapshot() {
        final List<BallSnapshot> snapshots = balls.stream()
                .map(b -> new BallSnapshot(b.getPosition(), b.getRadius(), b.getType()))
                .toList();
        // Convert internal Turn enum to snapshot Turn enum
        final GameSnapshot.Turn snapshotTurn = (currentTurn == Turn.HUMAN)
                ? GameSnapshot.Turn.HUMAN
                : GameSnapshot.Turn.BOT;
        return new GameSnapshot(snapshots,
                humanScore, botScore,
                status, List.copyOf(holes), boardWidth, boardHeight, snapshotTurn);
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
            switch (b.getType()) {
                case HUMAN -> {
                    // Human player's ball pocketed → Bot wins
                    status = GameStatus.BOT_WINS;
                    notifyAll();
                    return;
                }
                case BOT -> {
                    // Bot player's ball pocketed → Human wins
                    status = GameStatus.HUMAN_WINS;
                    notifyAll();
                    return;
                }
                case SMALL -> {
                    // Get the ball type that last collided with this small ball
                    final Ball.Type lastCollidedWith = getLastCollisionType(b);

                    // Only score if the small ball was directly hit by a player ball
                    if (lastCollidedWith == Ball.Type.HUMAN) {
                        humanScore++;
                        if (humanScore > (balls.size() - 2) / 2) {
                            status = GameStatus.HUMAN_WINS;
                        }
                    } else if (lastCollidedWith == Ball.Type.BOT) {
                        botScore++;
                        if (botScore > (balls.size() - 2) / 2) {
                            status = GameStatus.BOT_WINS;
                        }
                    }
                    // If lastCollidedWith is SMALL or null, no score is awarded
                    notifyAll();
                }
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
        if (b instanceof ImplBall implBall) {
            return implBall.getLastCollidedWithType();
        }
        return null;
    }

    /**
     * Checks if the board is empty (all small balls pocketed).
     * If so, decides the winner by score.
     */
    private void checkWinCondition() {
        final boolean noSmallBallsLeft = balls.stream()
                .noneMatch(b -> b.getType() == Ball.Type.SMALL);

        if (noSmallBallsLeft) {
            if (humanScore > botScore) {
                status = GameStatus.HUMAN_WINS;
            } else if (botScore > humanScore) {
                status = GameStatus.BOT_WINS;
            } else {
                status = GameStatus.DRAW;
            }
            notifyAll();
        }
    }

    /**
     * Two holes at the top corners, as described in the assignment.
     *
     * @return list containing the two holes
     */
    private List<Hole> buildHoles() {
        return List.of(
                new HoleImpl(new Vector2D(HOLE_RADIUS, HOLE_RADIUS), HOLE_RADIUS),
                new HoleImpl(new Vector2D(boardWidth - HOLE_RADIUS, HOLE_RADIUS), HOLE_RADIUS)
        );
    }

    private void spawnSmallBalls(final int count) {
        for (int i = 0; i < count; i++) {
            final double x = SMALL_BALL_RADIUS + Math.random() * (boardWidth - 2 * SMALL_BALL_RADIUS);
            final double y = SMALL_BALL_RADIUS + Math.random() * boardHeight * TOP_HALF_RATIO;
            balls.add(new ImplBall(
                    new Vector2D(x, y),
                    new Vector2D(0, 0),
                    SMALL_BALL_RADIUS,
                    Ball.Type.SMALL
            ));
        }
    }

    private boolean allBallsStopped() {
        return balls.stream()
                .filter(b -> b.getType() == Ball.Type.BOT || b.getType() == Ball.Type.HUMAN)
                .allMatch(b -> b.getVelocity().magnitude() <= STOP_THRESHOLD);
    }
}
