package it.unibo.sampleapp.model.snapshot;

import it.unibo.sampleapp.model.domain.ball.Ball;
import it.unibo.sampleapp.util.Vector2D;

/**
 * Immutable Snapshot of a single ball.
 *
 * @param position of the ball.
 * @param radius of the ball.
 * @param type of the ball (Player, Bot, Targets).
 */
public record BallSnapshot(
        Vector2D position,
        double radius,
        Ball.Type type
) {
}
