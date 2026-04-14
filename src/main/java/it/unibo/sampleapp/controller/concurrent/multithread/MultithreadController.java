package it.unibo.sampleapp.controller.concurrent.multithread;

import it.unibo.sampleapp.controller.AbstractController;
import it.unibo.sampleapp.model.Model;

/**
 * Controller implementation for the explicit thread-based runtime.
 */
public final class MultithreadController extends AbstractController {

    private GameLoopThread gameLoopThread;
    private BotThread botThread;

    /**
     * @param model the game model to control
     */
    public MultithreadController(final Model model) {
        super(model);
    }

    @Override
    public void start() {
        gameLoopThread = new GameLoopThread(model(), view());
        botThread = new BotThread(model());
        gameLoopThread.start();
        botThread.start();
        startGameOverWatcher();
    }

    @Override
    public void stop() {
        if (gameLoopThread != null) {
            gameLoopThread.stopLoop();
        }
        if (botThread != null) {
            botThread.stopBot();
        }
    }

    @Override
    public int getCurrentFps() {
        return gameLoopThread != null ? gameLoopThread.getCurrentFps() : 0;
    }
}

