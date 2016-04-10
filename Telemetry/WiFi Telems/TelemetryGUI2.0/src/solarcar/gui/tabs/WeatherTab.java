package solarcar.gui.tabs;


import solarcar.gui.modules.ElevationGraph;
import solarcar.gui.modules.SegmentIdentifier;
import solarcar.gui.modules.AnemometerModule;
import solarcar.gui.modules.WeatherOverviewModule;
import solarcar.gui.modules.SolarRadiationGraph;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import solarcar.gui.guiElements.SolarArray;

public class  WeatherTab extends SolarArray {

    @Override
    public void createPanels() {
        //setNoOfModules(5);
        //workers[0] = new WeatherOverviewModule();
        //workers[1] = new AnemometerModule();
        //workers[2] = new SolarRadiationGraph();
        //workers[3] = new SegmentIdentifier();
        //workers[4] = new ElevationGraph();
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        c.gridheight = 1;
        add(new WeatherOverviewModule(), c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.gridheight = 2;
        add(new AnemometerModule(), c);

        c.gridx = 2;
        c.gridy = 2;
        c.gridwidth = 2;
        c.gridheight = 2;
        add(new SolarRadiationGraph(), c);

        c.gridx = 4;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 2;
        add(new SegmentIdentifier(), c);

        c.gridx = 4;
        c.gridy = 2;
        c.gridwidth = 2;
        c.gridheight = 1;
        add(new ElevationGraph(), c);
    }

    public String getTabName() {
        return "Weather";
    }
}