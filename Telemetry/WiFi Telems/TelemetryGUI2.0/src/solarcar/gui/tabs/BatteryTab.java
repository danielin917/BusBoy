package solarcar.gui.tabs;


import solarcar.gui.guiElements.SolarArray;
import java.awt.FlowLayout;
import solarcar.gui.modules.BMSAutoCalMap;
import solarcar.gui.modules.BMSTempOverviewMap;
import solarcar.gui.modules.BMSVoltOverviewMap;

public class  BatteryTab extends SolarArray {

    @Override
    public void createPanels() {
        setLayout(new FlowLayout(FlowLayout.LEADING));
        //add(new BMSOverviewMap());
        add(new BMSVoltOverviewMap());
        add(new BMSTempOverviewMap());
        // add(new BMSAutoCalMap());

      //  add(new AuxBmsModule());
      //  add(new BatTempGraphBig());
    }
	
	@Override
	public String toString() {
		return "Battery tab";
	}
}
