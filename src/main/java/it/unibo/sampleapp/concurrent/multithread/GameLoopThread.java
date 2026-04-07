package it.unibo.sampleapp.concurrent.multithread;

import it.unibo.sampleapp.model.GameModel;
import it.unibo.sampleapp.view.View;

/**
 * The heartbeat of the game.
 *
 * <p>
 * Each tick: steps physics → grabs a snapshot → pushes it to the view.
 * Target: 60 fps → dt = ~16ms per tick.
 * Uses sleep-based timing — good enough for this use case.
 */
public final class GameLoopThread extends Thread {

    private static final long TARGET_FPS = 60;
    private static final long TICK_MS = 1000 / TARGET_FPS;
    private static final double TICK_S = TICK_MS / 1000.0;

    private final GameModel model;
    private final View view;
    private long framesThisSecond;
    private long lastFpsTimeMs;
    private volatile int currentFps;

    /**
     * Constructs a new GameLoopThread with the given model and view.
     *
     * @param model the game model to update
     * @param view the view to update with snapshots
     */
    public GameLoopThread(final GameModel model, final View view) {
        super("game-loop");
        this.model = model;
        this.view = view;
        this.framesThisSecond = 0;
        this.lastFpsTimeMs = System.currentTimeMillis();
        this.currentFps = 0;
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
            framesThisSecond++;
            final long now = System.currentTimeMillis();
            if (now - lastFpsTimeMs >= 1000) {
                currentFps = (int) framesThisSecond;
                framesThisSecond = 0;
                lastFpsTimeMs = now;
            }

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
        return currentFps;
    }

}
