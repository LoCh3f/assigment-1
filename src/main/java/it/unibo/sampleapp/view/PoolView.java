package it.unibo.sampleapp.view;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import it.unibo.sampleapp.controller.Controller;

import javax.swing.JFrame;

/**
 * Basic implementation for the view of the MVC.
 */
public class PoolView implements View {

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final String TITLE = "Pool";
    private Controller controller;
    private final JFrame frame;

    /**
     * Build a Simple Version of the view, must be called show method after.
     */
    public PoolView() {
        this.frame = new JFrame();
        this.frame.setSize(WIDTH, HEIGHT);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setResizable(true);
        this.frame.setTitle(TITLE);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void show() {
        this.frame.setVisible(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings(value = "EI_EXPOSE_REP", justification = "Controller interface should be mutable")
    public void setController(final Controller controller) {
        this.controller = controller;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings(value = "EI_EXPOSE_REP", justification = "Controller interface should be mutable")
    public Controller getController() {
        return this.controller;
    }

}
