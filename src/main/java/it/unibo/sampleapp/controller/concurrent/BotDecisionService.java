package it.unibo.sampleapp.controller.concurrent;

import it.unibo.sampleapp.model.ball.Ball;
import it.unibo.sampleapp.model.snapshot.BallSnapshot;
import it.unibo.sampleapp.model.snapshot.GameSnapshot;
import it.unibo.sampleapp.util.Vector2D;

import java.util.List;
import java.util.Random;

import static it.unibo.sampleapp.controller.concurrent.BotAIConstants.DEFENSIVE_NOISE_AMOUNT;
import static it.unibo.sampleapp.controller.concurrent.BotAIConstants.MIN_DISTANCE_TO_CONSIDER;
import static it.unibo.sampleapp.controller.concurrent.BotAIConstants.PI_MULTIPLE;
import static it.unibo.sampleapp.controller.concurrent.BotAIConstants.TARGET_NOISE_AMOUNT;

/**
 * Shared bot decision logic reused by thread-based and task-based bot runners.
 */
public final class BotDecisionService {
    private final Random rng = new Random();

    /**
     * Computes the next movement direction for the bot.
     *
     * @param snapshot the current game snapshot
     * @return a normalized direction vector
     */
    public Vector2D chooseDirection(final GameSnapshot snapshot) {
        final List<BallSnapshot> balls = snapshot.balls();
        final BallSnapshot botBall = findBallOfType(balls, Ball.Type.BOT);

        if (botBall == null) {
            return randomDirection();
        }

        final BallSnapshot closestSmallBall = findClosestSmallBall(balls, botBall);
        if (closestSmallBall != null) {
            final Vector2D directionToTarget = closestSmallBall.position().subtract(botBall.position());
            return addNoise(directionToTarget, TARGET_NOISE_AMOUNT);
        }

        final BallSnapshot humanBall = findBallOfType(balls, Ball.Type.HUMAN);
        if (humanBall != null) {
            final Vector2D awayFromHuman = botBall.position().subtract(humanBall.position());
            return addNoise(awayFromHuman, DEFENSIVE_NOISE_AMOUNT);
        }

        return randomDirection();
    }

    private BallSnapshot findBallOfType(final List<BallSnapshot> balls, final Ball.Type type) {
        for (final BallSnapshot ball : balls) {
            if (ball.type() == type) {
                return ball;
            }
        }
        return null;
    }

    private BallSnapshot findClosestSmallBall(final List<BallSnapshot> balls, final BallSnapshot botBall) {
        BallSnapshot closestSmallBall = null;
        double minDistance = Double.MAX_VALUE;

        for (final BallSnapshot ball : balls) {
            if (ball.type() != Ball.Type.SMALL) {
                continue;
            }
            final double distance = botBall.position().distance(ball.position());
            if (distance < minDistance && distance >= MIN_DISTANCE_TO_CONSIDER) {
                minDistance = distance;
                closestSmallBall = ball;
            }
        }
        return closestSmallBall;
    }

    private Vector2D randomDirection() {
        final double angle = rng.nextDouble() * PI_MULTIPLE * Math.PI;
        return new Vector2D(Math.cos(angle), Math.sin(angle));
    }

    private Vector2D addNoise(final Vector2D direction, final double noiseAmount) {
        final double randomAngle = (rng.nextDouble() - 0.5) * Math.PI * noiseAmount;
        final double cos = Math.cos(randomAngle);
        final double sin = Math.sin(randomAngle);

        final double rotatedX = direction.x() * cos - direction.y() * sin;
        final double rotatedY = direction.x() * sin + direction.y() * cos;

        return new Vector2D(rotatedX, rotatedY).normalize();
    }
}

