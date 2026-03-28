package it.unibo.sampleapp.view;

import it.unibo.sampleapp.controller.Controller;

/**
 * View interface.
 */
public interface View {
    /**
     * Shows the view, in particular the JFrame.
     */
    void show();

    /**
     * @param controller for the MVC pattern
     */
    void setController(Controller controller);

    /**
     * @return controller of the MVC associated to this view.
     */
    Controller getController();
}
