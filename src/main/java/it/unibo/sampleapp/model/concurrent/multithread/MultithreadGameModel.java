package it.unibo.sampleapp.model.concurrent.multithread;

import it.unibo.sampleapp.model.GameModel;
import it.unibo.sampleapp.model.physics.PhysicsEngine;
import it.unibo.sampleapp.model.physics.collision.multithread.CollisionBag;
import it.unibo.sampleapp.model.physics.collision.multithread.CollisionWorker;
import it.unibo.sampleapp.model.physics.step.multithread.PhysicsStepBag;
import it.unibo.sampleapp.model.physics.step.multithread.PhysicsStepWorker;

/**
 * Game model configured with multithread collision workers.
 */
public final class MultithreadGameModel extends GameModel {

    /**
     * Creates a model configured with worker-based collision resolution.
     *
     * @param boardWidth board width
     * @param boardHeight board height
     * @param numSmallBalls number of small balls
     */
    public MultithreadGameModel(final int boardWidth, final int boardHeight, final int numSmallBalls) {
        super(boardWidth, boardHeight, numSmallBalls, createPhysicsEngine());
    }

    private static PhysicsEngine createPhysicsEngine() {
        final CollisionBag collisionBag = new CollisionBag();
        final PhysicsStepBag stepBag = new PhysicsStepBag();
        final int nWorkers = Math.max(2, Runtime.getRuntime().availableProcessors() + 1);
        for (int i = 0; i < nWorkers; i++) {
            new CollisionWorker(i, collisionBag).start();
            new PhysicsStepWorker(i, stepBag).start();
        }
        return new PhysicsEngine(collisionBag, stepBag);
    }
}

