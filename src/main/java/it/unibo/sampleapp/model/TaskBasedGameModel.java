package it.unibo.sampleapp.model;

import it.unibo.sampleapp.model.physics.PhysicsEngine;
import it.unibo.sampleapp.model.physics.collision.taskbased.TaskBasedCollisionResolver;
import it.unibo.sampleapp.model.physics.collision.sequential.SequentialCollisionResolver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Game model configured with task-based collision resolution.
 */
public final class TaskBasedGameModel extends GameModel {

    /**
     * Creates a model configured with task-based collision resolution.
     * Uses a default thread pool executor for collision tasks.
     *
     * @param boardWidth board width
     * @param boardHeight board height
     * @param numSmallBalls number of small balls
     */
    public TaskBasedGameModel(final int boardWidth, final int boardHeight, final int numSmallBalls) {
        this(boardWidth, boardHeight, numSmallBalls, createDefaultExecutor());
    }

    /**
     * Creates a model with an explicit executor service for collision tasks.
     *
     * @param boardWidth board width
     * @param boardHeight board height
     * @param numSmallBalls number of small balls
     * @param collisionExecutor executor for parallel collision resolution
     */
    public TaskBasedGameModel(final int boardWidth, final int boardHeight, final int numSmallBalls,
                              final ExecutorService collisionExecutor) {
        super(boardWidth, boardHeight, numSmallBalls,
              new PhysicsEngine(new TaskBasedCollisionResolver(collisionExecutor, new SequentialCollisionResolver())));
    }

    private static ExecutorService createDefaultExecutor() {
        final int nThreads = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
        return Executors.newFixedThreadPool(nThreads);
    }
}

