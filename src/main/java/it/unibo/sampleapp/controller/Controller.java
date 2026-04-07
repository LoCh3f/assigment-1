package it.unibo.sampleapp.controller;

import it.unibo.sampleapp.util.Vector2D;
import it.unibo.sampleapp.view.View;

/**
 * Controller of the MVC.
 */
public interface Controller {
    /**
     * @param view for the MVC.
     */
    void setView(View view);

    /**
     * Handles directional input from the user (arrow key pressed).
     *
     * @param impulse the direction vector for the impulse
     */
    void onDirectionInput(Vector2D impulse);

    /**
     * Handles the game start event.
     */
    void onGameStartRequested();

    /**
     * Returns the current frames per second.
     *
     * @return the current FPS value
     */
    int getCurrentFps();
}
