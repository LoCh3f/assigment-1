package it.unibo.sampleapp.controller;

import it.unibo.sampleapp.concurrent.multithread.GameLoopThread;
import it.unibo.sampleapp.concurrent.multithread.BotThread;
import it.unibo.sampleapp.model.GameModel;
import it.unibo.sampleapp.model.GameModelImpl;
import it.unibo.sampleapp.model.status.GameStatus;
import it.unibo.sampleapp.util.Vector2D;
import it.unibo.sampleapp.view.View;

import javax.swing.SwingUtilities;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The Controller.
 *
 * <p>
 * Implements ViewObserver → reacts to user input from the View.
 * Holds Model → delegates all state changes to the Model.
 * Holds ViewInterface → pushes updates to the View each tick.
 * Owns GameLoopThread and BotThread → starts/stops the game lifecycle.
 */
public final class ControllerImpl implements Controller {

    private final GameModel model;
    @SuppressFBWarnings("UwF")
    private View view;

    private GameLoopThread gameLoopThread;
    private BotThread botThread;

    /**
     * Constructs a new ControllerImpl with the given model.
     *
     * @param model the game model to control
     */
    public ControllerImpl(final GameModel model) {
        this.model = model;
    }

    /**
     * Called by Application after the View is constructed.
     * View is set separately because Controller is passed to View constructor
     * (chicken-and-egg — Application wires both directions).
     *
     * @param view the view to control
     */
    @Override
    public void setView(final View view) {
        this.view = view;
    }

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    /**
     * Starts the game loop and bot threads.
     * Called by Application after full wiring is complete.
     */
    public void start() {
        gameLoopThread = new GameLoopThread(model, view);
        botThread = new BotThread(model);

        gameLoopThread.start();
        botThread.start();

        // Wait for game over on a dedicated thread so we don't block the EDT
        Thread.ofPlatform().name("game-over-watcher").start(this::watchForGameOver);
    }

    /**
     * Gracefully stops all threads. Called on window close.
     */
    public void stop() {
        if (gameLoopThread != null) {
            gameLoopThread.stopLoop();
        }
        if (botThread != null) {
            botThread.stopBot();
        }
    }

    // -----------------------------------------------------------------------
    // ViewObserver — called by the View on user events (Swing EDT)
    // -----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDirectionInput(final Vector2D impulse) {
        // Run off the EDT so blocking wait() in GameModel doesn't freeze the UI
        Thread.ofPlatform().name("input-relay").start(() ->
                model.applyImpulseToHuman(impulse)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onGameStartRequested() {
        start();
    }

    // -----------------------------------------------------------------------
    // Game-over watcher
    // -----------------------------------------------------------------------

    private void watchForGameOver() {
        try {
            // Blocks until GameModel.notifyAll() is called on game over
            ((GameModelImpl) model).waitUntilGameOver();
            final GameStatus result = model.getStatus();
            stop();
            // Push game-over to the view on the EDT
            SwingUtilities.invokeLater(() -> view.displayGameOver(result));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Returns the current frames per second from the game loop thread.
     *
     * @return the current FPS value, or 0 if game loop is not running
     */
    @Override
    public int getCurrentFps() {
        return gameLoopThread != null ? gameLoopThread.getCurrentFps() : 0;
    }
}

