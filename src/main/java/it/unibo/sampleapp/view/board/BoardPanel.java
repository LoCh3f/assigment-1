package it.unibo.sampleapp.view.board;

import it.unibo.sampleapp.model.hole.Hole;
import it.unibo.sampleapp.model.snapshot.BallSnapshot;
import it.unibo.sampleapp.model.snapshot.GameSnapshot;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.io.Serial;

/**
 * The panel that renders the game board.
 * Displays balls, holes, scores, FPS, and game-over messages.
 */
public final class BoardPanel extends JPanel {

    private static final Color COLOR_SMALL_BALL = new Color(220, 220, 220);
    private static final Color COLOR_HUMAN_BALL = new Color(70, 130, 230);
    private static final Color COLOR_BOT_BALL = new Color(220, 60, 60);
    private static final Color COLOR_HOLE = new Color(10, 10, 10);
    private static final Color COLOR_SCORE_HUD = new Color(255, 255, 255, 200);
    private static final Color COLOR_OVERLAY = new Color(0, 0, 0, 160);
    private static final Color COLOR_SUBTEXT = new Color(200, 200, 200);
    private static final Color COLOR_HUD_BACKGROUND = new Color(255, 255, 255, 200);
    private static final Color COLOR_BLACK_TEXT = Color.BLACK;
    private static final Color COLOR_TURN_HUMAN = new Color(70, 130, 230, 180);
    private static final Color COLOR_TURN_BOT = new Color(220, 60, 60, 180);
    private static final String FONT_NAME = "Monospaced";
    private static final int FONT_SIZE_HUD = 18;
    private static final int FONT_SIZE_MESSAGE = 48;
    private static final int FONT_SIZE_SUBLABEL = 16;
    private static final int PADDING_HUD = 12;
    private static final int PADDING_TURN = 10;
    private static final int CORNER_RADIUS = 8;
    private static final int MESSAGE_OFFSET = 36;
    private static final int TURN_BOX_Y = 12;

    @Serial
    private static final long serialVersionUID = 1L;

    private final int boardWidth;
    private final int boardHeight;
    private String gameOverMessage;
    private transient volatile GameSnapshot currentSnapshot;
    private transient volatile int currentFps;

    private transient volatile boolean isAiming;
    private transient volatile Point2D.Double aimStartPoint;
    private transient volatile Point2D.Double aimEndPoint;
    private transient volatile double powerMultiplier;

    /**
     * Constructs a BoardPanel with specified dimensions.
     *
     * @param w the width of the board
     * @param h the height of the board
     */
    public BoardPanel(final int w, final int h) {
        this.boardWidth = w;
        this.boardHeight = h;
    }

    /**
     * Sets the game-over message to display as an overlay.
     *
     * @param msg the game-over message text
     */
    public void setGameOverMessage(final String msg) {
        this.gameOverMessage = msg;
    }

    /**
     * Sets the current game snapshot for rendering.
     *
     * @param snapshot the game snapshot to render
     */
    public void setCurrentSnapshot(final GameSnapshot snapshot) {
        this.currentSnapshot = snapshot;
    }

    /**
     * Sets the current FPS value for rendering.
     *
     * @param fps the frames per second value
     */
    public void setCurrentFps(final int fps) {
        this.currentFps = fps;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        final Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        final GameSnapshot snap = currentSnapshot;
        if (snap == null) {
            return;
        }

        drawHoles(g2, snap);
        drawBalls(g2, snap);
        drawHUD(g2, snap);

        if (gameOverMessage != null) {
            drawGameOverOverlay(g2, gameOverMessage);
        }

        // Draw aiming helper lines if aiming
        if (isAiming) {
            drawAimingGuides(g2);
        }
    }

    private void drawHoles(final Graphics2D g2, final GameSnapshot snap) {
        g2.setColor(COLOR_HOLE);
        for (final Hole hole : snap.holes()) {
            final int x = (int) (hole.getPosition2D().x() - hole.getRadius());
            final int y = (int) (hole.getPosition2D().y() - hole.getRadius());
            final int d = (int) (hole.getRadius() * 2);
            g2.fillOval(x, y, d, d);
        }
    }

    private void drawBalls(final Graphics2D g2, final GameSnapshot snap) {
        for (final BallSnapshot ball : snap.balls()) {
            final Color color = switch (ball.type()) {
                case SMALL -> COLOR_SMALL_BALL;
                case HUMAN -> COLOR_HUMAN_BALL;
                case BOT -> COLOR_BOT_BALL;
            };
            final int x = (int) (ball.position().x() - ball.radius());
            final int y = (int) (ball.position().y() - ball.radius());
            final int d = (int) (ball.radius() * 2);

            g2.setColor(color);
            g2.fillOval(x, y, d, d);

            g2.setColor(color.darker());
            g2.drawOval(x, y, d, d);
        }
    }

    private void drawHUD(final Graphics2D g2, final GameSnapshot snap) {
        g2.setFont(new Font(FONT_NAME, Font.BOLD, FONT_SIZE_HUD));
        final FontMetrics fm = g2.getFontMetrics();

        final String humanLabel = "You: " + snap.humanScore();
        final String botLabel = "Bot: " + snap.botScore();

        final int hudH = fm.getHeight() + PADDING_HUD;

        g2.setColor(COLOR_SCORE_HUD);
        g2.fillRoundRect(8, boardHeight - hudH - 8,
                fm.stringWidth(humanLabel) + PADDING_HUD, hudH, CORNER_RADIUS, CORNER_RADIUS);
        g2.setColor(COLOR_HUMAN_BALL);
        g2.drawString(humanLabel, 8 + PADDING_HUD / 2,
                boardHeight - 8 - PADDING_HUD / 2 + 2);

        final int botW = fm.stringWidth(botLabel) + PADDING_HUD;
        g2.setColor(COLOR_SCORE_HUD);
        g2.fillRoundRect(boardWidth - botW - 8, boardHeight - hudH - 8,
                botW, hudH, CORNER_RADIUS, CORNER_RADIUS);
        g2.setColor(COLOR_BOT_BALL);
        g2.drawString(botLabel, boardWidth - botW - 8 + PADDING_HUD / 2,
                boardHeight - 8 - PADDING_HUD / 2 + 2);

        // Draw FPS indicator
        final String fpsLabel = "FPS: " + currentFps;
        final int textWidth = fm.stringWidth(fpsLabel);
        final int textHeight = fm.getHeight();
        final int padding = 12;

        final int boxWidth = textWidth + padding;
        final int boxHeight = textHeight + padding;

        final int x = (boardWidth - boxWidth) / 2;
        final int y = boardHeight - boxHeight - 8;

        g2.setColor(COLOR_HUD_BACKGROUND);
        g2.fillRoundRect(x, y, boxWidth, boxHeight, 8, 8);

        g2.setColor(COLOR_BLACK_TEXT);
        g2.drawString(
                fpsLabel,
                x + (boxWidth - textWidth) / 2,
                y + (boxHeight - textHeight) / 2 + fm.getAscent()
        );

        // Draw turn indicator at the top
        drawTurnIndicator(g2, fm);
    }

    /**
     * Draws a turn indicator showing asynchronous play mode.
     *
     * @param g2 the graphics context
     * @param fm the font metrics
     */
    private void drawTurnIndicator(final Graphics2D g2, final FontMetrics fm) {
        final String turnText = "ASYNC PLAY - Move when balls stop";
        final int textWidth = fm.stringWidth(turnText);
        final int textHeight = fm.getHeight();

        final int turnBoxW = textWidth + PADDING_TURN * 2;
        final int turnBoxH = textHeight + PADDING_TURN;

        final int turnX = (boardWidth - turnBoxW) / 2;
        final int turnY = TURN_BOX_Y;

        // Draw semi-transparent background
        final Color turnBgColor = new Color(100, 100, 100, 180); // Gray for async mode

        g2.setColor(turnBgColor);
        g2.fillRoundRect(turnX, turnY, turnBoxW, turnBoxH, CORNER_RADIUS, CORNER_RADIUS);

        g2.setColor(Color.WHITE);
        g2.drawString(turnText, turnX + PADDING_TURN,
                turnY + PADDING_TURN + fm.getAscent());
    }

    private void drawGameOverOverlay(final Graphics2D g2,
                                     final String message) {
        g2.setColor(COLOR_OVERLAY);
        g2.fillRect(0, 0, boardWidth, boardHeight);

        g2.setFont(new Font(FONT_NAME, Font.BOLD, FONT_SIZE_MESSAGE));
        FontMetrics fm = g2.getFontMetrics();
        final int msgW = fm.stringWidth(message);
        final int msgH = fm.getAscent();

        g2.setColor(Color.WHITE);
        g2.drawString(message,
                (boardWidth - msgW) / 2,
                (boardHeight + msgH) / 2);

        g2.setFont(new Font(FONT_NAME, Font.PLAIN, FONT_SIZE_SUBLABEL));
        final String sub = "Close the window to exit.";
        fm = g2.getFontMetrics();
        g2.setColor(COLOR_SUBTEXT);
        g2.drawString(sub,
                (boardWidth - fm.stringWidth(sub)) / 2,
                (boardHeight + msgH) / 2 + MESSAGE_OFFSET);
    }

    /**
     * Sets the aiming state variables.
     *
     * @param isAiming        whether the player is currently aiming
     * @param aimStartPoint   the starting point of the aim (where the mouse pressed)
     * @param aimEndPoint     the ending point of the aim (where the mouse released)
     * @param powerMultiplier the multiplier for power based on aim length
     */
    public void setAimingState(final boolean isAiming,
                                final Point2D.Double aimStartPoint,
                                final Point2D.Double aimEndPoint,
                                final double powerMultiplier) {
        this.isAiming = isAiming;
        this.aimStartPoint = aimStartPoint;
        this.aimEndPoint = aimEndPoint;
        this.powerMultiplier = powerMultiplier;
        repaint(); // Repaint to update aiming visuals
    }

    private void drawAimingGuides(final Graphics2D g2) {
        if (aimStartPoint == null || aimEndPoint == null) {
            return;
        }

        // Draw the power vector
        g2.setColor(Color.RED);
        g2.drawLine(
                (int) aimStartPoint.x,
                (int) aimStartPoint.y,
                (int) aimEndPoint.x,
                (int) aimEndPoint.y
        );

        // Draw a circle at the end point of the aim to indicate power
        final int radius = (int) (powerMultiplier * 10); // Scale the radius by the power multiplier
        g2.setColor(Color.YELLOW);
        g2.fillOval(
                (int) (aimEndPoint.x - radius),
                (int) (aimEndPoint.y - radius),
                radius * 2,
                radius * 2
        );
    }
}
