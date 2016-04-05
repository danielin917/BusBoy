package solarcar.gui.tabs;


import solarcar.gui.modules.MotorPowerGraph;
import solarcar.gui.guiElements.SolarArray;
import solarcar.gui.modules.TorqueGraph;
import solarcar.gui.modules.SpeedGraph;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

public class MotorTab extends SolarArray {
    //ArrayList<SafeSolarPanel> ssps;

    @Override
    public void createPanels() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        add(new TorqueGraph(), c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.gridheight = 1;
        add(new MotorPowerGraph("wider",3600,5000), c);

        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 2;
        add(new MotorPowerGraph("bigger",3600,5000), c);

        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        add(new SpeedGraph(), c);
    }

    public static String getTabName() {
        return "Motor";
    }
}
