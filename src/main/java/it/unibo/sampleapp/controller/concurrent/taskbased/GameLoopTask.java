package it.unibo.sampleapp.concurrent.taskbased;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.sampleapp.model.GameModel;
import it.unibo.sampleapp.view.View;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The heartbeat of the game using task-based approach with Executor Framework.
 *
 * <p>
 * Each tick: steps physics → grabs a snapshot → pushes it to the view.
 * Target: 60 fps → dt = ~16ms per tick.
 * Uses ScheduledExecutorService for timing.
 */
public final class GameLoopTask implements Runnable {

    private static final long TARGET_FPS = 60;
    private static final long TICK_MS = 1000 / TARGET_FPS;
    private static final double TICK_S = TICK_MS / 1000.0;

    private final GameModel model;
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "View is application-owned and only used to push immutable snapshots"
    )
    private final View view;
    private final AtomicInteger framesThisSecond = new AtomicInteger(0);
    private long lastFpsTimeMs;
    private final AtomicInteger currentFps = new AtomicInteger(0);
    private ScheduledFuture<?> future;

    /**
     * Constructs a new GameLoopTask with the given model and view.
     *
     * @param model the game model to update
     * @param view the view to update with snapshots
     */
    public GameLoopTask(final GameModel model, final View view) {
        this.model = model;
        this.view = view;
        this.lastFpsTimeMs = System.currentTimeMillis();
    }

    /**
     * Runs one tick of the game loop: advance physics and update view.
     */
    @Override
    public void run() {
        // 1. Advance physics
        model.applyPhysicsStep(TICK_S);

        // 2. Snapshot + push to view (lock held only during snapshot copy)
        view.update(model.getSnapshot());
        framesThisSecond.incrementAndGet();
        final long now = System.currentTimeMillis();
        if (now - lastFpsTimeMs >= 1000) {
            currentFps.set(framesThisSecond.get());
            framesThisSecond.set(0);
            lastFpsTimeMs = now;
        }
    }

    /**
     * Starts the game loop by scheduling this task on the given executor.
     *
     * @param executor the executor to schedule on
     */
    public void start(final ScheduledExecutorService executor) {
        future = executor.scheduleAtFixedRate(this, 0, TICK_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the game loop.
     */
    public void stop() {
        if (future != null) {
            future.cancel(true);
        }
    }

    /**
     * Returns the current frames per second.
     *
     * @return the current FPS value
     */
    public int getCurrentFps() {
        return currentFps.get();
    }
}
