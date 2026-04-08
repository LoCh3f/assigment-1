package it.unibo.sampleapp.concurrent.taskbased;

import it.unibo.sampleapp.model.GameModel;
import it.unibo.sampleapp.model.ball.Ball;
import it.unibo.sampleapp.model.snapshot.BallSnapshot;
import it.unibo.sampleapp.model.snapshot.GameSnapshot;
import it.unibo.sampleapp.util.Vector2D;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

/**
 * The bot agent using task-based approach with Executor Framework.
 * Runs asynchronously and independently of the game loop.
 * Strategy: Analyzes the board and prioritizes targeting small balls to score points.
 * Falls back to random direction if no clear target is available.
 */
public final class BotTask implements Runnable {

    private static final double MIN_DISTANCE_TO_CONSIDER = 50.0;
    private static final double TARGET_NOISE_AMOUNT = 0.15;
    private static final double DEFENSIVE_NOISE_AMOUNT = 0.2;
    private static final double PI_MULTIPLE = 2.0;
    private static final long BOT_THINK_TIME_MS = 250L;

    private final GameModel model;
    private final Random rng = new Random();
    private final ExecutorService executor;

    /**
     * Constructs a new BotTask with the given model and executor.
     *
     * @param model the game model to control the bot ball
     * @param executor the executor to run on
     */
    public BotTask(final GameModel model, final ExecutorService executor) {
        this.model = model;
        this.executor = executor;
    }

    /**
     * Starts the bot by submitting this task to the executor.
     */
    public void start() {
        executor.execute(this);
    }

    /**
     * Runs the bot behavior, using an intelligent strategy to target small balls
     * when available, or random direction when no good target exists.
     * After applying impulse, resubmits itself to continue.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            final GameSnapshot snapshot = model.getSnapshot();
            final Vector2D direction = findBestMoveDirection(snapshot);
            model.applyImpulseToBot(direction);

            try {
                Thread.sleep(BOT_THINK_TIME_MS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * Analyzes the current board state and determines the best direction to move.
     * Prioritizes small balls that are reachable, otherwise uses random direction.
     *
     * @param snapshot the current game state
     * @return a normalized direction vector for the next move
     */
    private Vector2D findBestMoveDirection(final GameSnapshot snapshot) {
        final List<BallSnapshot> balls = snapshot.balls();

        // Find the bot ball (red/BOT type)
        BallSnapshot botBall = null;
        for (final BallSnapshot ball : balls) {
            if (ball.type() == Ball.Type.BOT) {
                botBall = ball;
                break;
            }
        }

        if (botBall == null) {
            return randomDirection();
        }

        // Find the closest small ball as a target
        BallSnapshot closestSmallBall = null;
        double minDistance = Double.MAX_VALUE;

        for (final BallSnapshot ball : balls) {
            if (ball.type() == Ball.Type.SMALL) {
                final double distance = botBall.position().distance(ball.position());
                if (distance < minDistance && distance >= MIN_DISTANCE_TO_CONSIDER) {
                    minDistance = distance;
                    closestSmallBall = ball;
                }
            }
        }

        // If we found a close small ball, aim toward it
        if (closestSmallBall != null) {
            final Vector2D directionToTarget = closestSmallBall.position()
                    .subtract(botBall.position());
            // Add slight randomness to avoid being too predictable
            return addNoise(directionToTarget, TARGET_NOISE_AMOUNT);
        }

        // Otherwise, aim away from the human ball to keep distance
        BallSnapshot humanBall = null;
        for (final BallSnapshot ball : balls) {
            if (ball.type() == Ball.Type.HUMAN) {
                humanBall = ball;
                break;
            }
        }

        if (humanBall != null) {
            final Vector2D awayFromHuman = botBall.position()
                    .subtract(humanBall.position());
            return addNoise(awayFromHuman, DEFENSIVE_NOISE_AMOUNT);
        }

        // Fallback: random direction
        return randomDirection();
    }

    /**
     * Generates a random direction vector.
     *
     * @return a normalized random direction
     */
    private Vector2D randomDirection() {
        final double angle = rng.nextDouble() * PI_MULTIPLE * Math.PI;
        return new Vector2D(Math.cos(angle), Math.sin(angle));
    }

    /**
     * Adds random noise to a direction vector to make movements less predictable.
     *
     * @param direction the base direction
     * @param noiseAmount the amount of noise (0.0 to 1.0)
     * @return the direction with added noise
     */
    private Vector2D addNoise(final Vector2D direction, final double noiseAmount) {
        final double randomAngle = (rng.nextDouble() - 0.5) * Math.PI * noiseAmount;
        final double cos = Math.cos(randomAngle);
        final double sin = Math.sin(randomAngle);

        // Rotate the direction vector by the random angle
        final double rotatedX = direction.x() * cos - direction.y() * sin;
        final double rotatedY = direction.x() * sin + direction.y() * cos;

        return new Vector2D(rotatedX, rotatedY).normalize();
    }

}
