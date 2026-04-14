package it.unibo.sampleapp.model.state;

import it.unibo.sampleapp.model.domain.ball.Ball;
import it.unibo.sampleapp.model.domain.ball.impl.ImplBall;
import it.unibo.sampleapp.model.domain.hole.Hole;
import it.unibo.sampleapp.model.domain.hole.impl.HoleImpl;
import it.unibo.sampleapp.model.physics.PhysicsEngine;
import it.unibo.sampleapp.model.snapshot.BallSnapshot;
import it.unibo.sampleapp.model.snapshot.GameSnapshot;
import it.unibo.sampleapp.model.status.GameStatus;
import it.unibo.sampleapp.util.Vector2D;

import java.util.ArrayList;
import java.util.List;

import static it.unibo.sampleapp.model.constants.GameModelConstants.BOT_BALL_X_RATIO;
import static it.unibo.sampleapp.model.constants.GameModelConstants.BOT_BALL_Y_RATIO;
import static it.unibo.sampleapp.model.constants.GameModelConstants.HOLE_RADIUS;
import static it.unibo.sampleapp.model.constants.GameModelConstants.HUMAN_BALL_X_RATIO;
import static it.unibo.sampleapp.model.constants.GameModelConstants.HUMAN_BALL_Y_RATIO;
import static it.unibo.sampleapp.model.constants.GameModelConstants.MAX_SPAWN_ATTEMPTS_PER_BALL;
import static it.unibo.sampleapp.model.constants.GameModelConstants.PLAYER_BALL_RADIUS;
import static it.unibo.sampleapp.model.constants.GameModelConstants.SPAWN_CLEARANCE;
import static it.unibo.sampleapp.model.constants.GameModelConstants.SMALL_BALL_RADIUS;
import static it.unibo.sampleapp.model.constants.GameModelConstants.TOP_HALF_RATIO;

/**
 * Owns board dimensions and mutable ball placement/state.
 */
public final class BoardState {

    private final List<Ball> balls;
    private final List<Hole> holes;
    private final Ball humanBall;
    private final Ball botBall;
    private final int width;
    private final int height;

    /**
     * Creates the board state and spawns all balls.
     *
     * @param width board width
     * @param height board height
     * @param totalSmallBalls number of small balls to spawn
     */
    public BoardState(final int width, final int height, final int totalSmallBalls) {
        this.width = width;
        this.height = height;
        this.balls = new ArrayList<>();
        this.holes = buildHoles();

        this.humanBall = new ImplBall(
                new Vector2D(width * HUMAN_BALL_X_RATIO, height * HUMAN_BALL_Y_RATIO),
                new Vector2D(0, 0),
                PLAYER_BALL_RADIUS,
                Ball.Type.HUMAN
        );
        this.botBall = new ImplBall(
                new Vector2D(width * BOT_BALL_X_RATIO, height * BOT_BALL_Y_RATIO),
                new Vector2D(0, 0),
                PLAYER_BALL_RADIUS,
                Ball.Type.BOT
        );

        balls.add(humanBall);
        balls.add(botBall);
        spawnSmallBalls(totalSmallBalls);
    }

    /**
     * Advances board physics one step.
     *
     * @param physicsEngine physics engine instance
     * @param dt step duration in seconds
     * @return list of balls pocketed during this step
     */
    public List<Ball> applyPhysicsStep(final PhysicsEngine physicsEngine, final double dt) {
        return physicsEngine.step(balls, width, height, holes, dt);
    }

    /**
     * Applies an impulse to the human-controlled ball.
     *
     * @param impulse impulse vector to add to velocity
     */
    public void applyImpulseToHuman(final Vector2D impulse) {
        humanBall.setVelocity(humanBall.getVelocity().add(impulse));
    }

    /**
     * Applies an impulse to the bot-controlled ball.
     *
     * @param impulse impulse vector to add to velocity
     */
    public void applyImpulseToBot(final Vector2D impulse) {
        botBall.setVelocity(botBall.getVelocity().add(impulse));
    }

    /**
     * Returns true when all small balls have been pocketed.
     *
     * @return true when no small ball remains on board
     */
    public boolean hasNoSmallBallsLeft() {
        return balls.stream().noneMatch(ball -> ball.getType() == Ball.Type.SMALL);
    }

    /**
     * Creates an immutable snapshot for rendering.
     *
     * @param status current game status
     * @param humanScore current human score
     * @param botScore current bot score
     * @return immutable snapshot of current board state
     */
    public GameSnapshot toSnapshot(final GameStatus status, final int humanScore, final int botScore) {
        final List<BallSnapshot> snapshots = balls.stream()
                .map(ball -> new BallSnapshot(ball.getPosition(), ball.getRadius(), ball.getType()))
                .toList();
        return new GameSnapshot(snapshots, humanScore, botScore, status, List.copyOf(holes), width, height);
    }

    private List<Hole> buildHoles() {
        return List.of(
                new HoleImpl(new Vector2D(HOLE_RADIUS, HOLE_RADIUS), HOLE_RADIUS),
                new HoleImpl(new Vector2D(width - HOLE_RADIUS, HOLE_RADIUS), HOLE_RADIUS)
        );
    }

    private void spawnSmallBalls(final int count) {
        for (int i = 0; i < count; i++) {
            boolean placed = false;
            for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS_PER_BALL && !placed; attempt++) {
                final double x = SMALL_BALL_RADIUS + Math.random() * (width - 2 * SMALL_BALL_RADIUS);
                final double y = SMALL_BALL_RADIUS + Math.random() * height * TOP_HALF_RATIO;
                final Vector2D candidate = new Vector2D(x, y);
                if (isPositionFree(candidate)) {
                    balls.add(new ImplBall(candidate, new Vector2D(0, 0), SMALL_BALL_RADIUS, Ball.Type.SMALL));
                    placed = true;
                }
            }
            if (!placed) {
                final double x = SMALL_BALL_RADIUS + Math.random() * (width - 2 * SMALL_BALL_RADIUS);
                final double y = SMALL_BALL_RADIUS + Math.random() * height * TOP_HALF_RATIO;
                balls.add(new ImplBall(new Vector2D(x, y), new Vector2D(0, 0), SMALL_BALL_RADIUS, Ball.Type.SMALL));
            }
        }
    }

    private boolean isPositionFree(final Vector2D candidate) {
        for (final Ball existing : balls) {
            final double minDistance = SMALL_BALL_RADIUS + existing.getRadius() + SPAWN_CLEARANCE;
            if (existing.getPosition().distance(candidate) < minDistance) {
                return false;
            }
        }
        return true;
    }
}



