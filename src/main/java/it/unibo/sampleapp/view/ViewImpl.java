package it.unibo.sampleapp.view;

import it.unibo.sampleapp.controller.Controller;
import it.unibo.sampleapp.model.snapshot.GameSnapshot;
import it.unibo.sampleapp.model.status.GameStatus;
import it.unibo.sampleapp.util.Vector2D;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.sampleapp.view.board.BoardPanel;

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
    private static final double IMPULSE_MAGNITUDE = 1.0;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final transient Controller controller;
    private final BoardPanel boardPanel;

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
        boardPanel.setCurrentSnapshot(snapshot);
        boardPanel.setCurrentFps(controller.getCurrentFps());
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

    private String formatResult(final GameStatus status) {
        return switch (status) {
            case HUMAN_WINS -> "You Win!";
            case BOT_WINS -> "Bot Wins!";
            case DRAW -> "Draw!";
            default -> "";
        };
    }

}
