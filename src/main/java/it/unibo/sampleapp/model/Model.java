package it.unibo.sampleapp.model;

import it.unibo.sampleapp.controller.Controller;
import it.unibo.sampleapp.model.game.GameStatus;
import it.unibo.sampleapp.model.game.snapshot.GameSnapshot;
import it.unibo.sampleapp.util.Vector2D;

/**
 * Model interface for MVC.
 */
public interface Model {
    /**
     * @param controller for the MVC.
     */
    void setController(Controller controller);

    /**
     * @return controller associated to the model.
     */
    Controller getController();

    /**
     * Applies an impulse to the human-controlled ball.
     *
     * @param direction the impulse direction vector
     */
    void applyImpulseToHuman(Vector2D direction);

    /**
     * Applies an impulse to the bot-controlled ball.
     *
     * @param direction the impulse direction vector
     */
    void applyImpulseToBot(Vector2D direction);

    /**
     * Gets a snapshot of the current game state.
     *
     * @return the current game snapshot
     */
    GameSnapshot getSnapshot();

    /**
     * Gets the current status of the game.
     *
     * @return the game status
     */
    GameStatus getStatus();

    /**
     * Waits until the game is over.
     *
     * @throws InterruptedException if the thread is interrupted
     */
    void waitUntilGameOver() throws InterruptedException;
}
