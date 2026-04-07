package it.unibo.sampleapp.model;

import it.unibo.sampleapp.model.ball.Ball;
import it.unibo.sampleapp.model.ball.impl.ImplBall;
import it.unibo.sampleapp.model.hole.Hole;
import it.unibo.sampleapp.model.hole.impl.HoleImpl;
import it.unibo.sampleapp.model.snapshot.BallSnapshot;
import it.unibo.sampleapp.model.snapshot.GameSnapshot;
import it.unibo.sampleapp.model.status.GameStatus;
import it.unibo.sampleapp.physics.PhysicsEngine;
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
    private static final double STOP_THRESHOLD = 2.0; // must match

    private Turn currentTurn = Turn.HUMAN;  // human starts

    // -----------------------------------------------------------------------
    // State — only accessed inside synchronized methods
    // -----------------------------------------------------------------------

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

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

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

    // -----------------------------------------------------------------------
    // ModelInterface — synchronized public API
    // -----------------------------------------------------------------------

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

        // If game still playing and all balls have stopped → switch turn
        if (status == GameStatus.PLAYING && allBallsStopped()) {
            currentTurn = (currentTurn == Turn.HUMAN) ? Turn.BOT : Turn.HUMAN;
            notifyAll(); // wake up whoever is waiting for their turn
        } else {
            // still wake up waiters so they can re-check ball speeds
            notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void applyImpulseToHuman(final Vector2D direction) {
        try {
            while (status == GameStatus.PLAYING
                    && (currentTurn != Turn.HUMAN || !allBallsStopped())) {
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
        try {
            while (status == GameStatus.PLAYING
                    && (currentTurn != Turn.BOT || !allBallsStopped())) {
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
        return new GameSnapshot(snapshots,
                humanScore, botScore,
                status, List.copyOf(holes), boardWidth, boardHeight);
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

    // -----------------------------------------------------------------------
    // Private logic — called only inside synchronized methods
    // -----------------------------------------------------------------------

    /**
     * Handles balls that fell into holes this step.
     * Player balls → immediate game over.
     * Small balls → score the player who last touched them (simplified: random).
     *
     * @param pocketed the list of balls that fell into holes
     */
    private void handlePocketedBalls(final List<Ball> pocketed) {
        for (final Ball b : pocketed) {
            switch (b.getType()) {
                case HUMAN -> {
                    status = GameStatus.BOT_WINS;
                    notifyAll();
                    return;
                }
                case BOT -> {
                    status = GameStatus.HUMAN_WINS;
                    notifyAll();
                    return;
                }
                case SMALL -> {
                    // Attribution: the last player to touch a small ball scores.
                    // Simplified here — assign to the player whose ball is closest to the hole.
                    // Replace with proper "last-touch" tracking if desired.
                    scoreNearestPlayer(b);
                }
            }
        }
    }

    private void scoreNearestPlayer(final Ball smallBall) {
        final double distHuman = smallBall.getPosition()
                .subtract(humanBall.getPosition()).magnitude();
        final double distBot = smallBall.getPosition()
                .subtract(botBall.getPosition()).magnitude();
        if (distHuman < distBot) {
            humanScore++;
        } else {
            botScore++;
        }
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

    // -----------------------------------------------------------------------
    // Setup helpers
    // -----------------------------------------------------------------------

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
        return balls.stream().allMatch(
                b -> b.getVelocity().magnitude() <= STOP_THRESHOLD
        );
    }
}
