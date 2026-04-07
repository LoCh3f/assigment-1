package it.unibo.sampleapp;

import it.unibo.sampleapp.controller.ControllerImpl;
import it.unibo.sampleapp.model.GameModel;
import it.unibo.sampleapp.model.GameModelImpl;
import it.unibo.sampleapp.view.View;
import it.unibo.sampleapp.view.ViewImpl;

/**
 * Entry point. Wires Model → Controller → View and starts the game.
 * This is the only class that knows all three concrete types.
 */
public final class Main {

    private static final int BOARD_W = 900;
    private static final int BOARD_H = 600;
    private static final int NUM_SMALL_BALLS = 500;

    /**
     * Concurrency mode enum.
     */
    public enum ConcurrencyMode {
        MULTITHREAD, TASKBASED
    }

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private Main() {
    }

    /**
     * Main entry point. Builds the MVC components and starts the game.
     *
     * @param args command line arguments: optional "taskbased" to use task-based version
     */
    public static void main(final String[] args) {
        final ConcurrencyMode mode = args.length > 0 && "taskbased".equalsIgnoreCase(args[0])
                ? ConcurrencyMode.TASKBASED
                : ConcurrencyMode.MULTITHREAD;

        // 1. Build model
        final GameModel model = new GameModelImpl(BOARD_W, BOARD_H, NUM_SMALL_BALLS);

        // 2. Build controller (no view yet)
        final ControllerImpl controller = new ControllerImpl(model, mode);

        // 3. Build view (needs observer = controller)
        final View view = new ViewImpl(BOARD_W, BOARD_H, controller);

        // 4. Complete the wiring
        controller.setView(view);
        view.setConcurrencyMode(mode.name());

        // 5. Show window and start
        view.show();
        controller.start();
    }
}
