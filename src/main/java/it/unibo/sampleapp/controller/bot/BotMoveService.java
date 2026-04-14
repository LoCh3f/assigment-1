package it.unibo.sampleapp.controller.bot;

import it.unibo.sampleapp.model.snapshot.GameSnapshot;
import it.unibo.sampleapp.util.Vector2D;

/**
 * Service that decides the bot move direction based on a game snapshot.
 */
public final class BotMoveService {
    private final BotDecisionService decisionService = new BotDecisionService();

    /**
     * Decides the next move direction based on the current game snapshot.
     *
     * @param snapshot the current game state
     * @return the direction to move the bot
     */
    public Vector2D decideMove(final GameSnapshot snapshot) {
        return decisionService.chooseDirection(snapshot);
    }
}




