package solarcar.gui.guiElements;


import java.awt.Component;

abstract public class SolarArray extends SolarPanel {

    public abstract void createPanels();

    @Override
    public void init() {
		SplashScreenDrawer.get().splashText("Creating " + this.getClass().getName());
        createPanels();
        initPanels();
    }

    private void initPanels() {
        for (Component comp : getComponents()) {
            if (comp instanceof SolarPanel) {
                    ((SolarPanel) comp).init();
            }
        }
    }
}