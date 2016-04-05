package solarcar.gui.tabs;


import solarcar.gui.modules.MotorPowerGraph;
import solarcar.gui.guiElements.SolarArray;
import solarcar.gui.modules.PowerInGraph;
import solarcar.gui.modules.PowerDeficitGraph;
import solarcar.gui.modules.VMinMaxGraph;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import solarcar.gui.modules.BatteryPowerGraph;

public class  BottomOfPackTab extends SolarArray {

    @Override
    public void createPanels() {
        //setNoOfModules(4);
        //workers[0] = new VMinMaxGraph();
        //workers[1] = new MotorPowerGraph();
        //workers[2] = new PowerDeficitGraph();
        // workers[3] = new PowerInGraph();

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        /*
         * panels = new SolarPanel[NUM_MODULES]; for(int i = 0; i < NUM_MODULES;
         * i++) { panels[i] = new SolarPanel(workers[i], socket);wd //
         * add(panels[i]); }
         */
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        add(new VMinMaxGraph(), c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.gridheight = 1;
        add(new MotorPowerGraph("wider",1200,5000), c);

        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 2;
        add(new BatteryPowerGraph(), c);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        add(new PowerInGraph(), c);
    }

    @Override
    public String getName() {
		
        return "Bottom of Pack";
    }
}