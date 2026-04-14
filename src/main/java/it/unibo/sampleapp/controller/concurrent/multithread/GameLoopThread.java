package it.unibo.sampleapp.controller.concurrent.multithread;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.sampleapp.model.Model;
import it.unibo.sampleapp.view.View;
import it.unibo.sampleapp.util.FpsCounter;

import static it.unibo.sampleapp.controller.concurrent.GameLoopConstants.TICK_MS;
import static it.unibo.sampleapp.controller.concurrent.GameLoopConstants.TICK_S;

/**
 * The heartbeat of the game.
 *
 * <p>
 * Each tick: steps physics → grabs a snapshot → pushes it to the view.
 * Target: 60 fps → dt = ~16ms per tick.
 * Uses sleep-based timing — good enough for this use case.
 */
public final class GameLoopThread extends Thread {

    private final Model model;
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "View is provided by the application wiring and only used for update callbacks"
    )
    private final View view;
    private final FpsCounter fpsCounter = new FpsCounter();

    /**
     * Constructs a new GameLoopThread with the given model and view.
     *
     * @param model the game model to update
     * @param view the view to update with snapshots
     */
    public GameLoopThread(final Model model, final View view) {
        super("game-loop");
        this.model = model;
        this.view = view;
        setDaemon(true);   // dies when main thread dies
    }

    /**
     * Runs the game loop, updating physics and view at target frame rate.
     */
    @Override
    public void run() {
        while (!isInterrupted()) {
            final long startMs = System.currentTimeMillis();

            // 1. Advance physics
            model.applyPhysicsStep(TICK_S);

            // 2. Snapshot + push to view (lock held only during snapshot copy)
            view.update(model.getSnapshot());
            fpsCounter.tick(System.currentTimeMillis());

            // 3. Sleep for the remainder of the tick budget
            final long elapsed = System.currentTimeMillis() - startMs;
            final long sleepMs = TICK_MS - elapsed;
            if (sleepMs > 0) {
                try {
                    sleep(sleepMs);
                } catch (final InterruptedException e) {
                    currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * Stops the game loop thread.
     */
    public void stopLoop() {
        interrupt();
    }

    /**
     * Returns the current frames per second.
     *
     * @return the current FPS value
     */
    public int getCurrentFps() {
        return fpsCounter.getCurrentFps();
    }

}
