package it.unibo.sampleapp.model.physics.step.multithread;

import it.unibo.sampleapp.model.domain.ball.Ball;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Shared monitor between master thread and step workers.
 */
public final class PhysicsStepBag {

    private final Queue<StepTask> tasks = new LinkedList<>();
    private int pendingCount;

    /**
     * Called by the master to enqueue step tasks for the next physics tick.
     *
     * @param partitions step tasks for all chunks
     */
    public synchronized void submitPartitions(final List<StepTask> partitions) {
        tasks.addAll(partitions);
        pendingCount = partitions.size();
        notifyAll();
    }

    /**
     * Called by workers to retrieve the next task.
     *
     * @return next available task
     * @throws InterruptedException if interrupted while waiting
     */
    public synchronized StepTask takeTask() throws InterruptedException {
        while (tasks.isEmpty()) {
            wait();
        }
        return tasks.poll();
    }

    /**
     * Called by workers when one task completes.
     */
    public synchronized void markTaskDone() {
        pendingCount--;
        if (pendingCount == 0) {
            notifyAll();
        }
    }

    /**
     * Called by the master to wait until all tasks complete.
     *
     * @throws InterruptedException if interrupted while waiting
     */
    public synchronized void awaitCompletion() throws InterruptedException {
        while (pendingCount > 0) {
            wait();
        }
    }

    /**
     * Task unit processed by each step worker.
     *
     * @param partition chunk of balls to process
     * @param boardW board width
     * @param boardH board height
     * @param dt time step
     */
    public record StepTask(List<Ball> partition, double boardW, double boardH, double dt) {
        /**
         * Canonical constructor with defensive copy for the partition.
         *
         * @param partition chunk of balls to process
         * @param boardW board width
         * @param boardH board height
         * @param dt time step
         */
        public StepTask {
            partition = List.copyOf(partition);
        }
    }
}




