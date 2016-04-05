package solarcar.gui.tabs;


import solarcar.gui.modules.BatTempGraph;
import solarcar.gui.modules.AuxBmsModule;
import solarcar.gui.modules.ModuleVoltageBarGraph;
import solarcar.gui.modules.SOCGraph;
import solarcar.gui.guiElements.SolarArray;
import solarcar.gui.modules.BatteryImbalanceGraph;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import solarcar.gui.TelemetryGUI;

public class  ChargeTab extends SolarArray {

    @Override
    public void createPanels() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        add(new BatTempGraph(), c);

        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        add(new BatteryImbalanceGraph(), c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        add(new ModuleVoltageBarGraph(TelemetryGUI.QuarterGraph), c);

        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 2;
        add(new SOCGraph("Large",1200), c);

        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        add(new AuxBmsModule(), c);

    }
}
