package it.unibo.sampleapp.util;

/**
 * Small helper that counts completed events per elapsed wall-clock second.
 */
public final class FpsCounter {

    private long framesThisSecond;
    private long lastSampleTimeMs;
    private int currentFps;

    /**
     * Builds a counter starting from the current time.
     */
    public FpsCounter() {
        this(System.currentTimeMillis());
    }

    /**
     * Builds a counter with an explicit initial timestamp.
     *
     * @param initialTimeMs initial wall-clock time in milliseconds
     */
    public FpsCounter(final long initialTimeMs) {
        this.lastSampleTimeMs = initialTimeMs;
    }

    /**
     * Records one event and updates the sampled FPS value when a full second has elapsed.
     *
     * @param nowMs current wall-clock time in milliseconds
     * @return the latest sampled FPS value
     */
    public synchronized int tick(final long nowMs) {
        framesThisSecond++;
        if (nowMs - lastSampleTimeMs >= 1000) {
            currentFps = (int) framesThisSecond;
            framesThisSecond = 0;
            lastSampleTimeMs = nowMs;
        }
        return currentFps;
    }

    /**
     * @return the most recently sampled FPS value
     */
    public synchronized int getCurrentFps() {
        return currentFps;
    }

    /**
     * Resets the counter using the provided timestamp as new sampling origin.
     *
     * @param nowMs current wall-clock time in milliseconds
     */
    public synchronized void reset(final long nowMs) {
        framesThisSecond = 0;
        currentFps = 0;
        lastSampleTimeMs = nowMs;
    }
}

