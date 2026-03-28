package it.unibo.sampleapp.controller;

import it.unibo.sampleapp.model.Model;
import it.unibo.sampleapp.util.Vector2D;
import it.unibo.sampleapp.view.View;

/**
 * Controller of the MVC.
 */
public interface Controller {

    /**
     * @param model for MVC.
     */
    void setModel(Model model);

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
    void onGameStart();
}
