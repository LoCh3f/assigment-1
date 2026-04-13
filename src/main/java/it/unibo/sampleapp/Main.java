package it.unibo.sampleapp;

import it.unibo.sampleapp.controller.ControllerImpl;
import it.unibo.sampleapp.model.Model;
import it.unibo.sampleapp.model.GameModel;
import it.unibo.sampleapp.view.View;
import it.unibo.sampleapp.view.ViewImpl;

/**
 * Entry point. Wires Model → Controller → View and starts the game.
 * This is the only class that knows all three concrete types.
 */
public final class Main {

    private static final int BOARD_W = 1920;
    private static final int BOARD_H = 1080;
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

        final Model model = new GameModel(BOARD_W, BOARD_H, NUM_SMALL_BALLS);

        final ControllerImpl controller = new ControllerImpl(model, mode);

        final View view = new ViewImpl(BOARD_W, BOARD_H, controller);

        controller.setView(view);
        view.setConcurrencyMode(mode.name());

        view.show();
        controller.start();
    }
}
