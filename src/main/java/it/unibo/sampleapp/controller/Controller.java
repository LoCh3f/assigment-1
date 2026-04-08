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
     * Returns the current frames per second.
     *
     * @return the current FPS value
     */
    int getCurrentFps();

    /**
     * Handles mouse aiming input.
     *
     * @param startPoint the starting point of the aim
     * @param endPoint the ending point of the aim
     * @param powerMultiplier the power multiplier (0.3 to 2.0)
     */
    void onAim(java.awt.Point startPoint, java.awt.Point endPoint, double powerMultiplier);

    /**
     * Handles mouse shooting input.
     *
     * @param startPoint the starting point of the shot
     * @param endPoint the ending point of the shot
     * @param powerMultiplier the power multiplier (0.3 to 2.0)
     */
    void onShoot(java.awt.Point startPoint, java.awt.Point endPoint, double powerMultiplier);
}
