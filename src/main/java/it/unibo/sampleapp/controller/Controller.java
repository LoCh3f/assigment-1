package it.unibo.sampleapp.controller;

import it.unibo.sampleapp.model.Model;
import it.unibo.sampleapp.view.View;

/**
 * Controller of the MVC.
 */
public interface Controller {

    /**
     * @param model for MVC.
     */
    void setModel(Model model);

    /**
     * @param view for the MVC.
     */
    void setView(View view);
}
