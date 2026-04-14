package it.unibo.sampleapp.controller.concurrent.taskbased;

import it.unibo.sampleapp.controller.AbstractController;
import it.unibo.sampleapp.controller.concurrent.bot.BotMoveService;
import it.unibo.sampleapp.model.Model;
import it.unibo.sampleapp.util.FpsCounter;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static it.unibo.sampleapp.controller.concurrent.bot.BotAIConstants.BOT_MOVE_DELAY_MS;
import static it.unibo.sampleapp.controller.concurrent.bot.BotAIConstants.BOT_THINK_TIME_MS;
import static it.unibo.sampleapp.controller.concurrent.GameLoopConstants.TICK_MS;
import static it.unibo.sampleapp.controller.concurrent.GameLoopConstants.TICK_S;

/**
 * Controller implementation for the executor task-based runtime.
 */
public final class TaskBasedController extends AbstractController {

    private static final long SHUTDOWN_TIMEOUT_MS = 500L;

    private final BotMoveService botMoveService = new BotMoveService();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final FpsCounter fpsCounter = new FpsCounter();

    private ScheduledExecutorService gameLoopExecutor;
    private ScheduledExecutorService botExecutor;
    private ScheduledFuture<?> gameLoopFuture;
    private ScheduledFuture<?> botFuture;

    /**
     * @param model the game model to control
     */
    public TaskBasedController(final Model model) {
        super(model);
    }

    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        fpsCounter.reset(System.currentTimeMillis());

        gameLoopExecutor = Executors.newSingleThreadScheduledExecutor();
        botExecutor = Executors.newSingleThreadScheduledExecutor();

        try {
            gameLoopFuture = gameLoopExecutor.scheduleAtFixedRate(
                    this::runGameLoopStep,
                    0,
                    TICK_MS,
                    TimeUnit.MILLISECONDS
            );
            botFuture = botExecutor.scheduleWithFixedDelay(
                    this::runBotStep,
                    0,
                    BOT_MOVE_DELAY_MS + BOT_THINK_TIME_MS,
                    TimeUnit.MILLISECONDS
            );
            startGameOverWatcher();
        } catch (final RejectedExecutionException e) {
            stop();
            throw e;
        }
    }

    @Override
    public void stop() {
        if (!running.getAndSet(false)) {
            return;
        }

        cancelFuture(gameLoopFuture);
        cancelFuture(botFuture);
        shutdownExecutor(gameLoopExecutor);
        shutdownExecutor(botExecutor);

        gameLoopFuture = null;
        botFuture = null;
        gameLoopExecutor = null;
        botExecutor = null;
    }

    @Override
    public int getCurrentFps() {
        return fpsCounter.getCurrentFps();
    }

    private void runGameLoopStep() {
        model().applyPhysicsStep(TICK_S);
        view().update(model().getSnapshot());
        updateFpsCounters();
    }

    private void runBotStep() {
        final var snapshot = model().getSnapshot();
        final var direction = botMoveService.decideMove(snapshot);
        model().applyImpulseToBot(direction);
    }

    private void updateFpsCounters() {
        fpsCounter.tick(System.currentTimeMillis());
    }

    private void cancelFuture(final ScheduledFuture<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    private void shutdownExecutor(final ScheduledExecutorService executor) {
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (final InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

