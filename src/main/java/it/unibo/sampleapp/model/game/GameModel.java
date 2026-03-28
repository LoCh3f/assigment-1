package it.unibo.sampleapp.model.game;

import it.unibo.sampleapp.model.game.snapshot.GameSnapshot;
import it.unibo.sampleapp.util.Vector2D;

/**
 * Representation of the game.
 */
public interface GameModel {
    /**
     * Advances the physics simulation by dt seconds.
     * Called by the game loop thread each tick.
     *
     * @param dt delta second.
     */
    void applyPhysicsStep(double dt);

    /**
     * Adds an instantaneous velocity impulse to the human player's ball.
     * Called by the Controller on keyboard input from the view.
     *
     * @param direction velocity vector to apply at the human's ball.
     */
    void applyImpulseToHuman(Vector2D direction);

    /**
     * Adds an instantaneous velocity impulse to the bot's ball.
     * Called by BotThread each time the bot decides to move.
     *
     * @param direction velocity vector to apply at the bot's ball.
     */
    void applyImpulseToBot(Vector2D direction);

    /**
     * Returns an immutable snapshot of the current game state.
     * Safe to read from any thread after the call returns.
     *
     * @return an immutable snapshot of the current game state.
     */
    GameSnapshot getSnapshot();

    /**
     * Returns the current game status without a full snapshot.
     *
     * @return the current game status.
     */
    GameStatus getStatus();
}
