package it.unibo.sampleapp.model.rules;

import it.unibo.sampleapp.model.domain.ball.Ball;
import it.unibo.sampleapp.model.status.GameStatus;

/**
 * Encapsulates score updates and winner evaluation rules.
 */
public final class ScoreBoard {

    private final int totalSmallBalls;
    private int humanScore;
    private int botScore;

    /**
     * Creates an empty score board.
     *
     * @param totalSmallBalls initial amount of small balls used for majority threshold
     */
    public ScoreBoard(final int totalSmallBalls) {
        this.totalSmallBalls = totalSmallBalls;
        this.humanScore = 0;
        this.botScore = 0;
    }

    /**
     * Returns current human score.
     *
     * @return human score
     */
    public int getHumanScore() {
        return humanScore;
    }

    /**
     * Returns current bot score.
     *
     * @return bot score
     */
    public int getBotScore() {
        return botScore;
    }

    /**
     * Assigns one point only when a player's ball directly pocketed a small ball.
     *
     * @param lastCollidedWith type of the ball that last collided with a pocketed small ball
     */
    public void assignPointForSmallBall(final Ball.Type lastCollidedWith) {
        if (lastCollidedWith == Ball.Type.HUMAN) {
            humanScore++;
        } else if (lastCollidedWith == Ball.Type.BOT) {
            botScore++;
        }
    }

    /**
     * Returns immediate winner when a player ball is pocketed.
     *
     * @param pocketedType type of the pocketed ball
     * @return immediate winner or PLAYING if not applicable
     */
    public GameStatus winnerForPocketedPlayerBall(final Ball.Type pocketedType) {
        if (pocketedType == Ball.Type.HUMAN) {
            return GameStatus.BOT_WINS;
        }
        if (pocketedType == Ball.Type.BOT) {
            return GameStatus.HUMAN_WINS;
        }
        return GameStatus.PLAYING;
    }

    /**
     * Returns winner when someone reaches strict majority of small balls.
     *
     * @return winner status or PLAYING if nobody reached majority yet
     */
    public GameStatus winnerByMajority() {
        if (humanScore > totalSmallBalls / 2) {
            return GameStatus.HUMAN_WINS;
        }
        if (botScore > totalSmallBalls / 2) {
            return GameStatus.BOT_WINS;
        }
        return GameStatus.PLAYING;
    }

    /**
     * Returns winner when no small balls are left on board.
     *
     * @return final winner or DRAW
     */
    public GameStatus winnerWhenBoardIsCleared() {
        if (humanScore > botScore) {
            return GameStatus.HUMAN_WINS;
        }
        if (botScore > humanScore) {
            return GameStatus.BOT_WINS;
        }
        return GameStatus.DRAW;
    }
}


