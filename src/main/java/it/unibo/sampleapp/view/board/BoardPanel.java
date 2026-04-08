package it.unibo.sampleapp.view.board;

import it.unibo.sampleapp.model.ball.Ball;
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
    private static final String FONT_NAME = "Monospaced";
    private static final int FONT_SIZE_HUD = 18;
    private static final int FONT_SIZE_MESSAGE = 48;
    private static final int FONT_SIZE_SUBLABEL = 16;
    private static final int PADDING_HUD = 12;
    private static final int CORNER_RADIUS = 8;
    private static final int MESSAGE_OFFSET = 36;
    private static final int COLOR_AIMING_CIRCLE_RED = 255;
    private static final int COLOR_AIMING_CIRCLE_GREEN = 255;
    private static final int COLOR_AIMING_CIRCLE_BLUE = 0;
    private static final int COLOR_AIMING_CIRCLE_ALPHA = 128;
    private static final double AIMING_LINE_SCALE = 100.0;
    private static final double POWER_INDICATOR_SCALE = 15.0;

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
            drawAimingGuides(g2, snap);
        }
    }

    private void drawHoles(final Graphics2D g2, final GameSnapshot snap) {
        g2.setColor(COLOR_HOLE);
        for (final Hole hole : snap.holes()) {
            final int radius = toScreenLength(hole.getRadius());
            final int x = toScreenX(hole.getPosition2D().x()) - radius;
            final int y = toScreenY(hole.getPosition2D().y()) - radius;
            final int d = radius * 2;
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
            final int radius = toScreenLength(ball.radius());
            final int x = toScreenX(ball.position().x()) - radius;
            final int y = toScreenY(ball.position().y()) - radius;
            final int d = radius * 2;

            g2.setColor(color);
            g2.fillOval(x, y, d, d);

            g2.setColor(color.darker());
            g2.drawOval(x, y, d, d);
        }
    }

    private void drawHUD(final Graphics2D g2, final GameSnapshot snap) {
        final int panelWidth = getWidth();
        final int panelHeight = getHeight();
        g2.setFont(new Font(FONT_NAME, Font.BOLD, FONT_SIZE_HUD));
        final FontMetrics fm = g2.getFontMetrics();

        final String humanLabel = "You: " + snap.humanScore();
        final String botLabel = "Bot: " + snap.botScore();

        final int hudH = fm.getHeight() + PADDING_HUD;

        g2.setColor(COLOR_SCORE_HUD);
        g2.fillRoundRect(8, panelHeight - hudH - 8,
                fm.stringWidth(humanLabel) + PADDING_HUD, hudH, CORNER_RADIUS, CORNER_RADIUS);
        g2.setColor(COLOR_HUMAN_BALL);
        g2.drawString(humanLabel, 8 + PADDING_HUD / 2,
                panelHeight - 8 - PADDING_HUD / 2 + 2);

        final int botW = fm.stringWidth(botLabel) + PADDING_HUD;
        g2.setColor(COLOR_SCORE_HUD);
        g2.fillRoundRect(panelWidth - botW - 8, panelHeight - hudH - 8,
                botW, hudH, CORNER_RADIUS, CORNER_RADIUS);
        g2.setColor(COLOR_BOT_BALL);
        g2.drawString(botLabel, panelWidth - botW - 8 + PADDING_HUD / 2,
                panelHeight - 8 - PADDING_HUD / 2 + 2);

        // Draw FPS indicator
        final String fpsLabel = "FPS: " + currentFps;
        final int textWidth = fm.stringWidth(fpsLabel);
        final int textHeight = fm.getHeight();
        final int padding = 12;

        final int boxWidth = textWidth + padding;
        final int boxHeight = textHeight + padding;

        final int x = (panelWidth - boxWidth) / 2;
        final int y = panelHeight - boxHeight - 8;

        g2.setColor(COLOR_HUD_BACKGROUND);
        g2.fillRoundRect(x, y, boxWidth, boxHeight, 8, 8);

        g2.setColor(COLOR_BLACK_TEXT);
        g2.drawString(
                fpsLabel,
                x + (boxWidth - textWidth) / 2,
                y + (boxHeight - textHeight) / 2 + fm.getAscent()
        );

    }

    private void drawGameOverOverlay(final Graphics2D g2,
                                     final String message) {
        final int panelWidth = getWidth();
        final int panelHeight = getHeight();
        g2.setColor(COLOR_OVERLAY);
        g2.fillRect(0, 0, panelWidth, panelHeight);

        g2.setFont(new Font(FONT_NAME, Font.BOLD, FONT_SIZE_MESSAGE));
        FontMetrics fm = g2.getFontMetrics();
        final int msgW = fm.stringWidth(message);
        final int msgH = fm.getAscent();

        g2.setColor(Color.WHITE);
        g2.drawString(message,
                (panelWidth - msgW) / 2,
                (panelHeight + msgH) / 2);

        g2.setFont(new Font(FONT_NAME, Font.PLAIN, FONT_SIZE_SUBLABEL));
        final String sub = "Close the window to exit.";
        fm = g2.getFontMetrics();
        g2.setColor(COLOR_SUBTEXT);
        g2.drawString(sub,
                (panelWidth - fm.stringWidth(sub)) / 2,
                (panelHeight + msgH) / 2 + MESSAGE_OFFSET);
    }

    /**
     * Sets the aiming state variables.
     *
     * @param isAimingState    whether the player is currently aiming
     * @param aimStart         the starting point of the aim (where the mouse pressed)
     * @param aimEnd           the ending point of the aim (where the mouse released)
     * @param power            the multiplier for power based on aim length
     */
    public void setAimingState(final boolean isAimingState,
                                final Point2D.Double aimStart,
                                final Point2D.Double aimEnd,
                                final double power) {
        this.isAiming = isAimingState;
        this.aimStartPoint = aimStart == null ? null : new Point2D.Double(aimStart.x, aimStart.y);
        this.aimEndPoint = aimEnd == null ? null : new Point2D.Double(aimEnd.x, aimEnd.y);
        this.powerMultiplier = power;
        repaint(); // Repaint to update aiming visuals
    }

    private void drawAimingGuides(final Graphics2D g2, final GameSnapshot snap) {
        if (aimStartPoint == null || aimEndPoint == null) {
            return;
        }

        // Find the human ball position
        final BallSnapshot humanBall = snap.balls().stream()
                .filter(ball -> ball.type() == Ball.Type.HUMAN)
                .findFirst()
                .orElse(null);
        if (humanBall == null) {
            return;
        }

        // Calculate the direction vector from mouse drag
        final double dx = aimEndPoint.x - aimStartPoint.x;
        final double dy = aimEndPoint.y - aimStartPoint.y;
        final double length = Math.sqrt(dx * dx + dy * dy);
        if (length == 0) {
            return;
        }

        // Normalize and scale the direction
        final double scaledDx = dx / length * AIMING_LINE_SCALE;
        final double scaledDy = dy / length * AIMING_LINE_SCALE;

        // Draw the aiming line from the human ball in the direction of the aim
        final double ballX = toScreenX(humanBall.position().x());
        final double ballY = toScreenY(humanBall.position().y());
        g2.setColor(Color.RED);
        g2.drawLine(
                (int) ballX,
                (int) ballY,
                (int) (ballX + scaledDx),
                (int) (ballY + scaledDy)
        );

        // Draw an arrowhead at the end of the line
        drawArrowhead(g2, ballX, ballY, ballX + scaledDx, ballY + scaledDy);

        // Draw a power indicator circle at the ball
        final int radius = (int) (powerMultiplier * POWER_INDICATOR_SCALE);
        final Color aimingColor = new Color(COLOR_AIMING_CIRCLE_RED, COLOR_AIMING_CIRCLE_GREEN,
                COLOR_AIMING_CIRCLE_BLUE, COLOR_AIMING_CIRCLE_ALPHA);
        g2.setColor(aimingColor);
        g2.fillOval(
                (int) (ballX - radius),
                (int) (ballY - radius),
                radius * 2,
                radius * 2
        );
    }

    private void drawArrowhead(final Graphics2D g2, final double x1, final double y1, final double x2, final double y2) {
        final double arrowLength = 15.0;
        final double arrowAngle = Math.PI / 6; // 30 degrees

        final double angle = Math.atan2(y2 - y1, x2 - x1);
        final double arrowX1 = x2 - arrowLength * Math.cos(angle - arrowAngle);
        final double arrowY1 = y2 - arrowLength * Math.sin(angle - arrowAngle);
        final double arrowX2 = x2 - arrowLength * Math.cos(angle + arrowAngle);
        final double arrowY2 = y2 - arrowLength * Math.sin(angle + arrowAngle);

        g2.setColor(Color.RED);
        g2.drawLine((int) x2, (int) y2, (int) arrowX1, (int) arrowY1);
        g2.drawLine((int) x2, (int) y2, (int) arrowX2, (int) arrowY2);
    }

    private int toScreenX(final double boardX) {
        return (int) Math.round(boardX * getWidth() / boardWidth);
    }

    private int toScreenY(final double boardY) {
        return (int) Math.round(boardY * getHeight() / boardHeight);
    }

    private int toScreenLength(final double boardLength) {
        final double xScale = getWidth() / (double) boardWidth;
        final double yScale = getHeight() / (double) boardHeight;
        return (int) Math.max(1, Math.round(boardLength * Math.min(xScale, yScale)));
    }
}
