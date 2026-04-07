package it.unibo.sampleapp.concurrent.multithread;

import it.unibo.sampleapp.model.GameModel;
import it.unibo.sampleapp.util.Vector2D;

import java.util.Random;

/**
 * The bot agent. Runs asynchronously and independently of the game loop.
 * Behaviour: every 600–1400ms, picks a random direction and kicks its ball.
 * The assignment says the bot doesn't need to be smart — this is intentional.
 */
public final class BotThread extends Thread {

    private final GameModel model;
    private final Random rng = new Random();

    /**
     * Constructs a new BotThread with the given model.
     *
     * @param model the game model to control the bot ball
     */
    public BotThread(final GameModel model) {
        super("bot-agent");
        this.model = model;
        setDaemon(true);
    }

    /**
     * Runs the bot behavior, periodically applying random impulses to the bot ball.
     */
    @Override
    public void run() {
        while (true) {
            // Block inside the model until it's bot's turn and balls stopped
            final double angle = rng.nextDouble() * 2 * Math.PI;
            final Vector2D direction = new Vector2D(Math.cos(angle), Math.sin(angle));
            model.applyImpulseToBot(direction);
            // After this, physics will run, balls will move; next call will block again
        }
    }

    /**
     * Stops the bot thread.
     */
    public void stopBot() {
        interrupt();
    }
}
