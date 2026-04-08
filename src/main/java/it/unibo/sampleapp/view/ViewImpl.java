package it.unibo.sampleapp.view;

import it.unibo.sampleapp.controller.Controller;
import it.unibo.sampleapp.model.snapshot.GameSnapshot;
import it.unibo.sampleapp.model.status.GameStatus;
import it.unibo.sampleapp.util.Vector2D;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.sampleapp.view.board.BoardPanel;

import java.io.Serial;

import static it.unibo.sampleapp.view.costants.ViewConstants.COLOR_BOARD;
import static it.unibo.sampleapp.view.costants.ViewConstants.IMPULSE_MAGNITUDE;
import static it.unibo.sampleapp.view.costants.ViewConstants.MAX_POWER_MULTIPLIER;
import static it.unibo.sampleapp.view.costants.ViewConstants.MAX_POWER_TIME;
import static it.unibo.sampleapp.view.costants.ViewConstants.MIN_POWER_MULTIPLIER;

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

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final transient Controller controller;
    private final BoardPanel boardPanel;

    private boolean isAiming;
    private Point aimStartPoint;
    private Point aimEndPoint;
    private long aimStartTime;

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
        this.controller = observer;
        this.boardPanel = new BoardPanel(boardWidth, boardHeight);
        boardPanel.setPreferredSize(new Dimension(boardWidth, boardHeight));
        boardPanel.setBackground(COLOR_BOARD);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);
        getContentPane().add(boardPanel);
        pack();
        setLocationRelativeTo(null);

        setupKeyBindings();
        setupMouseControls();
    }

    /**
     * Sets the concurrency mode and updates the window title.
     *
     * @param mode the concurrency mode as a string
     */
    @Override
    public void setConcurrencyMode(final String mode) {
        setTitle("Pool - " + mode);
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
        boardPanel.setCurrentSnapshot(snapshot);
        boardPanel.setCurrentFps(controller.getCurrentFps());
        // Convert Point to Point2D.Double for aiming state
        final Point2D.Double startPoint = isAiming && aimStartPoint != null
                ? new Point2D.Double(aimStartPoint.x, aimStartPoint.y) : null;
        final Point2D.Double endPoint = isAiming && aimEndPoint != null
                ? new Point2D.Double(aimEndPoint.x, aimEndPoint.y) : null;
        boardPanel.setAimingState(isAiming, startPoint, endPoint,
                calculatePowerMultiplier());
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
                    controller.onDirectionInput(impulse);
                }
            }
        });
        setFocusable(true);
        requestFocusInWindow();
    }

    private void setupMouseControls() {
        boardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) { // Left click
                    isAiming = true;
                    aimStartPoint = e.getPoint();
                    aimStartTime = System.currentTimeMillis();
                }
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && isAiming) {
                    aimEndPoint = e.getPoint();
                    // Power must be computed while aiming is still active.
                    final double powerMultiplier = calculatePowerMultiplier();
                    controller.onShoot(aimStartPoint, aimEndPoint, powerMultiplier);
                    isAiming = false;
                }
            }
        });

        boardPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(final MouseEvent e) {
                if (isAiming) {
                    aimEndPoint = e.getPoint();
                    final double powerMultiplier = calculatePowerMultiplier();
                    controller.onAim(aimStartPoint, aimEndPoint, powerMultiplier);
                }
            }
        });
    }

    private double calculatePowerMultiplier() {
        if (isAiming) {
            final double elapsedTime = System.currentTimeMillis() - aimStartTime;
            return Math.min(MAX_POWER_MULTIPLIER, Math.max(MIN_POWER_MULTIPLIER,
                    elapsedTime / MAX_POWER_TIME));
        }
        return 0;
    }

    private String formatResult(final GameStatus status) {
        return switch (status) {
            case HUMAN_WINS -> "You Win!";
            case BOT_WINS -> "Bot Wins!";
            case DRAW -> "Draw!";
            default -> "";
        };
    }

}
