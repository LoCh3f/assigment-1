package it.unibo.sampleapp.controller.concurrent.multithread;

import it.unibo.sampleapp.controller.bot.BotMoveService;
import it.unibo.sampleapp.model.Model;

import java.util.concurrent.locks.LockSupport;

import static it.unibo.sampleapp.controller.bot.BotAIConstants.BOT_MOVE_DELAY_MS;
import static it.unibo.sampleapp.controller.bot.BotAIConstants.BOT_THINK_TIME_MS;
import static it.unibo.sampleapp.controller.bot.BotAIConstants.NANOS_PER_MILLIS;

/**
 * The bot agent. Runs asynchronously and independently of the game loop.
 * Strategy: Analyzes the board and prioritizes targeting small balls to score points.
 * Falls back to random direction if no clear target is available.
 */
public final class BotThread extends Thread {

    private final Model model;
    private final BotMoveService moveService;

    /**
     * Constructs a new BotThread with the given model.
     *
     * @param model the game model to control the bot ball
     */
    public BotThread(final Model model) {
        super("bot-agent");
        this.model = model;
        this.moveService = new BotMoveService();
        setDaemon(true);
    }

    /**
     * Runs the bot behavior, using an intelligent strategy to target small balls
     * when available, or random direction when no good target exists.
     */
    @Override
    public void run() {
        while (!isInterrupted()) {
            final var snapshot = model.getSnapshot();
            final var direction = moveService.decideMove(snapshot);
            model.applyImpulseToBot(direction);
            try {
                sleep(BOT_MOVE_DELAY_MS);
            } catch (final InterruptedException e) {
                currentThread().interrupt();
                break;
            }
            LockSupport.parkNanos(BOT_THINK_TIME_MS * NANOS_PER_MILLIS);
            if (isInterrupted()) {
                break;
            }
        }
    }

    /**
     * Stops the bot thread.
     */
    public void stopBot() {
        interrupt();
    }
}
