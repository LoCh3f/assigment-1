package it.unibo.sampleapp.controller;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.sampleapp.model.GameModel;
import it.unibo.sampleapp.model.Model;
import it.unibo.sampleapp.model.status.GameStatus;
import it.unibo.sampleapp.util.Vector2D;
import it.unibo.sampleapp.view.View;

import javax.swing.SwingUtilities;

/**
 * Base controller with shared input routing and game-over watching.
 */
public abstract class AbstractController implements Controller {

    private final Model model;
    @SuppressFBWarnings("UwF")
    private View view;

    /**
     * @param model the model managed by this controller
     */
    protected AbstractController(final Model model) {
        this.model = model;
    }

    /**
     * Starts the controller lifecycle.
     */
    public abstract void start();

    /**
     * Stops the controller lifecycle.
     */
    public abstract void stop();

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "View reference is provided by the composition root during startup wiring"
    )
    @Override
    public final void setView(final View view) {
        this.view = view;
    }

    @Override
    public final void onDirectionInput(final Vector2D impulse) {
        model.applyImpulseToHuman(impulse);
    }

    @Override
    public final void onAim(final java.awt.Point startPoint, final java.awt.Point endPoint,
                            final double powerMultiplier) {
        // View-only feedback; no model update while aiming.
    }

    @Override
    public final void onShoot(final java.awt.Point startPoint, final java.awt.Point endPoint,
                              final double powerMultiplier) {
        final double dx = endPoint.x - startPoint.x;
        final double dy = endPoint.y - startPoint.y;
        final Vector2D impulse = new Vector2D(dx, dy).normalize().scale(powerMultiplier * 200.0);
        model.applyImpulseToHuman(impulse);
    }

    /**
     * @return the backing model instance
     */
    protected final Model model() {
        return model;
    }

    /**
     * @return the wired view reference
     */
    protected final View view() {
        return view;
    }

    /**
     * Starts the shared watcher that waits for game over and notifies the view.
     */
    protected final void startGameOverWatcher() {
        Thread.ofPlatform().name("game-over-watcher").start(this::watchForGameOver);
    }

    private void watchForGameOver() {
        try {
            ((GameModel) model).waitUntilGameOver();
            final GameStatus result = model.getStatus();
            stop();
            SwingUtilities.invokeLater(() -> view.displayGameOver(result));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}



