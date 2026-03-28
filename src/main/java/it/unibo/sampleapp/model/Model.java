package it.unibo.sampleapp.model;

import it.unibo.sampleapp.controller.Controller;

/**
 * Model interface for MVC.
 */
public interface Model {
    /**
     * @param controller for the MVC.
     */
    void setController(Controller controller);

    /**
     * @return controller associated to the model.
     */
    Controller getController();
}
