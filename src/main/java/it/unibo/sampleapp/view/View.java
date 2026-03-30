package it.unibo.sampleapp.view;

import it.unibo.sampleapp.model.snapshot.GameSnapshot;
import it.unibo.sampleapp.model.status.GameStatus;

/**
 * View interface.
 */
public interface View {
    /**
     * Shows the view, in particular the JFrame.
     */
    void show();

    /**
     * Updates the view with a new game snapshot.
     *
     * @param snapshot the current game snapshot to display
     */
    void update(GameSnapshot snapshot);

    /**
     * Displays a game over message.
     *
     * @param result the game result status
     */
    void displayGameOver(GameStatus result);
}


