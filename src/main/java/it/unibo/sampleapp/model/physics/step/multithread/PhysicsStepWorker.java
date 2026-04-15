package it.unibo.sampleapp.model.physics.step.multithread;

import it.unibo.sampleapp.model.domain.ball.Ball;
import it.unibo.sampleapp.util.Vector2D;

/**
 * Worker thread that applies per-ball step operations for one chunk at a time.
 */
public final class PhysicsStepWorker extends Thread {
    private static final double FRICTION_COEFFICIENT = 0.1;
    private static final double MIN_SPEED = 0.1;

    private final PhysicsStepBag bag;

    /**
     * Builds a new physics-step worker.
     *
     * @param id worker id used in thread naming
     * @param bag shared monitor
     */
    public PhysicsStepWorker(final int id, final PhysicsStepBag bag) {
        super("physics-step-worker-" + id);
        this.bag = bag;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                final PhysicsStepBag.StepTask task = bag.takeTask();
                applyStep(task);
                bag.markTaskDone();
            } catch (final InterruptedException e) {
                currentThread().interrupt();
            }
        }
    }

    private void applyStep(final PhysicsStepBag.StepTask task) {
        for (final Ball ball : task.partition()) {
            moveBall(ball, task.dt());
            applyFriction(ball, task.dt());
            handleBorderCollision(ball, task.boardW(), task.boardH());
        }
    }

    private void moveBall(final Ball ball, final double dt) {
        ball.setPosition(ball.getPosition().add(ball.getVelocity().scale(dt)));
    }

    private void applyFriction(final Ball ball, final double dt) {
        final Vector2D velocity = ball.getVelocity();
        final double speed = velocity.magnitude();
        if (speed < MIN_SPEED) {
            ball.setVelocity(new Vector2D(0, 0));
            return;
        }
        ball.setVelocity(velocity.scale(Math.max(0, 1.0 - FRICTION_COEFFICIENT * dt)));
    }

    private void handleBorderCollision(final Ball ball, final double boardW, final double boardH) {
        final Vector2D pos = ball.getPosition();
        final Vector2D vel = ball.getVelocity();
        final double radius = ball.getRadius();

        double newVx = vel.x();
        double newVy = vel.y();
        double newPx = pos.x();
        double newPy = pos.y();

        if (newPx - radius < 0) {
            newPx = radius;
            newVx = Math.abs(newVx);
        } else if (newPx + radius > boardW) {
            newPx = boardW - radius;
            newVx = -Math.abs(newVx);
        }

        if (newPy - radius < 0) {
            newPy = radius;
            newVy = Math.abs(newVy);
        } else if (newPy + radius > boardH) {
            newPy = boardH - radius;
            newVy = -Math.abs(newVy);
        }

        ball.setPosition(new Vector2D(newPx, newPy));
        ball.setVelocity(new Vector2D(newVx, newVy));
    }
}

