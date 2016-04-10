package solarcar.gui.modules;


import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import solarcar.gui.guiElements.SolarArray;
import solarcar.gui.TelemetryGUI;

public class  PowerPane extends SolarArray {

    @Override
    public void createPanels() {
        setPreferredSize(new Dimension(((TelemetryGUI.WindowWidth * 3 / 4) - 5), TelemetryGUI.TopPaneHeight));
        setLayout(new FlowLayout(FlowLayout.LEADING));
		//setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
		//c.fill = GridBagConstraints.BOTH;
		//c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;
        add(new ArrayOverview(), c);

        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        add(new SOCModule(), c);

        c.gridx = 3;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        add(new SquawkModule(), c);
        
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.gridheight = 1;
        add(new BatteryOverviewPanel(), c);

        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        add(new MotorControllerOverviewModule(), c);
		
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.gridheight = 1;
        add(new MotorOverviewPanel(), c);

        c.gridx = 2;
        c.gridy = 2;
        c.gridwidth = 2;
        c.gridheight = 1;
        add(new CruiseStatusModule(), c);
        
        /*c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        add(new FSGPModule(), c);*/


    }
	
	@Override
	public String toString() {
		return "Power Pane";
	}
}
