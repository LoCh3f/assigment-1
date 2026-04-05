package it.unibo.sampleapp.view;

import it.unibo.sampleapp.controller.Controller;
import it.unibo.sampleapp.model.hole.Hole;
import it.unibo.sampleapp.model.snapshot.BallSnapshot;
import it.unibo.sampleapp.model.snapshot.GameSnapshot;
import it.unibo.sampleapp.model.status.GameStatus;
import it.unibo.sampleapp.util.Vector2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serial;

/**
 * The View layer — purely passive.
 * Responsibilities:
 *  - Paint whatever GameSnapshot it holds (called by GameLoopThread via update())
 *  - Forward raw keyboard events to ViewObserver (the Controller)
 *  - Never touch the model directly
 */
public final class ViewImpl extends JFrame implements View {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Color COLOR_BOARD = new Color(34, 85, 34);
    private static final Color COLOR_SMALL_BALL = new Color(220, 220, 220);
    private static final Color COLOR_HUMAN_BALL = new Color(70, 130, 230);
    private static final Color COLOR_BOT_BALL = new Color(220, 60, 60);
    private static final Color COLOR_HOLE = new Color(10, 10, 10);
    private static final Color COLOR_SCORE_HUD = new Color(255, 255, 255, 200);
    private static final Color COLOR_OVERLAY = new Color(0, 0, 0, 160);
    private static final Color COLOR_SUBTEXT = new Color(200, 200, 200);
    private static final double IMPULSE_MAGNITUDE = 1.0;
    private static final String FONT_NAME = "Monospaced";
    private static final int FONT_SIZE_HUD = 18;
    private static final int FONT_SIZE_MESSAGE = 48;
    private static final int FONT_SIZE_SUBLABEL = 16;
    private static final int PADDING_HUD = 12;
    private static final int CORNER_RADIUS = 8;
    private static final int MESSAGE_OFFSET = 36;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final transient Controller observer;
    private final BoardPanel boardPanel;
    private transient volatile GameSnapshot currentSnapshot;

    /**
     * Constructs a new ViewImpl with the given board dimensions and observer.
     *
     * @param boardWidth the width of the board
     * @param boardHeight the height of the board
     * @param observer the controller to notify of user input
     */
    public ViewImpl(final int boardWidth, final int boardHeight,
                    final Controller observer) {
        super("Pool");
        this.observer = observer;
        this.boardPanel = new BoardPanel(boardWidth, boardHeight);
        boardPanel.setPreferredSize(new Dimension(boardWidth, boardHeight));
        boardPanel.setBackground(COLOR_BOARD);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().add(boardPanel);
        pack();
        setLocationRelativeTo(null);

        setupKeyBindings();
    }

    /**
     * Receives a new snapshot from the game loop thread.
     * Stores it and requests a repaint on the EDT.
     * Never blocks the caller.
     *
     * @param snapshot the current game snapshot to display
     */
    @Override
    public void update(final GameSnapshot snapshot) {
        this.currentSnapshot = snapshot;
        boardPanel.repaint();
    }

    /**
     * Overlays a game-over message. Called on the EDT by the Controller.
     *
     * @param result the game result to display
     */
    @Override
    public void displayGameOver(final GameStatus result) {
        boardPanel.setGameOverMessage(formatResult(result));
        boardPanel.repaint();
    }

    private void setupKeyBindings() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                final Vector2D impulse = switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> new Vector2D(0, -IMPULSE_MAGNITUDE);
                    case KeyEvent.VK_DOWN -> new Vector2D(0, IMPULSE_MAGNITUDE);
                    case KeyEvent.VK_LEFT -> new Vector2D(-IMPULSE_MAGNITUDE, 0);
                    case KeyEvent.VK_RIGHT -> new Vector2D(IMPULSE_MAGNITUDE, 0);
                    default -> null;
                };
                if (impulse != null) {
                    observer.onDirectionInput(impulse);
                }
            }
        });
        setFocusable(true);
        requestFocusInWindow();
    }

    private String formatResult(final GameStatus status) {
        return switch (status) {
            case HUMAN_WINS -> "You Win!";
            case BOT_WINS -> "Bot Wins!";
            case DRAW -> "Draw!";
            default -> "";
        };
    }

    // -----------------------------------------------------------------------
    // Inner panel — all painting lives here
    // -----------------------------------------------------------------------

    private class BoardPanel extends JPanel {

        @Serial
        private static final long serialVersionUID = 1L;

        private final int boardWidth;
        private final int boardHeight;
        private String gameOverMessage;

        BoardPanel(final int w, final int h) {
            this.boardWidth = w;
            this.boardHeight = h;
        }

        void setGameOverMessage(final String msg) {
            this.gameOverMessage = msg;
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
        }

        // ── Rendering helpers ─────────────────────────────────────────────

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
    }
}
