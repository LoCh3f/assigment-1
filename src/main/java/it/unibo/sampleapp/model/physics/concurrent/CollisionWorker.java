package it.unibo.sampleapp.model.physics.concurrent;

import it.unibo.sampleapp.model.ball.Ball;
import it.unibo.sampleapp.model.physics.collision.CollisionPairResolver;

import java.util.List;

/**
 * Worker thread interno a GameModel.
 *
 * <p>
 * Preleva una partizione di Ball dal CollisionBag,
 * risolve le collisioni in place e notifica il completamento al master.
 *
 * <p>
 * Opera sempre sotto il monitor del modello, quindi non introduce race condition
 * sullo stato condiviso.
 */
public final class CollisionWorker extends Thread {
    private final CollisionBag bag;

    /**
     * Costruisce un CollisionWorker.
     *
     * @param id  identificativo del worker (per il nome del thread)
     * @param bag il monitor condiviso da cui preleva e in cui deposita
     */
    public CollisionWorker(final int id, final CollisionBag bag) {
        super("collision-worker-" + id);
        this.bag = bag;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                final List<Ball> partition = bag.takePartition();
                resolveCollisions(partition);
                bag.markPartitionDone();
            } catch (final InterruptedException e) {
                currentThread().interrupt();
            }
        }
    }

    /**
     * Risolve tutte le collisioni nella partizione in place.
     *
     * @param partition lista di Ball della zona spaziale assegnata
     */
    private void resolveCollisions(final List<Ball> partition) {
        for (int i = 0; i < partition.size(); i++) {
            for (int j = i + 1; j < partition.size(); j++) {
                CollisionPairResolver.resolve(partition.get(i), partition.get(j));
            }
        }
    }
}

