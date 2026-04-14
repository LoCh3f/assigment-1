package it.unibo.sampleapp.controller.concurrent.taskbased;

import it.unibo.sampleapp.controller.concurrent.BotMoveService;
import it.unibo.sampleapp.model.Model;

import java.util.concurrent.ExecutorService;

import static it.unibo.sampleapp.controller.concurrent.BotAIConstants.BOT_THINK_TIME_MS;

/**
 * The bot agent using task-based approach with Executor Framework.
 * Runs asynchronously and independently of the game loop.
 * Strategy: Analyzes the board and prioritizes targeting small balls to score points.
 * Falls back to random direction if no clear target is available.
 */
public final class BotTask implements Runnable {

    private final Model model;
    private final BotMoveService moveService;
    private final ExecutorService executor;

    /**
     * Constructs a new BotTask with the given model and executor.
     *
     * @param model the game model to control the bot ball
     * @param executor the executor to run on
     */
    public BotTask(final Model model, final ExecutorService executor) {
        this.model = model;
        this.executor = executor;
        this.moveService = new BotMoveService();
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
     * Reads snapshot, decides direction, applies impulse.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            final var snapshot = model.getSnapshot();
            final var direction = moveService.decideMove(snapshot);
            model.applyImpulseToBot(direction);

            try {
                Thread.sleep(BOT_THINK_TIME_MS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

}
