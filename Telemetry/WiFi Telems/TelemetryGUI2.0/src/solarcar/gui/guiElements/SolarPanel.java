package solarcar.gui.guiElements;

import javax.swing.JPanel;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author aaresh
 */
abstract public class SolarPanel extends JPanel {

    public SolarPanel() {
        super();
		SplashScreenDrawer.get().splashText("Creating " + this.getClass().getName());
    }

    public SolarPanel(String params[]) {
        super();
    }

    // init function called after object is constructed;
    // allows classes to send 'this' to other classes without leaking 'this'
    // in the constructor
    abstract public void init();
}
