package it.unibo.sampleapp.controller;

import it.unibo.sampleapp.Main.ConcurrencyMode;
import it.unibo.sampleapp.concurrent.multithread.GameLoopThread;
import it.unibo.sampleapp.concurrent.multithread.BotThread;
import it.unibo.sampleapp.concurrent.taskbased.GameLoopTask;
import it.unibo.sampleapp.concurrent.taskbased.BotTask;
import it.unibo.sampleapp.model.GameModel;
import it.unibo.sampleapp.model.GameModelImpl;
import it.unibo.sampleapp.model.status.GameStatus;
import it.unibo.sampleapp.util.Vector2D;
import it.unibo.sampleapp.view.View;

import javax.swing.SwingUtilities;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ExecutorService;

/**
 * The Controller.
 *
 * <p>
 * Implements ViewObserver → reacts to user input from the View.
 * Holds Model → delegates all state changes to the Model.
 * Holds ViewInterface → pushes updates to the View each tick.
 * Owns GameLoopThread/GameLoopTask and BotThread/BotTask → starts/stops the game lifecycle.
 */
public final class ControllerImpl implements Controller {

    private final GameModel model;
    private final ConcurrencyMode mode;
    @SuppressFBWarnings("UwF")
    private View view;

    private GameLoopThread gameLoopThread;
    private BotThread botThread;

    private ScheduledExecutorService gameLoopExecutor;
    private ExecutorService botExecutor;
    private GameLoopTask gameLoopTask;

    /**
     * Constructs a new ControllerImpl with the given model and concurrency mode.
     *
     * @param model the game model to control
     * @param mode the concurrency mode (MULTITHREAD or TASKBASED)
     */
    public ControllerImpl(final GameModel model, final ConcurrencyMode mode) {
        this.model = model;
        this.mode = mode;
    }

    /**
     * Called by Application after the View is constructed.
     * View is set separately because Controller is passed to View constructor
     * (chicken-and-egg — Application wires both directions).
     *
     * @param view the view to control
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "View reference is provided by the composition root during startup wiring"
    )
    @Override
    public void setView(final View view) {
        this.view = view;
    }

    /**
     * Starts the game loop and bot threads.
     * Called by Application after full wiring is complete.
     */
    public void start() {
        if (mode == ConcurrencyMode.MULTITHREAD) {
            gameLoopThread = new GameLoopThread(model, view);
            botThread = new BotThread(model);

            gameLoopThread.start();
            botThread.start();
        } else { // TASKBASED
            gameLoopExecutor = Executors.newSingleThreadScheduledExecutor();
            botExecutor = Executors.newSingleThreadExecutor();
            gameLoopTask = new GameLoopTask(model, view);

            gameLoopTask.start(gameLoopExecutor);
            new BotTask(model, botExecutor).start();
        }

        // Wait for game over on a dedicated thread so we don't block the EDT
        Thread.ofPlatform().name("game-over-watcher").start(this::watchForGameOver);
    }

    /**
     * Gracefully stops all threads. Called on window close.
     */
    public void stop() {
        if (mode == ConcurrencyMode.MULTITHREAD) {
            if (gameLoopThread != null) {
                gameLoopThread.stopLoop();
            }
            if (botThread != null) {
                botThread.stopBot();
            }
        } else { // TASKBASED
            if (gameLoopExecutor != null) {
                gameLoopExecutor.shutdownNow();
            }
            if (botExecutor != null) {
                botExecutor.shutdownNow();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDirectionInput(final Vector2D impulse) {
        model.applyImpulseToHuman(impulse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onGameStartRequested() {
        start();
    }

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
     * Returns the current frames per second from the game loop thread/task.
     *
     * @return the current FPS value, or 0 if game loop is not running
     */
    @Override
    public int getCurrentFps() {
        if (mode == ConcurrencyMode.MULTITHREAD) {
            return gameLoopThread != null ? gameLoopThread.getCurrentFps() : 0;
        } else {
            return gameLoopTask != null ? gameLoopTask.getCurrentFps() : 0;
        }
    }

    /**
     * Handles mouse aiming input (for visual feedback).
     *
     * @param startPoint the starting point of the aim
     * @param endPoint the ending point of the aim
     * @param powerMultiplier the power multiplier (0.3 to 2.0)
     */
    @Override
    public void onAim(final java.awt.Point startPoint, final java.awt.Point endPoint,
            final double powerMultiplier) {
        // Aiming is handled in the view for visual feedback only
        // No model interaction needed during aiming
    }

    /**
     * Handles mouse shooting input.
     *
     * @param startPoint the starting point of the shot
     * @param endPoint the ending point of the shot
     * @param powerMultiplier the power multiplier (0.3 to 2.0)
     */
    @Override
    public void onShoot(final java.awt.Point startPoint, final java.awt.Point endPoint,
            final double powerMultiplier) {
        // Convert mouse coordinates to impulse vector
        final double dx = endPoint.x - startPoint.x;
        final double dy = endPoint.y - startPoint.y;

        // Create impulse vector with power multiplier
        final Vector2D impulse = new Vector2D(dx, dy).normalize()
                .scale(powerMultiplier * 200.0); // Scale to match existing impulse strength

        model.applyImpulseToHuman(impulse);
    }
}
