package solarcar.gui.tabs;

import solarcar.gui.guiElements.SolarArray;
import solarcar.gui.modules.MotorControllerStatusModule;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

public class MotorControllerTab extends SolarArray {
    @Override
    public void createPanels() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        add(new MotorControllerStatusModule(2), c); //Right

        c.gridx = 1;
        add(new MotorControllerStatusModule(1), c); //Left
    }

    public static String getTabName() {
        return "Motor Controller";
    }
}
