package solarcar.gui.tabs;


import solarcar.gui.modules.MotorDetailPanel;
import solarcar.gui.modules.wsMotVoltVecGraph;
import java.awt.FlowLayout;
import solarcar.gui.guiElements.SolarArray;

public class  WavesculptorTab extends SolarArray {
    //ArrayList<SafeSolarPanel> ssps;

    @Override
    public void createPanels() {
        setLayout(new FlowLayout());

        add(new wsMotVoltVecGraph());
        add(new MotorDetailPanel());
    }

    public String getTabName() {
        return "Strategy";
    }
}
