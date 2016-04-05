package solarcar.gui.tabs;


import solarcar.gui.modules.AuxBmsModule;
import solarcar.gui.modules.BatTempGraphBig;
import solarcar.gui.modules.BMSOverviewMap;
import solarcar.gui.guiElements.SolarArray;
import java.awt.FlowLayout;
import solarcar.gui.modules.BMSAutoCalMap;

public class  BMSAutoCalTab extends SolarArray {

    @Override
    public void createPanels() {
        setLayout(new FlowLayout(FlowLayout.LEADING));
        add(new BMSAutoCalMap());
    }
	
	@Override
	public String toString() {
		return "Battery tab";
	}
}
