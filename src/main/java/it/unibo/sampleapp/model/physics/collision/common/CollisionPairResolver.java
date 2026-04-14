package it.unibo.sampleapp.model.physics.collision.common;

import it.unibo.sampleapp.model.domain.ball.Ball;
import it.unibo.sampleapp.util.Vector2D;

/**
 * Utility for resolving a single pairwise collision.
 */
public final class CollisionPairResolver {
    private static final double EPSILON = 0.01;

    private CollisionPairResolver() {
    }

    /**
     * Resolves one elastic collision if the two balls overlap and approach each other.
     *
     * @param a first ball
     * @param b second ball
     */
    public static void resolve(final Ball a, final Ball b) {

        Vector2D posA = a.getPosition();
        Vector2D posB = b.getPosition();

        final Vector2D delta = posB.subtract(posA);
        final double dist = delta.magnitude();
        final double minDist = a.getRadius() + b.getRadius();

        if (dist == 0 || dist >= minDist) {
            return;
        }

        final double overlap = minDist - dist + EPSILON;
        final Vector2D n = delta.scale(1.0 / dist);

        posA = posA.subtract(n.scale(overlap / 2));
        posB = posB.add(n.scale(overlap / 2));
        a.setPosition(posA);
        b.setPosition(posB);

        final Vector2D vA = a.getVelocity();
        final Vector2D vB = b.getVelocity();
        final Vector2D t = new Vector2D(-n.y(), n.x());

        final double normalA = vA.dot(n);
        final double tangentA = vA.dot(t);
        final double normalB = vB.dot(n);
        final double tangentB = vB.dot(t);

        if (normalA - normalB <= 0) {
            return;
        }

        final double normalAfterA = normalB;
        final double normalAfterB = normalA;

        a.setVelocity(n.scale(normalAfterA).add(t.scale(tangentA)));
        b.setVelocity(n.scale(normalAfterB).add(t.scale(tangentB)));

        if (a.getType() == Ball.Type.SMALL && b.getType() != Ball.Type.SMALL) {
            a.recordCollision(b.getType());
        }
        if (b.getType() == Ball.Type.SMALL && a.getType() != Ball.Type.SMALL) {
            b.recordCollision(a.getType());
        }
    }
}


