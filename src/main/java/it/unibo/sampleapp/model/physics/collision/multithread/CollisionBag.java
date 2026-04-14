package it.unibo.sampleapp.model.physics.collision.multithread;

import it.unibo.sampleapp.model.domain.ball.Ball;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Monitor condiviso tra GameModel (Master) e CollisionWorker (Workers).
 *
 * <p>
 * Ciclo per ogni physics step:
 *  1. Master: submitPartitions()  → deposita partizioni, sveglia Worker
 *  2. Worker: takePartition()     → preleva una partizione (wait se vuoto)
 *  3. Worker: markPartitionDone() → segnala il completamento della partizione
 *  4. Master: awaitCompletion()   → wait finché pendingCount == 0
 */
public final class CollisionBag {

    private final Queue<List<Ball>> partitions = new LinkedList<>();
    private int pendingCount;

    /**
     * Chiamato dal Master: deposita le partizioni e sveglia i Worker in attesa.
     *
     * @param parts partizioni da processare nel prossimo physics step
     */
    public synchronized void submitPartitions(final List<List<Ball>> parts) {
        partitions.addAll(parts);
        pendingCount = parts.size();
        notifyAll();
    }

    /**
     * Chiamato dai Worker: preleva una partizione.
     * Se il bag è vuoto, il Worker si mette in wait().
     *
     * @return la prossima partizione da processare
     * @throws InterruptedException se il thread viene interrotto
     */
    public synchronized List<Ball> takePartition() throws InterruptedException {
        while (partitions.isEmpty()) {
            wait();
        }
        return partitions.poll();
    }

    /**
     * Chiamato dai Worker al termine di una partizione.
     * Decrementa il contatore e, se necessario, sveglia il Master.
     */
    public synchronized void markPartitionDone() {
        pendingCount--;
        if (pendingCount == 0) {
            notifyAll(); // sveglia il Master in awaitCompletion()
        }
    }

    /**
     * Chiamato dal Master: aspetta finché tutti i Worker hanno finito.
     * Barriera monitor pura — nessuna libreria esterna.
     *
     * @throws InterruptedException se il thread viene interrotto
     */
    public synchronized void awaitCompletion() throws InterruptedException {
        while (pendingCount > 0) {
            wait();
        }
    }
}


